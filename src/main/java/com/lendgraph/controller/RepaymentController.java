package com.lendgraph.controller;

import com.lendgraph.config.JwtUtil;
import com.lendgraph.dto.ApiResponse;
import com.lendgraph.dto.RepaymentDto;
import com.lendgraph.service.RepaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Repayments", description = "Record and view loan repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;
    private final JwtUtil jwtUtil;

    @PostMapping("/loans/{loanId}/repay")
    @Operation(summary = "Make a repayment on a loan",
               description = "Borrower makes a repayment. Trust score is automatically updated based on timing.")
    public ResponseEntity<ApiResponse<RepaymentDto.Response>> makeRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody RepaymentDto.CreateRequest request,
            HttpServletRequest httpRequest) {
        Long borrowerId = extractUserId(httpRequest);
        RepaymentDto.Response repayment = repaymentService.makeRepayment(loanId, borrowerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repayment recorded", repayment));
    }

    @GetMapping("/loans/{loanId}/repayments")
    @Operation(summary = "Get all repayments for a specific loan")
    public ResponseEntity<ApiResponse<List<RepaymentDto.Response>>> getRepaymentsByLoan(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(repaymentService.getRepaymentsByLoan(loanId)));
    }

    @GetMapping("/repayments/my")
    @Operation(summary = "Get all repayments made by me")
    public ResponseEntity<ApiResponse<List<RepaymentDto.Response>>> getMyRepayments(
            HttpServletRequest request) {
        Long borrowerId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(repaymentService.getRepaymentsByBorrower(borrowerId)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
