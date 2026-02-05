package br.gov.mt.seplag.backendapi.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    @Value("${api.security.token.secret}")
    private String accessSecret;

    @Value("${api.security.refresh-token.secret:}")
    private String refreshSecret;

    @Getter
    @Value("${api.security.token.ttl-seconds:300}")
    private long accessTokenTtlSeconds;

    @Getter
    @Value("${api.security.refresh-token.ttl-seconds:300}")
    private long refreshTokenTtlSeconds;

    public String gerarAccessToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
                .signWith(getAccessSecretKey())
                .compact();
    }

    public String gerarRefreshToken(String username, String tokenId, Instant expiresAt) {
        return Jwts.builder()
                .id(tokenId)
                .subject(username)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .signWith(getRefreshSecretKey())
                .compact();
    }

    public String validarAccessToken(String token) {
        try {
            var payload = Jwts.parser()
                    .verifyWith(getAccessSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!TOKEN_TYPE_ACCESS.equals(payload.get(CLAIM_TOKEN_TYPE, String.class))) {
                return null;
            }

            return payload.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Optional<RefreshTokenPayload> parseRefreshToken(String token) {
        try {
            var payload = Jwts.parser()
                    .verifyWith(getRefreshSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!TOKEN_TYPE_REFRESH.equals(payload.get(CLAIM_TOKEN_TYPE, String.class))) {
                return Optional.empty();
            }

            return Optional.of(new RefreshTokenPayload(
                    payload.getSubject(),
                    UUID.fromString(payload.getId())));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private SecretKey getAccessSecretKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshSecretKey() {
        String secretToUse = (refreshSecret == null || refreshSecret.isBlank()) ? accessSecret : refreshSecret;
        return Keys.hmacShaKeyFor(secretToUse.getBytes(StandardCharsets.UTF_8));
    }

    public record RefreshTokenPayload(String username, UUID tokenId) {}
}