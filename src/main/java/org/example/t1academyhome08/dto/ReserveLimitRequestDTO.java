package org.example.t1academyhome08.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReserveLimitRequestDTO(
        @NotNull(message = "ID пользователя обязателен")
        Long userId,

        @NotBlank(message = "ID операции не может быть пустым")
        String operationId,

        @NotNull(message = "Сумма обязательна")
        @Positive(message = "Сумма должна быть больше нуля")
        BigDecimal amount
) {}

