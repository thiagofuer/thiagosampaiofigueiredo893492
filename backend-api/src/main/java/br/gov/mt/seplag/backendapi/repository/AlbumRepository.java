package br.gov.mt.seplag.backendapi.repository;

import br.gov.mt.seplag.backendapi.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}
