package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.*;
import com.stockmate.payment.api.payment.entity.*;
import com.stockmate.payment.api.payment.repository.BalanceRepository;
import com.stockmate.payment.api.payment.repository.DepositTransactionRepository;
import com.stockmate.payment.api.payment.repository.PaymentRepository;
import com.stockmate.payment.common.exception.NotFoundException;
import com.stockmate.payment.common.producer.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final BalanceRepository balanceRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaProducerService kafkaProducerService;
    private final DepositTransactionRepository depositTransactionRepository;

    // 예치금 조회
    public Balance getDeposit(Long userId) {
        Balance balance = balanceRepository.findByUserId(userId);

        // TODO: 테이블 정보가 없을 때 0을 return 하도록
        if (balance == null) {
            log.warn("⚠️ 예치금 정보 없음 - userId: {}", userId);
            throw new NotFoundException("예치금 정보가 존재하지 않습니다. userId=" + userId);
        }

        log.info("💰 예치금 조회 - userId: {}, 잔액: {}", userId, balance.getBalance());
        return balance;
    }

    // 예치금 충전
    @Transactional
    public void depositCharge(Long userId, Long amount) {
        log.info("💰 예치금 충전 요청 - userId: {}, 금액: {}", userId, amount);

        Balance balance = balanceRepository.findBalanceByUserIdWithLock(userId);
        if (balance == null) {
            balance = new Balance();
            balance.setUserId(userId);
            balance.setBalance(0L);
        }

        balance.setBalance(balance.getBalance() + amount);
        balanceRepository.save(balance);

        DepositTransaction depositTransaction = DepositTransaction.of(TransactionType.CHARGE, balance);
        depositTransactionRepository.save(depositTransaction);

        log.info("✅ 예치금 충전 완료 - userId: {}, 최종 잔액: {}", userId, balance.getBalance());
    }

    // 예치금 결제 처리
    @Transactional
    public PayResponseEvent handleDepositPayRequest(PayRequestEvent event) {
        log.info("💳 결제 요청 수신 - orderId: {}, payAmount: {}", event.getOrderId(), event.getTotalPrice());

        Payment pay = Payment.of(event, PaymentStatus.REQUESTED);

        try {
            // TODO: 검증 요청
            // ✅ 1. 주문 검증
//            ValidateDto validate = orderService.getOrderByOrderId(event.getOrderId());
//            if (validate == null) {
//                throw new IllegalStateException("Order 서버 검증 실패 (null 응답)");
//            }
//            if (validate.getTotalPrice() != event.getTotalPrice()) {
//                throw new IllegalArgumentException("결제 금액 불일치");
//            }
//
//            if (event.getOrderStatus() != OrderStatus.ORDER_COMPLETED) {
//                throw new IllegalStateException("결제 불가 상태: " + validate.getOrderStatus());
//            }

            // ✅ 2. 잔액 확인
            Balance balance = balanceRepository.findBalanceByUserIdWithLock(event.getMemberId());
            if (balance == null) {
                throw new IllegalStateException("잔액 정보 없음");
            }
            if (balance.getBalance() < event.getTotalPrice()) {
                throw new IllegalStateException("잔액 부족");
            }

            // ✅ 3. 차감 및 결제 완료
            balance.setBalance(balance.getBalance() - event.getTotalPrice());
            balanceRepository.save(balance);

            pay.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(pay);

            DepositTransaction depositTransaction = DepositTransaction.of(pay, TransactionType.PAY, balance);
            depositTransactionRepository.save(depositTransaction);

            log.info("✅ 결제 성공 - userId: {}, 차감 금액: {}, 잔여 잔액: {}",
                    event.getMemberId(), event.getTotalPrice(), balance.getBalance());

            PayResponseEvent response = PayResponseEvent.of(event, true, null);
//            kafkaProducerService.sendPaySuccess(response);

            return response;

        } catch (Exception e) {
            log.error("❌ 결제 실패 - orderId={}, reason={}", event.getOrderId(), e.getMessage());

            pay.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(pay);

            PayResponseEvent response = PayResponseEvent.of(event, false, e.getMessage());
//            kafkaProducerService.sendPayFailed(response);

            return response;
        }
    }

    // 예치금 결제 취소 처리
    @Transactional
    public CancelResponseEvent handleDepositCancelRequest(CancelRequestEvent event) {
        log.info("💳 결제 취소 요청 수신 - orderId: {}, payAmount: {}", event.getOrderId(), event.getTotalPrice());

        try {
            // ✅ 1. 결제 내역 확인
            Payment payment = paymentRepository.findByOrderNumber(event.getOrderNumber());
            if (payment == null) throw new IllegalStateException("결제 정보 없음");

            // 이미 취소된 결제면 중복 처리 방지
            if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.REFUNDED) {
                log.warn("⚠️ 이미 취소된 결제 - orderId: {}", event.getOrderId());
                throw new IllegalStateException("이미 취소된 결제입니다.");
            }

            // ✅ 2. 잔액 복원
            Balance balance = balanceRepository.findBalanceByUserIdWithLock(event.getMemberId());
            if (balance == null) throw new IllegalStateException("잔액 정보 없음");

            balance.setBalance(balance.getBalance() + event.getTotalPrice());
            balanceRepository.save(balance);

            // ✅ 3. 결제 상태 변경
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            log.info("✅ 결제 취소 완료 - userId: {}, 환불 금액: {}, 복원 후 잔액: {}",
                    event.getMemberId(), event.getTotalPrice(), balance.getBalance());

            // ✅ 4. 성공 이벤트 발행
            CancelResponseEvent response = CancelResponseEvent.of(event);
            kafkaProducerService.sendCancelSuccess(response); // 결제 성공/취소 공용 토픽으로 발행

        } catch (IllegalStateException e) {
            log.error("❌ 결제 취소 실패 - orderId={}, reason={}", event.getOrderId(), e.getMessage());

            CancelResponseEvent response = CancelResponseEvent.of(event);
            kafkaProducerService.sendCancelFailed(response);
        } catch (Exception e) {
            log.error("💥 시스템 오류 - orderId={}, ex={}", event.getOrderId(), e.toString(), e);

            PayResponseEvent response = PayResponseEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .approvalAttemptId("CANCEL-" + System.currentTimeMillis())
                    .build();

            kafkaProducerService.sendPayFailed(response);
        }
        return null;
    }

    // 최근 5개월 지출 정보
    public List<MonthlyPayResponseDto> getLast5MonthSpending(Long userId) {
        log.info("[MonthlyPay] 최근 5개월 지출 조회 시작 ─ userId={}", userId);

        List<MonthlyPayResponseDto> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 0; i < 5; i++) {
            LocalDate target = now.minusMonths(i);
            int year = target.getYear();
            int month = target.getMonthValue();

            Long sum = paymentRepository.findMonthlySpending(userId, year, month);
            if (sum == null) sum = 0L;

            String ym = String.format("%04d-%02d", year, month);
            MonthlyPayResponseDto dto = MonthlyPayResponseDto.of(ym, sum);
            result.add(dto);
        }

        log.info("[MonthlyPay] 최근 5개월 result = {}", result);
        return result;
    }
}