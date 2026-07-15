package org.example.t1academyhome08.dto;

import jakarta.validation.constraints.NotBlank;

public record OperationActionRequestDTO(
        @NotBlank(message = "ID операции не может быть пустым")
        String operationId
) {}
