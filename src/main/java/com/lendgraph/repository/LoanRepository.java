package com.lendgraph.repository;

import com.lendgraph.model.Loan;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends Neo4jRepository<Loan, Long> {

    List<Loan> findByLenderId(Long lenderId);

    List<Loan> findByBorrowerId(Long borrowerId);

    List<Loan> findByStatus(String status);

    List<Loan> findByLenderIdAndStatus(Long lenderId, String status);

    List<Loan> findByBorrowerIdAndStatus(Long borrowerId, String status);

    // Find overdue loans (past due date and still ACTIVE)
    @Query("MATCH (loan:Loan) " +
           "WHERE loan.status = 'ACTIVE' AND loan.dueDate < date() " +
           "RETURN loan")
    List<Loan> findOverdueLoans();

    // Find overdue loans for a specific borrower
    @Query("MATCH (loan:Loan) " +
           "WHERE loan.borrowerId = $borrowerId AND loan.status = 'ACTIVE' AND loan.dueDate < date() " +
           "RETURN loan")
    List<Loan> findOverdueLoansByBorrower(@Param("borrowerId") Long borrowerId);

    // Count active loans for borrower (credit limit check)
    @Query("MATCH (loan:Loan) " +
           "WHERE loan.borrowerId = $borrowerId AND loan.status = 'ACTIVE' " +
           "RETURN COUNT(loan)")
    Long countActiveLoansForBorrower(@Param("borrowerId") Long borrowerId);

    // Total outstanding amount for borrower
    @Query("MATCH (loan:Loan) " +
           "WHERE loan.borrowerId = $borrowerId AND loan.status = 'ACTIVE' " +
           "RETURN COALESCE(SUM(loan.amount - loan.totalRepaid), 0.0)")
    Double getTotalOutstandingForBorrower(@Param("borrowerId") Long borrowerId);

    // Loans between two specific users
    @Query("MATCH (loan:Loan) " +
           "WHERE loan.lenderId = $lenderId AND loan.borrowerId = $borrowerId " +
           "RETURN loan ORDER BY loan.createdAt DESC")
    List<Loan> findLoansBetweenUsers(@Param("lenderId") Long lenderId,
                                      @Param("borrowerId") Long borrowerId);
}
