package com.tokenmall.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventoryAdjustRequest(
        @NotNull @PositiveOrZero Integer totalStock,
        @NotNull @PositiveOrZero Integer availableStock,
        @NotNull @PositiveOrZero Integer lockedStock
) {
}
