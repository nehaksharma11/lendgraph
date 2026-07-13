package com.lendgraph.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Borrower ID is required")
        private Long borrowerId;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Max(value = 100000, message = "Max loan amount is ₹1,00,000")
        private Double amount;

        @NotNull(message = "Interest rate is required")
        @Min(value = 0, message = "Interest rate cannot be negative")
        @Max(value = 50, message = "Interest rate cannot exceed 50%")
        private Double interestRate;

        @NotNull(message = "Due date is required")
        @Future(message = "Due date must be in the future")
        private LocalDate dueDate;

        @NotBlank(message = "Purpose is required")
        @Size(max = 200, message = "Purpose cannot exceed 200 characters")
        private String purpose;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Double amount;
        private Double interestRate;
        private LocalDate dueDate;
        private String purpose;
        private String status;
        private LocalDateTime createdAt;
        private Double totalRepaid;
        private Double remainingAmount;
        private Long lenderId;
        private String lenderName;
        private Long borrowerId;
        private String borrowerName;
        private Boolean isOverdue;
    }
}
