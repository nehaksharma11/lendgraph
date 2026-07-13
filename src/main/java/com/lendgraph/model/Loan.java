package com.lendgraph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("Loan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue
    private Long id;

    @Property("amount")
    private Double amount;

    @Property("interestRate")
    private Double interestRate;

    @Property("dueDate")
    private LocalDate dueDate;

    @Property("purpose")
    private String purpose;

    @Property("status")
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACTIVE, REPAID, DEFAULTED, CANCELLED

    @Property("createdAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Property("totalRepaid")
    @Builder.Default
    private Double totalRepaid = 0.0;

    // Relationships - stored as properties (lender/borrower IDs for query convenience)
    @Property("lenderId")
    private Long lenderId;

    @Property("borrowerId")
    private Long borrowerId;

    // Graph relationships
    @Relationship(type = "LENT_TO", direction = Relationship.Direction.INCOMING)
    private User lender;

    @Relationship(type = "BORROWED_BY", direction = Relationship.Direction.OUTGOING)
    private User borrower;

    @Relationship(type = "REPAID", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<Repayment> repayments = new ArrayList<>();

    public Double getRemainingAmount() {
        return amount - totalRepaid;
    }

    public boolean isFullyRepaid() {
        return totalRepaid >= amount;
    }
}
