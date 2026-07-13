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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("User")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Property("email")
    private String email;

    @Property("passwordHash")
    private String passwordHash;

    @Property("fullName")
    private String fullName;

    @Property("phone")
    private String phone;

    @Property("trustScore")
    @Builder.Default
    private Double trustScore = 100.0;

    @Property("kycVerified")
    @Builder.Default
    private Boolean kycVerified = false;

    @Property("role")
    @Builder.Default
    private String role = "USER";

    @Property("createdAt")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Property("isActive")
    @Builder.Default
    private Boolean isActive = true;

    // Graph relationships
    @Relationship(type = "MEMBER_OF", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<Circle> circles = new ArrayList<>();
}
