package br.gov.mt.seplag.backendapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class AlbumDTO {
    private Long id;
    @NotBlank(message = "O título do álbum é obrigatório")
    private String titulo;
    private LocalDateTime dataCadastro;
    // Campo para receber os IDs no POST/PUT (Entrada)
    private Set<Long> artistaIds;
    // Lista de IDs ou Nomes dos artistas vinculados
    private Set<String> nomesArtistas;
}