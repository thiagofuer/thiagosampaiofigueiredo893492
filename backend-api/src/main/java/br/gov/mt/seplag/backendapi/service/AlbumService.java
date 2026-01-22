package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.dto.AlbumDTO;
import br.gov.mt.seplag.backendapi.model.Album;
import br.gov.mt.seplag.backendapi.model.Artista;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.repository.AlbumRepository;
import br.gov.mt.seplag.backendapi.repository.ArtistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository repository;
    private final ArtistaRepository artistaRepository;

    @Transactional(readOnly = true)
    public Page<AlbumDTO> listarPaginado(TipoArtista tipo, String busca, Pageable pageable) {
        return buscarAlbuns(tipo, busca, pageable)
                .map(this::toDTO);
    }

    private Page<Album> buscarAlbuns(TipoArtista tipo, String busca, Pageable pageable) {
        if (tipo != null) {
            return repository.findByArtistasTipo(tipo, pageable);
        }
        if (busca != null && !busca.isBlank()) {
            return repository.findByTituloOrArtistaNome(busca, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    public AlbumDTO salvar(AlbumDTO dto) {
        Album album = new Album();
        album.setTitulo(dto.getTitulo());

        // Vinculando artistas pelos IDs informados (Relacionamento N:N)
        if (dto.getArtistaIds() != null) {
            album.setArtistas(artistaRepository.findAllById(dto.getArtistaIds())
                    .stream().collect(Collectors.toSet()));
        }

        return toDTO(repository.save(album));
    }

    private AlbumDTO toDTO(Album entity) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(entity.getId());
        dto.setTitulo(entity.getTitulo());
        dto.setDataCadastro(entity.getDataCadastro());
        dto.setImagemCapa(entity.getImagemCapa());
        dto.setNomesArtistas(entity.getArtistas().stream()
                .map(Artista::getNome)
                .collect(Collectors.toSet()));
        dto.setArtistaIds(entity.getArtistas().stream()
                .map(Artista::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}