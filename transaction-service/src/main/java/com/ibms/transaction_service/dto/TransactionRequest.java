package com.ibms.transaction_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull @Positive Long accountId,
        @Positive Long destinationAccountId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
