package br.gov.mt.seplag.backendapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

}
