package br.gov.mt.seplag.backendapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados da Regional")
public class RegionalDTO {
    @Schema(description = "ID da regional na SEPLAG", example = "10")
    private Long id;
    @Schema(description = "Nome descritivo da regional", example = "Baixada Cuiabana")
    private String nome;
}