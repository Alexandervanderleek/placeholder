package com.taskmanagement.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;


@Component
public class GoogleTokenVerifier {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${security.oauth2.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
        logger.info("GoogleTokenVerifier initialized with client ID: {}", clientId);
    }

    public Optional<GoogleUserInfo> verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                GoogleUserInfo userInfo = new GoogleUserInfo(
                        payload.getSubject(),
                        payload.getEmail(),
                        (String) payload.get("name"),
                        (String) payload.get("picture")
                );

                logger.info("Successfully verified Google token for user: {}", userInfo.getEmail());
                return Optional.of(userInfo);
            } else {
                logger.warn("Invalid Google ID token provided");
                return Optional.empty();
            }
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Error verifying Google token", e);
            return Optional.empty();
        }
    }

    public static class GoogleUserInfo {
        private final String googleId;
        private final String email;
        private final String name;
        private final String pictureUrl;

        public GoogleUserInfo(String googleId, String email, String name, String pictureUrl) {
            this.googleId = googleId;
            this.email = email;
            this.name = name;
            this.pictureUrl = pictureUrl;
        }

        public String getGoogleId() {
            return googleId;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getPictureUrl() {
            return pictureUrl;
        }
    }

}