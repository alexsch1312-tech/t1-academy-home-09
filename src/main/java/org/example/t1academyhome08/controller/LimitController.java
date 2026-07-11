package org.example.t1academyhome08.controller;

import org.example.t1academyhome08.dto.ApiResponseDTO;
import org.example.t1academyhome08.dto.OperationActionRequestDTO;
import org.example.t1academyhome08.dto.ReserveLimitRequestDTO;
import org.example.t1academyhome08.dto.UserLimitResponseDTO;
import org.example.t1academyhome08.service.LimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/limits")
@RequiredArgsConstructor
public class LimitController {

    private final LimitService limitService;

    @GetMapping("/user/{userId}")
    public UserLimitResponseDTO getUserLimit(@PathVariable Long userId) {
        return limitService.getUserLimit(userId);
    }

    @PostMapping("/reserve")
    public ApiResponseDTO reserve(@Valid @RequestBody ReserveLimitRequestDTO request) {
        limitService.reserveLimit(request.userId(), request.operationId(), request.amount());
        return new ApiResponseDTO("Лимит успешно зарезервирован", true);
    }

    @PostMapping("/confirm")
    public ApiResponseDTO confirm(@Valid @RequestBody OperationActionRequestDTO request) {
        limitService.confirmOperation(request.operationId());
        return new ApiResponseDTO("Операция подтверждена, лимит списан окончательно", true);
    }

    @PostMapping("/cancel")
    public ApiResponseDTO cancel(@Valid @RequestBody OperationActionRequestDTO request) {
        limitService.cancelOperation(request.operationId());
        return new ApiResponseDTO("Операция отменена, лимит восстановлен", true);
    }

    @DeleteMapping("/clear-all-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearTestData() {
        limitService.clearAllData();
    }
}
