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

@Node("TrustEvent")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustEvent {

    @Id
    @GeneratedValue
    private Long id;

    // EARLY_REPAYMENT, ON_TIME_REPAYMENT, LATE_PAYMENT, DEFAULT, PARTIAL_REPAYMENT
    @Property("type")
    private String type;

    @Property("scoreImpact")
    private Double scoreImpact;

    @Property("userId")
    private Long userId;

    @Property("loanId")
    private Long loanId;

    @Property("occurredAt")
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Property("description")
    private String description;
}
