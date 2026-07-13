package com.lendgraph.controller;

import com.lendgraph.config.JwtUtil;
import com.lendgraph.dto.ApiResponse;
import com.lendgraph.exception.ResourceNotFoundException;
import com.lendgraph.model.User;
import com.lendgraph.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyProfile(HttpServletRequest request) {
        Long userId = extractUserId(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(toMap(user)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user's public profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(toPublicMap(user)));
    }

    @PatchMapping("/me/kyc")
    @Operation(summary = "Mark KYC as verified (demo endpoint)")
    public ResponseEntity<ApiResponse<String>> verifyKyc(HttpServletRequest request) {
        Long userId = extractUserId(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setKycVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("KYC verified successfully"));
    }

    private Map<String, Object> toMap(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "phone", user.getPhone(),
                "trustScore", user.getTrustScore(),
                "kycVerified", user.getKycVerified(),
                "createdAt", user.getCreatedAt()
        );
    }

    private Map<String, Object> toPublicMap(User user) {
        return Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "trustScore", user.getTrustScore(),
                "kycVerified", user.getKycVerified()
        );
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
