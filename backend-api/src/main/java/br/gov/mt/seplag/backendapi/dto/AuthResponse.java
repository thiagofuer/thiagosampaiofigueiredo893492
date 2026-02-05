package br.gov.mt.seplag.backendapi.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn) {
}
