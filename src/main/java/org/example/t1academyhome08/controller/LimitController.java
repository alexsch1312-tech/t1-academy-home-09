package org.example.t1academyhome08.controller;

import org.example.t1academyhome08.dto.ApiResponse;
import org.example.t1academyhome08.dto.OperationActionRequest;
import org.example.t1academyhome08.dto.ReserveLimitRequest;
import org.example.t1academyhome08.dto.UserLimitResponse;
import org.example.t1academyhome08.service.LimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/limits")
@RequiredArgsConstructor
public class LimitController {

    private final LimitService limitService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserLimitResponse> getUserLimit(@PathVariable Long userId) {
        UserLimitResponse response = limitService.getUserLimit(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse> reserve(@Valid @RequestBody ReserveLimitRequest request) {

        limitService.reserveLimit(request.userId(), request.operationId(), request.amount());
        return ResponseEntity.ok(new ApiResponse("Limit reserved successfully", true));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse> confirm(@Valid @RequestBody OperationActionRequest request) {

        limitService.confirmOperation(request.operationId());
        return ResponseEntity.ok(new ApiResponse("Operation confirmed, limit deducted permanently", true));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse> cancel(@Valid @RequestBody OperationActionRequest request) {
        limitService.cancelOperation(request.operationId());
        return ResponseEntity.ok(new ApiResponse("Operation cancelled, limit restored", true));
    }
}
