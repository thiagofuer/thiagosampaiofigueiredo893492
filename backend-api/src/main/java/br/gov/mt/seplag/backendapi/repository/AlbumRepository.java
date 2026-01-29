package br.gov.mt.seplag.backendapi.repository;

import br.gov.mt.seplag.backendapi.model.Album;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN a.artistas art " +
            "WHERE (:tipo IS NULL OR art.tipo = :tipo) " +
            "AND (:termo IS NULL OR LOWER(a.titulo) LIKE LOWER(CONCAT(:termo, '%')) " +
            "OR LOWER(art.nome) LIKE LOWER(CONCAT(:termo, '%')))")
    Page<Album> listarComFiltros(@Param("tipo") TipoArtista tipo, @Param("termo") String termo, Pageable pageable);

}