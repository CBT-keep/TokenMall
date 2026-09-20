package com.tokenmall.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        @NotBlank String code,
        Integer sortOrder,
        Integer status
) {
}
