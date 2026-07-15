package com.lendgraph.service;

import com.lendgraph.dto.TrustDto;
import com.lendgraph.exception.ResourceNotFoundException;
import com.lendgraph.model.User;
import com.lendgraph.repository.LoanRepository;
import com.lendgraph.repository.TrustEventRepository;
import com.lendgraph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrustService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final TrustEventRepository trustEventRepository;

    public TrustDto.TrustScoreResponse getTrustScore(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long onTimeRepayments = trustEventRepository.countEventsByType(userId, "ON_TIME_REPAYMENT")
                + trustEventRepository.countEventsByType(userId, "EARLY_REPAYMENT");
        Long latePayments = trustEventRepository.countEventsByType(userId, "LATE_PAYMENT");
        Long defaults = trustEventRepository.countEventsByType(userId, "DEFAULT");

        Long totalLoans = loanRepository.findByBorrowerId(userId).stream()
                .filter(l -> !l.getStatus().equals("PENDING") && !l.getStatus().equals("CANCELLED"))
                .count();

        List<User> network = userRepository.findLendingNetwork(userId);

        List<String> badges = computeBadges(user, onTimeRepayments, latePayments, defaults, totalLoans);

        return TrustDto.TrustScoreResponse.builder()
                .userId(userId)
                .fullName(user.getFullName())
                .trustScore(user.getTrustScore())
                .riskLevel(computeRiskLevel(user.getTrustScore()))
                .totalLoans(totalLoans)
                .onTimeRepayments(onTimeRepayments)
                .latePayments(latePayments)
                .defaults(defaults)
                .NetworkSize(Long.valueOf(network.size()));
                .badges(badges)
                .build();
    }

    /**
     * The hackathon wow factor: graph-powered fraud detection.
     * Traverses Neo4j relationships to detect shared defaults in lending network.
     */
    public TrustDto.FraudRiskResponse getFraudRisk(Long loanId, Long borrowerId, Long lenderId) {
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));

        // Graph query: how many defaults has this borrower caused to users in lender's network?
        Long defaultsInNetwork = userRepository.countDefaultsInLenderNetwork(borrowerId, lenderId);

        // Graph query: mutual connections (higher = more trust)
        List<User> mutualConnections = userRepository.findMutualConnections(borrowerId, lenderId);

        Long overdueCount = loanRepository.findOverdueLoansByBorrower(borrowerId).size();
        Long activeLoans = loanRepository.countActiveLoansForBorrower(borrowerId);

        List<String> riskFlags = new ArrayList<>();
        double riskScore = 0.0;

        if (defaultsInNetwork > 0) {
            riskFlags.add("Borrower has " + defaultsInNetwork + " default(s) within your lending network");
            riskScore += defaultsInNetwork * 25.0;
        }
        if (overdueCount > 0) {
            riskFlags.add("Borrower has " + overdueCount + " currently overdue loan(s)");
            riskScore += overdueCount * 20.0;
        }
        if (activeLoans > 3) {
            riskFlags.add("Borrower has " + activeLoans + " active loans");
            riskScore += (activeLoans - 3) * 10.0;
        }
        if (borrower.getTrustScore() < 60) {
            riskFlags.add("Low trust score: " + String.format("%.1f", borrower.getTrustScore()));
            riskScore += (60 - borrower.getTrustScore());
        }

        // Mutual connections REDUCE risk
        riskScore = Math.max(0, riskScore - (mutualConnections.size() * 5.0));
        riskScore = Math.min(100, riskScore);

        String riskLevel;
        String recommendation;
        if (riskScore < 20) {
            riskLevel = "LOW";
            recommendation = "PROCEED";
        } else if (riskScore < 50) {
            riskLevel = "MEDIUM";
            recommendation = "CAUTION";
        } else if (riskScore < 75) {
            riskLevel = "HIGH";
            recommendation = "AVOID";
        } else {
            riskLevel = "CRITICAL";
            recommendation = "AVOID";
        }

        if (riskFlags.isEmpty()) {
            riskFlags.add("No risk signals detected");
        }

        log.info("Fraud risk for borrower {}: {} (score: {})", borrowerId, riskLevel, riskScore);

        return TrustDto.FraudRiskResponse.builder()
                .loanId(loanId)
                .borrowerId(borrowerId)
                .lenderId(lenderId)
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .defaultsInNetwork(defaultsInNetwork)
                .MutualConnections(Long.valueOf(mutualConnections.size()));
                .riskFlags(riskFlags)
                .recommendation(recommendation)
                .build();
    }

    public TrustDto.NetworkResponse getLendingNetwork(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<User> network = userRepository.findLendingNetwork(userId);

        List<TrustDto.NetworkMember> members = network.stream()
                .map(u -> TrustDto.NetworkMember.builder()
                        .userId(u.getId())
                        .fullName(u.getFullName())
                        .trustScore(u.getTrustScore())
                        .riskLevel(computeRiskLevel(u.getTrustScore()))
                        .build())
                .toList();

        return TrustDto.NetworkResponse.builder()
                .userId(userId)
                .networkSize((long) network.size())
                .members(members)
                .build();
    }

    private String computeRiskLevel(Double trustScore) {
        if (trustScore >= 80) return "LOW";
        if (trustScore >= 60) return "MEDIUM";
        if (trustScore >= 40) return "HIGH";
        return "CRITICAL";
    }

    private List<String> computeBadges(User user, Long onTime, Long late, Long defaults, Long totalLoans) {
        List<String> badges = new ArrayList<>();
        if (totalLoans >= 5 && defaults == 0) badges.add("RELIABLE_BORROWER");
        if (onTime >= 10) badges.add("CONSISTENT_PAYER");
        if (late == 0 && totalLoans > 0) badges.add("ZERO_LATE_PAYMENTS");
        if (user.getTrustScore() >= 90) badges.add("ELITE_MEMBER");
        if (user.getKycVerified()) badges.add("KYC_VERIFIED");
        return badges;
    }
}
