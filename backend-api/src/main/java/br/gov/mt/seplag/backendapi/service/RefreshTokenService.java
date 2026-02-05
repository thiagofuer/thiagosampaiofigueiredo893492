package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.model.RefreshToken;
import br.gov.mt.seplag.backendapi.repository.RefreshTokenRepository;
import br.gov.mt.seplag.backendapi.config.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    @Transactional
    public String createRefreshTokenForUser(String username) {
        UUID tokenId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtService.getRefreshTokenTtlSeconds());

        RefreshToken token = new RefreshToken(tokenId, username, now, expiresAt, false);
        repository.save(token);

        return jwtService.gerarRefreshToken(username, tokenId.toString(), expiresAt);
    }

    @Transactional
    public Optional<UUID> validateAndGetId(String refreshToken) {
        var payloadOpt = jwtService.parseRefreshToken(refreshToken);
        if (payloadOpt.isEmpty()) return Optional.empty();

        var payload = payloadOpt.get();
        UUID id = payload.tokenId();
        Optional<RefreshToken> stored = repository.findByIdAndRevokedFalse(id);
        if (stored.isEmpty()) return Optional.empty();
        RefreshToken rt = stored.get();
        if (rt.getExpiresAt().isBefore(Instant.now())) return Optional.empty();
        return Optional.of(id);
    }

    @Transactional
    public void revoke(UUID id) {
        repository.findById(id).ifPresent(rt -> {
            rt.setRevoked(true);
            repository.save(rt);
        });
    }

    @Transactional
    public void rotate(UUID oldId, String username) {
        revoke(oldId);
        createRefreshTokenForUser(username);
    }
}
