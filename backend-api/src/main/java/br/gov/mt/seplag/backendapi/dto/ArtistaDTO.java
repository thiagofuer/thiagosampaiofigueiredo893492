package br.gov.mt.seplag.backendapi.dto;

import br.gov.mt.seplag.backendapi.model.TipoArtista;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtistaDTO {
    private Long id;
    @NotBlank(message = "O nome é obrigatório")
    private String nome;
    @NotNull(message = "O tipo é obrigatório")
    private TipoArtista tipo;
}
