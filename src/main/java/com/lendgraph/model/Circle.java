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

@Node("Circle")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Circle {

    @Id
    @GeneratedValue
    private Long id;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("createdBy")
    private Long createdBy;

    @Property("createdAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Property("maxLoanAmount")
    @Builder.Default
    private Double maxLoanAmount = 10000.0;

    @Property("isActive")
    @Builder.Default
    private Boolean isActive = true;
}
