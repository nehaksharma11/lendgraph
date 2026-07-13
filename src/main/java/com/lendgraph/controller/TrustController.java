package com.lendgraph.controller;

import com.lendgraph.config.JwtUtil;
import com.lendgraph.dto.ApiResponse;
import com.lendgraph.dto.TrustDto;
import com.lendgraph.service.TrustService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trust")
@RequiredArgsConstructor
@Tag(name = "Trust & Graph Analysis",
     description = "Graph-powered trust scores and fraud detection using Neo4j")
public class TrustController {

    private final TrustService trustService;
    private final JwtUtil jwtUtil;

    @GetMapping("/score/me")
    @Operation(summary = "Get my trust score",
               description = "Returns trust score, risk level, badges, and repayment statistics")
    public ResponseEntity<ApiResponse<TrustDto.TrustScoreResponse>> getMyTrustScore(
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(trustService.getTrustScore(userId)));
    }

    @GetMapping("/score/{userId}")
    @Operation(summary = "Get trust score for any user",
               description = "Check another user's trust score before lending to them")
    public ResponseEntity<ApiResponse<TrustDto.TrustScoreResponse>> getTrustScore(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(trustService.getTrustScore(userId)));
    }

    @GetMapping("/network")
    @Operation(summary = "Get my lending network",
               description = "Returns all users connected to you through lending relationships (2 hops). " +
                             "Powered by Neo4j graph traversal.")
    public ResponseEntity<ApiResponse<TrustDto.NetworkResponse>> getMyNetwork(
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.success(trustService.getLendingNetwork(userId)));
    }

    @GetMapping("/network/{userId}")
    @Operation(summary = "Get lending network for a specific user")
    public ResponseEntity<ApiResponse<TrustDto.NetworkResponse>> getNetwork(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(trustService.getLendingNetwork(userId)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
