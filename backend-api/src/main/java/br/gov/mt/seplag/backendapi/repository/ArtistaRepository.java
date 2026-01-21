package br.gov.mt.seplag.backendapi.repository;

import br.gov.mt.seplag.backendapi.model.Artista;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
    List<Artista> findByNomeContainingIgnoreCase(String nome, Sort sort);
}
