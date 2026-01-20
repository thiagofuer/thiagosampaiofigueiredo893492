package br.gov.mt.seplag.backendapi.repository;

import br.gov.mt.seplag.backendapi.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
}
