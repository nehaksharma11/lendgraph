package com.lendgraph.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class TrustDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrustScoreResponse {
        private Long userId;
        private String fullName;
        private Double trustScore;
        private String riskLevel;        // LOW, MEDIUM, HIGH
        private Long totalLoans;
        private Long onTimeRepayments;
        private Long latePayments;
        private Long defaults;
        private Long networkSize;        // How many users in lending network
        private List<String> badges;     // RELIABLE_BORROWER, ACTIVE_LENDER, etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FraudRiskResponse {
        private Long loanId;
        private Long borrowerId;
        private Long lenderId;
        private String riskLevel;        // LOW, MEDIUM, HIGH, CRITICAL
        private Double riskScore;        // 0 to 100
        private Long defaultsInNetwork;  // defaults within lender's 2-hop network
        private Long mutualConnections;  // shared contacts = trust signal
        private List<String> riskFlags;  // reasons for risk rating
        private String recommendation;   // PROCEED, CAUTION, AVOID
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkResponse {
        private Long userId;
        private Long networkSize;
        private List<NetworkMember> members;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkMember {
        private Long userId;
        private String fullName;
        private Double trustScore;
        private String riskLevel;
    }
}
