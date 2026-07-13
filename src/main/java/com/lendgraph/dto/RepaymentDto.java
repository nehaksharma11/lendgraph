package com.lendgraph.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class RepaymentDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private Double amount;

        private String note;

        private String paymentMethod;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Double amount;
        private LocalDateTime paidAt;
        private String note;
        private String paymentMethod;
        private Long loanId;
        private Long borrowerId;
        private Long lenderId;
    }
}
