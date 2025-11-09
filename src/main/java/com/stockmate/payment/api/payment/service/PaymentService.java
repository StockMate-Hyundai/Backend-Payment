package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.*;
import com.stockmate.payment.api.payment.dto.common.PageResponseDto;
import com.stockmate.payment.api.payment.dto.order.*;
import com.stockmate.payment.api.payment.dto.payment.DepositTransactionResponseDto;
import com.stockmate.payment.api.payment.dto.payment.MonthlyPayResponseDto;
import com.stockmate.payment.api.payment.entity.*;
import com.stockmate.payment.api.payment.repository.BalanceRepository;
import com.stockmate.payment.api.payment.repository.DepositTransactionRepository;
import com.stockmate.payment.api.payment.repository.PaymentRepository;
import com.stockmate.payment.common.exception.BadRequestException;
import com.stockmate.payment.common.exception.NotFoundException;
import com.stockmate.payment.common.producer.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        DepositTransaction depositTransaction = DepositTransaction.of(amount, balance, userId);
        depositTransactionRepository.save(depositTransaction);

        log.info("✅ 예치금 충전 완료 - userId: {}, 최종 잔액: {}", userId, balance.getBalance());
    }

    // 예치금 결제 처리
    @Transactional
    public PayResponseEvent handleDepositPayRequest(PayRequestEvent event) {
        log.info("💳 결제 요청 수신 - orderId: {}, payAmount: {}", event.getOrderId(), event.getTotalPrice());

        Payment pay = Payment.of(event, PaymentStatus.REQUESTED);

        try {
            // 주문 검증
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

            // 잔액 확인
            Balance balance = balanceRepository.findBalanceByUserIdWithLock(event.getMemberId());
            if (balance == null) {
                throw new IllegalStateException("잔액 정보 없음");
            }
            if (balance.getBalance() < event.getTotalPrice()) {
                throw new IllegalStateException("잔액 부족");
            }

            // 차감 및 결제 완료
            balance.setBalance(balance.getBalance() - event.getTotalPrice());
            balanceRepository.save(balance);

            pay.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(pay);

            DepositTransaction depositTransaction = DepositTransaction.of(pay, balance, event.getMemberId());
            depositTransactionRepository.save(depositTransaction);

            log.info("✅ 결제 성공 - userId: {}, 차감 금액: {}, 잔여 잔액: {}",
                    event.getMemberId(), event.getTotalPrice(), balance.getBalance());

            //            kafkaProducerService.sendPaySuccess(response);

            return PayResponseEvent.of(event, true, null);

        } catch (Exception e) {
            log.error("❌ 결제 실패 - orderId={}, reason={}", event.getOrderId(), e.getMessage());

            pay.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(pay);

//            kafkaProducerService.sendPayFailed(response);

            return PayResponseEvent.of(event, false, e.getMessage());
        }
    }

    // 예치금 결제 취소 처리
    @Transactional
    public PayCancelResponseEvent handleDepositPayCancelRequest(PayCancelRequestEvent event) {
        log.info("💳 결제 취소 요청 수신 - orderId: {}, payAmount: {}", event.getOrderId(), event.getTotalPrice());

        Payment payment = paymentRepository.findByOrderNumber(event.getOrderNumber());

        try {
            // 결제 내역 확인

            if (payment == null) {
                throw new IllegalStateException("결제 정보 없음");
            }

            // 이미 취소된 결제면 중복 처리 방지
            if (payment.getStatus() == PaymentStatus.REFUNDED) {
                log.warn("⚠️ 이미 취소된 결제 - orderId: {}", event.getOrderId());
                throw new IllegalStateException("이미 취소된 결제입니다.");
            }

            // 잔액 복원
            Balance balance = balanceRepository.findBalanceByUserIdWithLock(event.getMemberId());
            if (balance == null) throw new IllegalStateException("잔액 정보 없음");

            balance.setBalance(balance.getBalance() + event.getTotalPrice());
            balanceRepository.save(balance);

            // 결제 상태 변경
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // 트랜잭션 저장
            DepositTransaction depositTransaction = DepositTransaction.cancel(payment, balance, event.getMemberId());
            depositTransactionRepository.save(depositTransaction);

            // 성공 이벤트 발행
            // kafkaProducerService.sendCancelSuccess(response);

            log.info("✅ 결제 취소 완료 - userId: {}, 환불 금액: {}, 복원 후 잔액: {}",
                    event.getMemberId(), event.getTotalPrice(), balance.getBalance());

            return PayCancelResponseEvent.of(event, true, null);

        } catch (Exception e) {
            log.error("❌ 결제 취소 실패 - orderId={}, reason={}", event.getOrderId(), e.getMessage());

            if (payment != null) {
                payment.setStatus(PaymentStatus.CANCEL_FAILED);
                paymentRepository.save(payment);
            }
//            kafkaProducerService.sendCancelFailed(response);
            return PayCancelResponseEvent.of(event, false, e.getMessage());

        }
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

    // 예치금 거래내역 (배치로 주문 상세 조회)
    public PageResponseDto<DepositTransactionResponseDto> getDepositTransaction(Long userId, int page, int size) {
        log.info("[Deposit] ✅ 거래내역 조회 요청 ─ userId={}, page={}, size={}", userId, page, size);

        if (page < 0 || size <= 0)
            throw new BadRequestException("페이지 번호나 사이즈가 유효하지 않습니다.");

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<DepositTransaction> depositTransaction = depositTransactionRepository.findAllByUserId(userId, pageable);

        /** orderId 목록 수집  */
        List<Long> orderIds = depositTransaction.getContent().stream()
                .map(DepositTransaction::getPayment)
                .filter(p -> p != null && p.getOrderId() != null)
                .map(Payment::getOrderId)
                .distinct()
                .toList();

        /** batch 호출 → orderId → detail 매핑 */
        Map<Long, List<DepositPartDetailDTO>> orderDetailMap = new HashMap<>();

        if (!orderIds.isEmpty()) {
            try {
                List<DepositListResponseDTO> details = orderService.getOrderDetailBatch(orderIds);

                // ✅ orderId 로 묶기
                orderDetailMap = details.stream()
                        .collect(Collectors.toMap(
                                DepositListResponseDTO::getOrderId,    // key
                                DepositListResponseDTO::getOrderItems       // value
                        ));

                log.info("[Deposit] ✅ Batch 주문 상세 조회 성공");

            } catch (Exception e) {
                log.warn("[Deposit] ⚠ Batch 조회 실패 ─ msg={}", e.getMessage());
            }
        }

        /** Page 매핑 */
        Map<Long, List<DepositPartDetailDTO>> finalOrderDetailMap = orderDetailMap;

        Page<DepositTransactionResponseDto> mapped = depositTransaction.map(dt -> {

            Long orderId = null;
            Payment payment = dt.getPayment();
            if (payment != null) {
                orderId = payment.getOrderId();
            }

            List<DepositPartDetailDTO> partDetail = null;
            if (orderId != null) {
                partDetail = finalOrderDetailMap.getOrDefault(orderId, null);
            }

            return DepositTransactionResponseDto.of(dt, partDetail);
        });

        log.info("[Deposit] 거래내역 조회 완료 ─ totalElements={}, totalPages={}, currentPage={}",
                mapped.getTotalElements(), mapped.getTotalPages(), mapped.getNumber());

        return PageResponseDto.from(mapped);
    }

    @Transactional
    public void makeDeposit(Long userId) {
        log.info("[makeDeposit] 요청 수신 - userId={}", userId);

        if (balanceRepository.existsByUserId(userId)) {
            log.warn("[makeDeposit] 이미 예치금 row 존재 - userId={}", userId);
            return;
        }

        try {
            Balance balance = Balance.builder()
                    .userId(userId)
                    .balance(0L)
                    .build();
            balanceRepository.save(balance);
            log.info("[makeDeposit] 예치금 row 생성 완료 - userId={}, balance={}", userId, balance.getBalance());
        } catch (DataIntegrityViolationException e) {
            log.warn("[makeDeposit] 동시 요청으로 인한 중복 생성 시도 무시 - userId={}", userId);
        }
    }
}