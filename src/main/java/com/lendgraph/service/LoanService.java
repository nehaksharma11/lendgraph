package com.lendgraph.service;

import com.lendgraph.dto.LoanDto;
import com.lendgraph.exception.BadRequestException;
import com.lendgraph.exception.ResourceNotFoundException;
import com.lendgraph.exception.UnauthorizedException;
import com.lendgraph.model.Loan;
import com.lendgraph.model.User;
import com.lendgraph.repository.LoanRepository;
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
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public LoanDto.Response createLoan(Long lenderId, LoanDto.CreateRequest request) {
        if (lenderId.equals(request.getBorrowerId())) {
            throw new BadRequestException("You cannot lend to yourself");
        }

        User lender = userRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lender not found"));
        User borrower = userRepository.findById(request.getBorrowerId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));

        // Check borrower's existing debt
        Long activeLoans = loanRepository.countActiveLoansForBorrower(request.getBorrowerId());
        if (activeLoans >= 5) {
            throw new BadRequestException("Borrower already has 5 active loans");
        }

        Double outstanding = loanRepository.getTotalOutstandingForBorrower(request.getBorrowerId());
        if (outstanding + request.getAmount() > 500000) {
            throw new BadRequestException("Borrower would exceed total outstanding limit of ₹5,00,000");
        }

        Loan loan = Loan.builder()
                .amount(request.getAmount())
                .interestRate(request.getInterestRate())
                .dueDate(request.getDueDate())
                .purpose(request.getPurpose())
                .status("PENDING")
                .lenderId(lenderId)
                .borrowerId(request.getBorrowerId())
                .lender(lender)
                .borrower(borrower)
                .totalRepaid(0.0)
                .build();

        loan = loanRepository.save(loan);
        log.info("Loan created: {} from lender {} to borrower {}", loan.getId(), lenderId, request.getBorrowerId());

        return mapToResponse(loan, lender, borrower);
    }

    public LoanDto.Response acceptLoan(Long loanId, Long borrowerId) {
        Loan loan = getLoanOrThrow(loanId);

        if (!loan.getBorrowerId().equals(borrowerId)) {
            throw new UnauthorizedException("Only the borrower can accept this loan");
        }
        if (!"PENDING".equals(loan.getStatus())) {
            throw new BadRequestException("Loan is not in PENDING state");
        }

        loan.setStatus("ACTIVE");
        loan = loanRepository.save(loan);
        log.info("Loan {} accepted by borrower {}", loanId, borrowerId);
        return mapToResponse(loan);
    }

    public LoanDto.Response rejectLoan(Long loanId, Long borrowerId) {
        Loan loan = getLoanOrThrow(loanId);

        if (!loan.getBorrowerId().equals(borrowerId)) {
            throw new UnauthorizedException("Only the borrower can reject this loan");
        }
        if (!"PENDING".equals(loan.getStatus())) {
            throw new BadRequestException("Loan is not in PENDING state");
        }

        loan.setStatus("CANCELLED");
        loan = loanRepository.save(loan);
        return mapToResponse(loan);
    }

    public LoanDto.Response getLoan(Long loanId) {
        return mapToResponse(getLoanOrThrow(loanId));
    }

    public List<LoanDto.Response> getLoansByLender(Long lenderId) {
        return loanRepository.findByLenderId(lenderId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<LoanDto.Response> getLoansByBorrower(Long borrowerId) {
        return loanRepository.findByBorrowerId(borrowerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<LoanDto.Response> getMyLoans(Long userId) {
        List<Loan> asLender = loanRepository.findByLenderId(userId);
        List<Loan> asBorrower = loanRepository.findByBorrowerId(userId);
        asLender.addAll(asBorrower);
        return asLender.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<LoanDto.Response> getOverdueLoans() {
        return loanRepository.findOverdueLoans()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public Loan getLoanOrThrow(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
    }

    private LoanDto.Response mapToResponse(Loan loan) {
        User lender = loan.getLender();
        User borrower = loan.getBorrower();
        return mapToResponse(loan, lender, borrower);
    }

    private LoanDto.Response mapToResponse(Loan loan, User lender, User borrower) {
        return LoanDto.Response.builder()
                .id(loan.getId())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .dueDate(loan.getDueDate())
                .purpose(loan.getPurpose())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .totalRepaid(loan.getTotalRepaid())
                .remainingAmount(loan.getRemainingAmount())
                .lenderId(loan.getLenderId())
                .lenderName(lender != null ? lender.getFullName() : null)
                .borrowerId(loan.getBorrowerId())
                .borrowerName(borrower != null ? borrower.getFullName() : null)
                .isOverdue(loan.getDueDate() != null &&
                        "ACTIVE".equals(loan.getStatus()) &&
                        loan.getDueDate().isBefore(LocalDate.now()))
                .build();
    }
}
