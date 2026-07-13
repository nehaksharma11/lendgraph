package com.lendgraph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;

@Node("Repayment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repayment {

    @Id
    @GeneratedValue
    private Long id;

    @Property("amount")
    private Double amount;

    @Property("paidAt")
    @Builder.Default
    private LocalDateTime paidAt = LocalDateTime.now();

    @Property("note")
    private String note;

    @Property("loanId")
    private Long loanId;

    @Property("borrowerId")
    private Long borrowerId;

    @Property("lenderId")
    private Long lenderId;

    @Property("paymentMethod")
    @Builder.Default
    private String paymentMethod = "UPI";
}
