package com.lendgraph.service;

import com.lendgraph.dto.RepaymentDto;
import com.lendgraph.exception.BadRequestException;
import com.lendgraph.exception.UnauthorizedException;
import com.lendgraph.model.Loan;
import com.lendgraph.model.Repayment;
import com.lendgraph.model.TrustEvent;
import com.lendgraph.repository.LoanRepository;
import com.lendgraph.repository.RepaymentRepository;
import com.lendgraph.repository.TrustEventRepository;
import com.lendgraph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final TrustEventRepository trustEventRepository;

    public RepaymentDto.Response makeRepayment(Long loanId, Long borrowerId,
                                               RepaymentDto.CreateRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BadRequestException("Loan not found"));

        if (!loan.getBorrowerId().equals(borrowerId)) {
            throw new UnauthorizedException("Only the borrower can make repayments");
        }
        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new BadRequestException("Loan is not active");
        }
        if (request.getAmount() > loan.getRemainingAmount()) {
            throw new BadRequestException("Repayment amount exceeds remaining balance of ₹"
                    + loan.getRemainingAmount());
        }

        Repayment repayment = Repayment.builder()
                .amount(request.getAmount())
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI")
                .loanId(loanId)
                .borrowerId(borrowerId)
                .lenderId(loan.getLenderId())
                .build();

        repayment = repaymentRepository.save(repayment);

        // Update loan total repaid
        loan.setTotalRepaid(loan.getTotalRepaid() + request.getAmount());

        // Check if fully repaid
        if (loan.isFullyRepaid()) {
            loan.setStatus("REPAID");
            log.info("Loan {} fully repaid by borrower {}", loanId, borrowerId);
        }
        loanRepository.save(loan);

        // Update trust score based on repayment timing
        updateTrustScore(loan, borrowerId, request.getAmount());

        return mapToResponse(repayment);
    }

    private void updateTrustScore(Loan loan, Long borrowerId, Double amount) {
        var userOpt = userRepository.findById(borrowerId);
        if (userOpt.isEmpty()) return;

        var user = userOpt.get();
        String eventType;
        double scoreImpact;
        String description;

        boolean isEarly = loan.getDueDate().isAfter(LocalDate.now().plusDays(7));
        boolean isOnTime = !loan.getDueDate().isBefore(LocalDate.now());

        if (isEarly) {
            eventType = "EARLY_REPAYMENT";
            scoreImpact = 3.0;
            description = "Early repayment of ₹" + amount;
        } else if (isOnTime) {
            eventType = "ON_TIME_REPAYMENT";
            scoreImpact = 1.5;
            description = "On-time repayment of ₹" + amount;
        } else {
            eventType = "LATE_PAYMENT";
            scoreImpact = -5.0;
            description = "Late repayment of ₹" + amount;
        }

        TrustEvent event = TrustEvent.builder()
                .type(eventType)
                .scoreImpact(scoreImpact)
                .userId(borrowerId)
                .loanId(loan.getId())
                .description(description)
                .build();
        trustEventRepository.save(event);

        double newScore = Math.max(0, Math.min(1000, user.getTrustScore() + scoreImpact));
        user.setTrustScore(newScore);
        userRepository.save(user);

        log.info("Trust score updated for user {}: {} -> {} ({})",
                borrowerId, user.getTrustScore() - scoreImpact, newScore, eventType);
    }

    public List<RepaymentDto.Response> getRepaymentsByLoan(Long loanId) {
        return repaymentRepository.findByLoanId(loanId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<RepaymentDto.Response> getRepaymentsByBorrower(Long borrowerId) {
        return repaymentRepository.findByBorrowerId(borrowerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private RepaymentDto.Response mapToResponse(Repayment r) {
        return RepaymentDto.Response.builder()
                .id(r.getId())
                .amount(r.getAmount())
                .paidAt(r.getPaidAt())
                .note(r.getNote())
                .paymentMethod(r.getPaymentMethod())
                .loanId(r.getLoanId())
                .borrowerId(r.getBorrowerId())
                .lenderId(r.getLenderId())
                .build();
    }
}
