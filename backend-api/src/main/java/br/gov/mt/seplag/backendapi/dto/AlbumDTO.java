package br.gov.mt.seplag.backendapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Schema(description = "Representação de um Álbum e seus vínculos")
public class AlbumDTO {

    @Schema(description = "ID único gerado pelo banco", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O título do álbum é obrigatório")
    @Schema(description = "Título da obra musical", example = "The Dark Side of the Moon", requiredMode = Schema.RequiredMode.REQUIRED)
    private String titulo;

    @Schema(description = "Data de registro no sistema", example = "2024-05-20T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCadastro;

    @Schema(description = "Entrada: Nome do arquivo no S3 (ex: capa.jpg). Saída: URL pré-assinada para visualização.",
            example = "http://localhost:9000/albuns/capa.jpg?token=...")
    private String imagemCapa;

    @Schema(description = "IDs dos artistas para vínculo (Utilizado no POST/PUT)", example = "[1, 2]")
    private Set<Long> artistaIds;

    @Schema(description = "Nomes dos artistas vinculados (Retornado apenas no GET)",
            example = "[\"Pink Floyd\"]", accessMode = Schema.AccessMode.READ_ONLY)
    private Set<String> nomesArtistas;
}