package com.tokenmall.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequest(
        @NotNull @Min(1) Integer quantity,
        Boolean selected
) {
}
