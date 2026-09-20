package com.tokenmall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderFromCartRequest(
        @NotEmpty List<Long> cartItemIds,
        @NotBlank String requestId
) {
}
