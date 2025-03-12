package com.taskmanagement.controller;

import com.taskmanagement.dto.AuthResponseDTO;
import com.taskmanagement.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        if (idToken == null || idToken.isEmpty()) {
            logger.warn("Authentication attempt with empty Google ID token");
            return ResponseEntity.badRequest().body(Map.of("error", "Google ID token is required"));
        }

        logger.info("Processing Google authentication request");

        Optional<AuthResponseDTO> authResult = authService.authenticateWithGoogle(idToken);

        if (authResult.isPresent()) {
            AuthResponseDTO authResponse = authResult.get();
            logger.info("Successfully authenticated user: {}", authResponse.getEmail());
            return ResponseEntity.ok(authResponse);
        } else {
            logger.warn("Failed to authenticate with Google ID token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid Google authentication"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        // This endpoint will be protected by JWT authentication
        // If we reach here, it means the token is valid
        return ResponseEntity.ok("Token is valid");
    }
}