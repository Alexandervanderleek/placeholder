package com.taskmanagement.service;

import com.taskmanagement.dto.AuthResponseDTO;
import com.taskmanagement.model.Role;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.RoleRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.GoogleTokenVerifier;
import com.taskmanagement.security.JWTTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final GoogleTokenVerifier googleTokenVerifier;
    private final JWTTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AuthService(
            GoogleTokenVerifier googleTokenVerifier,
            JWTTokenProvider tokenProvider,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public Optional<AuthResponseDTO> authenticateWithGoogle(String googleIdToken) {
        return googleTokenVerifier.verify(googleIdToken)
                .map(googleUserInfo -> {
                    // Look for existing user by Google ID
                    Optional<User> existingUser = userRepository.findByGoogleId(googleUserInfo.getGoogleId());

                    User user = existingUser.orElseGet(() -> createNewUser(googleUserInfo));

                    String jwt = tokenProvider.createToken(
                            user.getId(),
                            user.getEmail(),
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
                    );

                    return new AuthResponseDTO(jwt, user.getId(), user.getName(), user.getEmail());
                });
    }

    private User createNewUser(GoogleTokenVerifier.GoogleUserInfo googleUserInfo) {
        logger.info("Creating new user for Google account: {}", googleUserInfo.getEmail());

        // Get the default role (DEVELOPER)
        Role defaultRole = roleRepository.findByName("DEVELOPER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));

        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setGoogleId(googleUserInfo.getGoogleId());
        newUser.setEmail(googleUserInfo.getEmail());

        String name = googleUserInfo.getName();
        if (name == null || name.trim().isEmpty()) {
            name = googleUserInfo.getEmail().split("@")[0]; // Use part before @ as name
        }
        newUser.setName(name);

        newUser.setRole(defaultRole);

        return userRepository.save(newUser);
    }
}