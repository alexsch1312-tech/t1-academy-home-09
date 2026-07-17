package org.example.t1academyhome08.dto;

import java.math.BigDecimal;

public record LimitEvent(
        Long userId,
        String operationId,
        BigDecimal amount,
        String status, // RESERVED, CONFIRMED, CANCELLED
        String message
) {}
