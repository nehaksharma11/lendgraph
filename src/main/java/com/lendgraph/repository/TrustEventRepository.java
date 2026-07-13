package com.lendgraph.repository;

import com.lendgraph.model.TrustEvent;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustEventRepository extends Neo4jRepository<TrustEvent, Long> {

    List<TrustEvent> findByUserIdOrderByOccurredAtDesc(Long userId);

    List<TrustEvent> findByLoanId(Long loanId);

    @Query("MATCH (t:TrustEvent) WHERE t.userId = $userId " +
           "RETURN COALESCE(SUM(t.scoreImpact), 0.0)")
    Double getTotalScoreImpactForUser(@Param("userId") Long userId);

    @Query("MATCH (t:TrustEvent) WHERE t.userId = $userId AND t.type = $type " +
           "RETURN COUNT(t)")
    Long countEventsByType(@Param("userId") Long userId, @Param("type") String type);
}
