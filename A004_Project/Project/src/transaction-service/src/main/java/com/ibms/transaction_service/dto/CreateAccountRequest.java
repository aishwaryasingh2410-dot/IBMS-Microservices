package com.ibms.transaction_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAccountRequest(
        @NotNull @Positive Long userId
) {
}
