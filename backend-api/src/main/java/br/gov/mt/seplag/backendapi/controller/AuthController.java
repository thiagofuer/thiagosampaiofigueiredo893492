package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.config.JwtService;
import br.gov.mt.seplag.backendapi.dto.AuthRequest;
import br.gov.mt.seplag.backendapi.dto.AuthResponse;
import br.gov.mt.seplag.backendapi.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Gerenciamento de autenticação e tokens JWT")
public class AuthController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Login", description = "Autentica um usuário e retorna um token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\", \"refreshToken\": \"....\"}"))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de login", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"username\": \"admin\", \"password\": \"admin123\"}")))
            @RequestBody AuthRequest loginData) {
        String user = loginData.username();
        // Simulação de usuário e senha fixas apenas para o teste do PSS
        if ("admin".equals(user) && "admin123".equals(loginData.password())) {
            String accessToken = jwtService.gerarAccessToken(user);
            String refreshToken = refreshTokenService.createRefreshTokenForUser(user);
            return ResponseEntity.ok(new AuthResponse(
                    "Bearer",
                    accessToken,
                    jwtService.getAccessTokenTtlSeconds(),
                    refreshToken,
                    jwtService.getRefreshTokenTtlSeconds()
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @Operation(summary = "Refresh", description = "Renova access token usando refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiJ9...\", \"refreshToken\": \"....\"}"))),
            @ApiResponse(responseCode = "400", description = "Refresh token ausente ou inválido"),
            @ApiResponse(responseCode = "401", description = "Refresh token expirado ou não autorizado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token atual", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiJ9...\"}")))
            @RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<JwtService.RefreshTokenPayload> payloadOpt = jwtService.parseRefreshToken(refreshToken);
        if (payloadOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var payload = payloadOpt.get();

        var valid = refreshTokenService.validateAndGetId(refreshToken);
        if (valid.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String username = payload.username();
        // revoga o antigo e emite o novo
        refreshTokenService.revoke(payload.tokenId());
        String newRefresh = refreshTokenService.createRefreshTokenForUser(username);
        String newAccess = jwtService.gerarAccessToken(username);

        return ResponseEntity.ok(new AuthResponse(
                "Bearer",
                newAccess,
                jwtService.getAccessTokenTtlSeconds(),
                newRefresh,
                jwtService.getRefreshTokenTtlSeconds()
        ));
    }

    @Operation(summary = "Logout", description = "Revoga um refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiJ9...\"}"))),
            @ApiResponse(responseCode = "400", description = "Refresh token ausente ou inválido")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token a ser revogado", required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiJ9...\"}")))
            @RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) return ResponseEntity.badRequest().build();
        Optional<JwtService.RefreshTokenPayload> payloadOpt = jwtService.parseRefreshToken(refreshToken);
        if (payloadOpt.isEmpty()) return ResponseEntity.badRequest().build();
        var payload = payloadOpt.get();
        refreshTokenService.revoke(payload.tokenId());
        return ResponseEntity.noContent().build();
    }
}