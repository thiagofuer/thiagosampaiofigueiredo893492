package br.gov.mt.seplag.backendapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Regional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK interna do backendapi
    private Long idExterno; //PK externa que vem da api externa de Regionais
    private String nome;
    private boolean ativo;
}