package com.lendgraph.controller;

import com.lendgraph.config.JwtUtil;
import com.lendgraph.dto.ApiResponse;
import com.lendgraph.dto.LoanDto;
import com.lendgraph.service.LoanService;
import com.lendgraph.service.TrustService;
import com.lendgraph.dto.TrustDto;
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
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Create and manage peer-to-peer loans")
public class LoanController {

    private final LoanService loanService;
    private final TrustService trustService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "Create a loan offer",
               description = "Lender creates a loan offer for a borrower. Borrower must accept it.")
    public ResponseEntity<ApiResponse<LoanDto.Response>> createLoan(
            @Valid @RequestBody LoanDto.CreateRequest request,
            HttpServletRequest httpRequest) {
        Long lenderId = extractUserId(httpRequest);
        LoanDto.Response loan = loanService.createLoan(lenderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan offer created", loan));
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Get loan details")
    public ResponseEntity<ApiResponse<LoanDto.Response>> getLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoan(loanId)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all loans for the logged-in user (as lender or borrower)")
    public ResponseEntity<ApiResponse<List<LoanDto.Response>>> getMyLoans(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(loanService.getMyLoans(userId)));
    }

    @GetMapping("/as-lender")
    @Operation(summary = "Get all loans where I am the lender")
    public ResponseEntity<ApiResponse<List<LoanDto.Response>>> getLoansByLender(
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoansByLender(userId)));
    }

    @GetMapping("/as-borrower")
    @Operation(summary = "Get all loans where I am the borrower")
    public ResponseEntity<ApiResponse<List<LoanDto.Response>>> getLoansByBorrower(
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoansByBorrower(userId)));
    }

    @PatchMapping("/{loanId}/accept")
    @Operation(summary = "Accept a loan offer",
               description = "Borrower accepts a PENDING loan. Status changes to ACTIVE.")
    public ResponseEntity<ApiResponse<LoanDto.Response>> acceptLoan(
            @PathVariable Long loanId,
            HttpServletRequest request) {
        Long borrowerId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Loan accepted", loanService.acceptLoan(loanId, borrowerId)));
    }

    @PatchMapping("/{loanId}/reject")
    @Operation(summary = "Reject a loan offer",
               description = "Borrower rejects a PENDING loan. Status changes to CANCELLED.")
    public ResponseEntity<ApiResponse<LoanDto.Response>> rejectLoan(
            @PathVariable Long loanId,
            HttpServletRequest request) {
        Long borrowerId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Loan rejected", loanService.rejectLoan(loanId, borrowerId)));
    }

    @GetMapping("/{loanId}/fraud-risk")
    @Operation(summary = "Get fraud risk analysis for a loan",
               description = "Graph-powered fraud detection using Neo4j relationship traversal. " +
                             "Detects if borrower has defaulted on users in lender's network.")
    public ResponseEntity<ApiResponse<TrustDto.FraudRiskResponse>> getFraudRisk(
            @PathVariable Long loanId,
            HttpServletRequest request) {
        Long lenderId = extractUserId(request);
        var loan = loanService.getLoanOrThrow(loanId);
        TrustDto.FraudRiskResponse risk = trustService.getFraudRisk(loanId, loan.getBorrowerId(), lenderId);
        return ResponseEntity.ok(ApiResponse.success(risk));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue loans (admin view)")
    public ResponseEntity<ApiResponse<List<LoanDto.Response>>> getOverdueLoans() {
        return ResponseEntity.ok(ApiResponse.success(loanService.getOverdueLoans()));
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
