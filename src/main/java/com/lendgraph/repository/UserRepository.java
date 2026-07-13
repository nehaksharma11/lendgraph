package com.lendgraph.repository;

import com.lendgraph.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // Graph query: find all users connected through lending relationships within 2 hops
    @Query("MATCH (u:User {id: $userId})-[:LENT_TO|BORROWED_BY*1..2]-(connected:User) " +
           "WHERE connected.id <> $userId " +
           "RETURN DISTINCT connected")
    List<User> findLendingNetwork(@Param("userId") Long userId);

    // Graph query: find mutual connections between two users (trust signal)
    @Query("MATCH (a:User {id: $userId1})-[:LENT_TO|BORROWED_BY*1..3]-(mutual:User)-" +
           "[:LENT_TO|BORROWED_BY*1..3]-(b:User {id: $userId2}) " +
           "WHERE mutual.id <> $userId1 AND mutual.id <> $userId2 " +
           "RETURN DISTINCT mutual")
    List<User> findMutualConnections(@Param("userId1") Long userId1,
                                     @Param("userId2") Long userId2);

    // Graph query: detect if borrower has defaulted on users in lender's network
    @Query("MATCH (borrower:User {id: $borrowerId})-[:BORROWED_BY]-(loan:Loan {status: 'DEFAULTED'})" +
           "-[:LENT_TO]-(networkUser:User)-[:LENT_TO|BORROWED_BY*1..2]-(lender:User {id: $lenderId}) " +
           "RETURN COUNT(loan) as defaultCount")
    Long countDefaultsInLenderNetwork(@Param("borrowerId") Long borrowerId,
                                      @Param("lenderId") Long lenderId);

    @Query("MATCH (u:User) WHERE u.trustScore < $threshold RETURN u")
    List<User> findHighRiskUsers(@Param("threshold") Double threshold);
}
