package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.PayRequestEventDto;
import com.stockmate.payment.api.payment.dto.PayResponseEvent;
import com.stockmate.payment.api.payment.dto.ValidateDto;
import com.stockmate.payment.api.payment.entity.Balance;
import com.stockmate.payment.api.payment.entity.OrderStatus;
import com.stockmate.payment.api.payment.entity.Payment;
import com.stockmate.payment.api.payment.entity.PaymentStatus;
import com.stockmate.payment.api.payment.repository.BalanceRepository;
import com.stockmate.payment.api.payment.repository.PaymentRepository;
import com.stockmate.payment.common.producer.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final BalanceRepository balanceRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaProducerService kafkaProducerService;

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

        log.info("✅ 예치금 충전 완료 - userId: {}, 최종 잔액: {}", userId, balance.getBalance());
    }


    // 예치금 결제 처리
    @Transactional
    public void handleDepositPayRequest(PayRequestEventDto event) {
        log.info("💳 결제 요청 수신 - orderId: {}, payAmount: {}", event.getOrderId(), event.getTotalPrice());

        Payment pay = Payment.of(event, PaymentStatus.ORDERED);

        try {
            // ✅ 1. 주문 검증
            ValidateDto validate = orderService.getOrderByOrderId(event.getOrderId());
            if (validate == null) throw new IllegalStateException("Order 서버 검증 실패 (null 응답)");
            if (validate.getTotalPrice() != event.getTotalPrice())
                throw new IllegalArgumentException("결제 금액 불일치");
            if (validate.getOrderStatus() != OrderStatus.PENDING_APPROVAL)
                throw new IllegalStateException("결제 불가 상태: " + validate.getOrderStatus());

            // ✅ 2. 잔액 확인
            Balance balance = balanceRepository.findBalanceByUserIdWithLock(event.getMemberId());
            if (balance == null) throw new IllegalStateException("잔액 정보 없음");
            if (balance.getBalance() < event.getTotalPrice())
                throw new IllegalStateException("잔액 부족");

            // ✅ 3. 차감 및 결제 완료
            pay.setStatus(PaymentStatus.APPROVAL_PENDING);
            balance.setBalance(balance.getBalance() - event.getTotalPrice());
            balanceRepository.save(balance);

            pay.setStatus(PaymentStatus.APPROVED);
            paymentRepository.save(pay);

            log.info("✅ 결제 성공 - userId: {}, 차감 금액: {}, 잔여 잔액: {}",
                    event.getMemberId(), event.getTotalPrice(), balance.getBalance());

            // ✅ 4. 성공 이벤트 발행
            sendResponseEvent(event, "SUCCESS", null);

        } catch (IllegalArgumentException e) {
            log.error("❌ 결제 검증 실패 - orderId={}, reason={}", event.getOrderId(), e.getMessage());
            fail(pay, event, PaymentStatus.VALIDATE_FAILED, e.getMessage());

        } catch (IllegalStateException e) {
            log.error("❌ 결제 실패 - orderId={}, reason={}", event.getOrderId(), e.getMessage());
            PaymentStatus status =
                    e.getMessage().contains("잔액") ? PaymentStatus.NOT_ENOUGH :
                            e.getMessage().contains("상태") ? PaymentStatus.STATUS_ERROR :
                                    PaymentStatus.FAILED;
            fail(pay, event, status, e.getMessage());

        } catch (Exception e) {
            log.error("💥 시스템 오류 - orderId={}, ex={}", event.getOrderId(), e.toString(), e);
            fail(pay, event, PaymentStatus.FAILED, "INTERNAL_ERROR");
        }
    }

    // 실패 처리
    private void fail(Payment pay, PayRequestEventDto req, PaymentStatus status, String reason) {
        pay.setStatus(status);
        paymentRepository.save(pay);

        sendResponseEvent(req, "FAILED", reason);
    }

    // Kafka 응답 이벤트 발행
    private void sendResponseEvent(PayRequestEventDto req, String result, String reason) {
        PayResponseEvent response = PayResponseEvent.builder()
                .orderId(req.getOrderId())
                .orderNumber(req.getOrderNumber())
                .approvalAttemptId("PAY-" + System.currentTimeMillis())
                .build();

        if ("SUCCESS".equals(result)) {
            kafkaProducerService.sendPaySuccess(response);
        } else {
            kafkaProducerService.sendPayFailed(response);
        }
    }
}
