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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
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

    @Transactional(readOnly = true)
    public AlbumDTO buscarPorId(Long id) {
        Album album = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));
        return toDTO(album);
    }

    @Transactional
    public AlbumDTO salvar(AlbumDTO dto) {
        Album album = new Album();
        album.setTitulo(dto.getTitulo());
        album.setImagemCapa(dto.getImagemCapa());
        atualizarArtistas(album, dto.getArtistaIds());
        AlbumDTO albumDTO = toDTO(repository.save(album));
        //WebSocket para notificar o front a cada novo álbum cadastrado.
        notificationService.enviarNotificacao("Novo álbum cadastrado: " + album.getTitulo());
        return albumDTO;
    }

    @Transactional
    public AlbumDTO atualizar(Long id, AlbumDTO dto) {
        Album album = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));

        album.setTitulo(dto.getTitulo());
        album.setImagemCapa(dto.getImagemCapa());
        atualizarArtistas(album, dto.getArtistaIds());

        Album atualizado = repository.save(album);
        return toDTO(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Album album = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Álbum não encontrado"));
        album.getArtistas().forEach(artista -> artista.getAlbuns().remove(album));
        album.getArtistas().clear();
        repository.delete(album);
    }

    private void atualizarArtistas(Album album, Set<Long> artistaIds) {
        if (artistaIds == null) {
            return;
        }
        var artistas = new HashSet<>(artistaRepository.findAllById(artistaIds));
        if (artistas.size() != artistaIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um ou mais artistas informados não existem.");
        }
        artistas.forEach(a -> a.getAlbuns().add(album));
        album.setArtistas(artistas);
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