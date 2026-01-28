package br.gov.mt.seplag.backendapi.repository;

import br.gov.mt.seplag.backendapi.model.Album;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query("SELECT DISTINCT a FROM Album a JOIN a.artistas art WHERE art.tipo = :tipo")
    Page<Album> findByArtistasTipo(TipoArtista tipo, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Album a LEFT JOIN a.artistas art " +
            "WHERE LOWER(a.titulo) LIKE LOWER(CONCAT(:busca, '%')) " +
            "OR LOWER(art.nome) LIKE LOWER(CONCAT(:busca, '%'))")
    Page<Album> findByTituloOrArtistaNome(String busca, Pageable pageable);

}
