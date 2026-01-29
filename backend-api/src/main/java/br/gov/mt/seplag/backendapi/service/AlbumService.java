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

import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository repository;
    private final ArtistaRepository artistaRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<AlbumDTO> listarPaginado(TipoArtista tipo, String termo, Pageable pageable) {
        return repository.listarComFiltros(tipo, termo, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public AlbumDTO salvar(AlbumDTO dto) {
        Album album = new Album();
        album.setTitulo(dto.getTitulo());
        album.setImagemCapa(dto.getImagemCapa());
        // Vinculando artistas pelos IDs informados (Relacionamento N:N)
        if (dto.getArtistaIds() != null) {
            album.setArtistas(new HashSet<>(artistaRepository.findAllById(dto.getArtistaIds())));
        }
        AlbumDTO albumDTO = toDTO(repository.save(album));
        notificationService.enviarNotificacao("Novo álbum cadastrado: " + album.getTitulo());
        return albumDTO;
    }

    private AlbumDTO toDTO(Album entity) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(entity.getId());
        dto.setTitulo(entity.getTitulo());
        dto.setDataCadastro(entity.getDataCadastro());
        dto.setImagemCapa(s3Service.gerarUrlPreAssinada(entity.getImagemCapa()));
        dto.setNomesArtistas(entity.getArtistas().stream()
                .map(Artista::getNome)
                .collect(Collectors.toSet()));
        dto.setArtistaIds(entity.getArtistas().stream()
                .map(Artista::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}