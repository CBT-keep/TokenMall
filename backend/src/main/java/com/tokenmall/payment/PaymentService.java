package com.tokenmall.payment;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.order.OrderService;
import com.tokenmall.order.entity.MallOrder;
import com.tokenmall.payment.dto.MockPaymentRequest;
import com.tokenmall.payment.dto.PaymentResponse;
import com.tokenmall.payment.entity.PaymentRecord;
import com.tokenmall.payment.mapper.PaymentRecordMapper;
import com.tokenmall.token.TokenGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderService orderService;
    private final TokenGrantService tokenGrantService;

    /**
     * LEARNING-BASELINE MYSQL-04 and MQ-01:
     * The baseline grants Token synchronously in the payment transaction.
     * There is no distributed transaction with RabbitMQ yet.
     */
    @Transactional
    public PaymentResponse mockSuccess(Long userId, MockPaymentRequest request) {
        MallOrder order = orderService.getByOrderNo(request.orderNo());
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }

        PaymentRecord existing = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, order.getOrderNo())
        );
        if (existing != null && "SUCCESS".equals(existing.getStatus())) {
            if ("PAID".equals(order.getStatus())) {
                orderService.markCompleted(order);
            }
            return PaymentResponse.from(existing);
        }

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }

        PaymentRecord payment = new PaymentRecord();
        payment.setPaymentNo(nextPaymentNo());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(order.getPayAmount());
        payment.setChannel("MOCK");
        payment.setStatus("SUCCESS");
        payment.setRequestId(request.requestId());
        payment.setPaidAt(LocalDateTime.now());
        paymentRecordMapper.insert(payment);

        orderService.markPaid(order);
        tokenGrantService.grantForOrder(order, orderService.items(order.getOrderNo()));
        orderService.markCompleted(order);
        return PaymentResponse.from(payment);
    }

    public PaymentResponse get(String orderNo, Long userId) {
        PaymentRecord payment = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo)
        );
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }
        return PaymentResponse.from(payment);
    }

    private String nextPaymentNo() {
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
    }
}
