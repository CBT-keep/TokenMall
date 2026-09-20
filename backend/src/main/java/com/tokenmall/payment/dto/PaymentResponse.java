package com.tokenmall.payment.dto;

import com.tokenmall.payment.entity.PaymentRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String paymentNo,
        String orderNo,
        BigDecimal amount,
        String status,
        LocalDateTime paidAt
) {

    public static PaymentResponse from(PaymentRecord payment) {
        return new PaymentResponse(
                payment.getPaymentNo(),
                payment.getOrderNo(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}
