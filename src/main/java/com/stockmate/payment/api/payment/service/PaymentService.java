package com.stockmate.payment.api.payment.service;

import com.stockmate.payment.api.payment.dto.PayRequestEventDto;
import com.stockmate.payment.api.payment.dto.ValidateDto;
import com.stockmate.payment.api.payment.entity.Balance;
import com.stockmate.payment.api.payment.entity.OrderStatus;
import com.stockmate.payment.api.payment.entity.Payment;
import com.stockmate.payment.api.payment.entity.PaymentStatus;
import com.stockmate.payment.api.payment.repository.BalanceRepository;
import com.stockmate.payment.api.payment.repository.PaymentRepository;
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

    // 예치금 충전
    @Transactional
    public void depositCharge(Long userId, Long amount) {
        log.info("💰 예치금 충전 요청 - userId: {}, 충전 금액: {}", userId, amount);

        Balance balance = balanceRepository.findBalanceByUserIdWithLock(userId);

        if (balance == null) {
            balance = new Balance();
            balance.setUserId(userId);
            balance.setBalance(0L);
        }

        long newBalance = balance.getBalance() + amount;
        balance.setBalance(newBalance);
        balanceRepository.save(balance);

        log.info("✅ 예치금 충전 완료 - userId: {}, 충전 금액: {}, 최종 잔액: {}", userId, amount, newBalance);
    }

    // 결제 요청
    @Transactional
    public void handleDepositPayRequest(PayRequestEventDto event) {
        log.info("💳 결제 요청 수신 - orderId: {}, payAmount: {}",
                event.getOrderId(), event.getTotalPrice());

        Payment pay = Payment.of(event, PaymentStatus.ORDERED);

        ValidateDto validateData = orderService.getOrderByOrderId(event.getOrderId());

        if (validateData == null) {
            log.error("❌ 주문 검증 실패 - Order 서버로부터 응답이 없습니다. orderId={}", event.getOrderId());
            pay.setStatus(PaymentStatus.VALIDATE_FAILED);
            paymentRepository.save(pay);
            throw new IllegalStateException("주문 검증 실패: Order 데이터 없음");
        }

        if (validateData.getTotalPrice() != event.getTotalPrice()) {
            log.error("❌ 결제 금액 불일치 - 주문 금액: {}, 요청 금액: {}", validateData.getTotalPrice(), event.getTotalPrice());
            pay.setStatus(PaymentStatus.VALIDATE_FAILED);
            paymentRepository.save(pay);
            throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        if (validateData.getOrderStatus() != OrderStatus.PENDING_APPROVAL) {
            log.error("❌ 잘못된 주문 상태 - 현재 상태: {}", validateData.getOrderStatus());
            pay.setStatus(PaymentStatus.STATUS_ERROR);
            paymentRepository.save(pay);
            throw new IllegalStateException("결제할 수 없는 주문 상태입니다.");
        }

        log.info("✅ 결제 검증 완료 - 주문 ID: {}, 결제 진행 시작", event.getOrderId());

        Balance balance = balanceRepository.findBalanceByUserIdWithLock(event.getUserId()); //  비관적 lock

        if (balance == null) {
            log.error("❌ 잔액 정보 없음 - userId: {}", event.getUserId());
            pay.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(pay);
            throw new IllegalStateException("잔액 정보가 존재하지 않습니다.");
        }

        if (balance.getBalance() < event.getTotalPrice()) {
            log.error("❌ 잔액 부족 - userId: {}, 잔액: {}, 결제 요청 금액: {}", event.getUserId(), balance.getBalance(), event.getTotalPrice());
            pay.setStatus(PaymentStatus.NOT_ENOUGH);
            paymentRepository.save(pay);
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        pay.setStatus(PaymentStatus.APPROVAL_PENDING);

        // 결제 금액만큼 잔액 차감
        balance.setBalance(balance.getBalance() - event.getTotalPrice());
        balanceRepository.save(balance);

        // 결제 정보 저장
        pay.setStatus(PaymentStatus.APPROVED);
        paymentRepository.save(pay);

        log.info("✅ 결제 성공 - userId: {}, 차감 금액: {}, 잔여 잔액: {}", event.getUserId(), event.getTotalPrice(), balance.getBalance());

        // TODO: 결제 성공 이벤트를 Kafka로 발행
    }

    // TODO: 카드결제(토스)
    public void handleCardPayRequest(PayRequestEventDto event) {
        return;
    }
}
