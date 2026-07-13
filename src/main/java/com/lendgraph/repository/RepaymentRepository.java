package com.lendgraph.repository;

import com.lendgraph.model.Repayment;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRepository extends Neo4jRepository<Repayment, Long> {

    List<Repayment> findByLoanId(Long loanId);

    List<Repayment> findByBorrowerId(Long borrowerId);

    @Query("MATCH (r:Repayment) WHERE r.loanId = $loanId " +
           "RETURN COALESCE(SUM(r.amount), 0.0)")
    Double getTotalRepaidForLoan(@Param("loanId") Long loanId);

    @Query("MATCH (r:Repayment) WHERE r.borrowerId = $borrowerId " +
           "RETURN COUNT(r)")
    Long countRepaymentsByBorrower(@Param("borrowerId") Long borrowerId);
}
