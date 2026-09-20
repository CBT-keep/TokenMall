package com.tokenmall.token.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TokenConsumeRequest(
        @NotNull @Min(1) Long amount,
        @NotBlank String requestId,
        String description
) {
}
