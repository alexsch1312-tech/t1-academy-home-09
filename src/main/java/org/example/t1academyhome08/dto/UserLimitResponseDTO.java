package org.example.t1academyhome08.dto;

import java.math.BigDecimal;

public record UserLimitResponseDTO(
        Long userId,
        BigDecimal actualLimit,
        BigDecimal reservedLimit,
        BigDecimal totalLimit
) {}
