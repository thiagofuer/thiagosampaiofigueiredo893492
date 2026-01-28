package br.gov.mt.seplag.backendapi.dto;

import br.gov.mt.seplag.backendapi.model.TipoArtista;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados de cadastro de Artistas")
public class ArtistaDTO {

    @Schema(description = "ID do artista", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Schema(description = "Nome do cantor ou da banda", example = "Michel Teló ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotNull(message = "O tipo é obrigatório")
    @Schema(description = "Tipo de formação (CANTOR ou BANDA)", example = "CANTOR", requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoArtista tipo;
}