package com.tokenmall.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record MockPaymentRequest(
        @NotBlank String orderNo,
        @NotBlank String requestId
) {
}
