package com.taskmanagement.cli.service;

import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class OAuthService {

    @Value("${cli.auth.google.client-id}")
    private String clientId;

    @Value("${cli.auth.google.redirect-uri:http://localhost:8888/callback}")
    private String redirectUri;

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String SCOPE = "email profile";

    private CompletableFuture<String> authFuture;

    public URI getAuthorizationUrl() throws URISyntaxException {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);

        String url = GOOGLE_AUTH_URL +
                "?client_id=" + clientId +
                "&redirect_uri=" + encodedRedirectUri +
                "&scope=" + encodedScope +
                "&response_type=id_token" +
                "&prompt=select_account" +
                "&nonce=" + System.currentTimeMillis();

        return new URI(url);
    }

    public String waitForAuthorizationCode() throws IOException, InterruptedException {
        authFuture = new CompletableFuture<>();

        // Start local HTTP server to capture the response
        HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);

        server.createContext("/callback", exchange -> {
            String html = "<html><head><title>Authentication Successful</title></head>" +
                    "<body><h1>Authentication Successful</h1>" +
                    "<p>You can now close this window and return to the CLI.</p>" +
                    "<script>" +
                    "  const hash = window.location.hash.substring(1);" +
                    "  const params = new URLSearchParams(hash);" +
                    "  const idToken = params.get('id_token');" +
                    "  fetch('/token?id_token=' + idToken, { method: 'POST' });" +
                    "</script></body></html>";

            exchange.sendResponseHeaders(200, html.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes());
            }
        });

        server.createContext("/token", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String idToken = query.replace("id_token=", "");

            authFuture.complete(idToken);

            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        });

        server.setExecutor(null);
        server.start();

        try {
            // Wait for the authentication to complete
            String idToken = authFuture.get(5, TimeUnit.MINUTES);
            return idToken;
        } catch (Exception e) {
            authFuture.completeExceptionally(e);
            return null;
        } finally {
            server.stop(0);
        }
    }
}