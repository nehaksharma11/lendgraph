package com.lendgraph.repository;

import com.lendgraph.model.Circle;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CircleRepository extends Neo4jRepository<Circle, Long> {

    List<Circle> findByCreatedBy(Long createdBy);

    List<Circle> findByIsActive(Boolean isActive);

    @Query("MATCH (u:User {id: $userId})-[:MEMBER_OF]->(c:Circle) RETURN c")
    List<Circle> findCirclesForUser(@Param("userId") Long userId);
}
