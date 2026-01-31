package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.dto.AlbumDTO;
import br.gov.mt.seplag.backendapi.model.Album;
import br.gov.mt.seplag.backendapi.model.Artista;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.repository.AlbumRepository;
import br.gov.mt.seplag.backendapi.repository.ArtistaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository repository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AlbumService albumService;

    @Test
    @DisplayName("Deve salvar um álbum com sucesso e disparar notificação")
    void deveSalvarAlbumComSucesso() {
        // GIVEN (Preparação)
        AlbumDTO dto = new AlbumDTO();
        dto.setTitulo("Harakiri");
        dto.setImagemCapa("capa.jpg");

        Album albumSalvo = new Album();
        albumSalvo.setId(1L);
        albumSalvo.setTitulo("Harakiri");
        albumSalvo.setImagemCapa("capa.jpg");

        // Mockando comportamentos
        when(repository.save(any(Album.class))).thenReturn(albumSalvo);
        when(s3Service.gerarUrlPreAssinada("capa.jpg")).thenReturn("http://s3.link/capa.jpg");

        // WHEN (Execução)
        AlbumDTO resultado = albumService.salvar(dto);

        // THEN (Verificação)
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("http://s3.link/capa.jpg", resultado.getImagemCapa());

        // Verifica se o save foi chamado
        verify(repository, times(1)).save(any(Album.class));
        // Verifica se a notificação do websocket foi disparada
        verify(notificationService, times(1)).enviarNotificacao(anyString());
    }

    @Test
    @DisplayName("Deve excluir um álbum existente")
    void deveExcluirAlbum() {
        Album album = new Album();
        album.setId(1L);
        album.setTitulo("Harakiri");

        when(repository.findById(1L)).thenReturn(Optional.of(album));

        albumService.excluir(1L);

        verify(repository).delete(album);
    }

    @Test
    @DisplayName("Deve listar álbuns aplicando filtros e converter para DTOs")
    void deveListarAlbunsPaginados() {
        Pageable pageable = PageRequest.of(0, 5);
        Artista artista = artista(1L, "Pink Floyd", TipoArtista.BANDA);
        Album album = album(1L, "The Dark Side of the Moon", "capa.jpg", Set.of(artista));

        Page<Album> page = new PageImpl<>(List.of(album), pageable, 1);
        when(repository.listarComFiltros(TipoArtista.BANDA, "pink", pageable)).thenReturn(page);
        when(s3Service.gerarUrlPreAssinada("capa.jpg")).thenReturn("http://signed-url");

        Page<AlbumDTO> resultado = albumService.listarPaginado(TipoArtista.BANDA, "pink", pageable);

        assertEquals(1, resultado.getTotalElements());
        AlbumDTO dto = resultado.getContent().get(0);
        assertEquals("The Dark Side of the Moon", dto.getTitulo());
        assertEquals(Set.of("Pink Floyd"), dto.getNomesArtistas());
        assertEquals("http://signed-url", dto.getImagemCapa());

        verify(repository).listarComFiltros(TipoArtista.BANDA, "pink", pageable);
        verify(s3Service).gerarUrlPreAssinada("capa.jpg");
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver álbuns")
    void deveRetornarPaginaVazia() {
        Pageable pageable = PageRequest.of(1, 10);
        when(repository.listarComFiltros(null, null, pageable)).thenReturn(Page.empty(pageable));

        Page<AlbumDTO> resultado = albumService.listarPaginado(null, null, pageable);

        assertTrue(resultado.isEmpty());
        verify(repository).listarComFiltros(null, null, pageable);
        verify(s3Service, never()).gerarUrlPreAssinada(anyString());
    }

    @Test
    @DisplayName("Deve atualizar um álbum existente")
    void deveAtualizarAlbum() {
        Album existente = album(1L, "Hybrid Theory", "old.jpg", Set.of());
        AlbumDTO dto = new AlbumDTO();
        dto.setTitulo("Meteora");
        dto.setImagemCapa("nova.jpg");
        dto.setArtistaIds(Set.of(1L, 2L));

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(artistaRepository.findAllById(dto.getArtistaIds()))
                .thenReturn(List.of(artista(1L, "Linkin Park", TipoArtista.BANDA), artista(2L, "Mike Shinoda", TipoArtista.CANTOR)));
        when(repository.save(existente)).thenReturn(existente);
        when(s3Service.gerarUrlPreAssinada("nova.jpg")).thenReturn("http://nova");

        AlbumDTO resultado = albumService.atualizar(1L, dto);

        assertEquals("Meteora", resultado.getTitulo());
        assertEquals(Set.of(1L, 2L), resultado.getArtistaIds());
        assertEquals(Set.of("Linkin Park", "Mike Shinoda"), resultado.getNomesArtistas());
        assertEquals("http://nova", resultado.getImagemCapa());

        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND ao tentar atualizar álbum inexistente")
    void deveFalharAoAtualizarAlbumInexistente() {
        AlbumDTO dto = new AlbumDTO();
        dto.setTitulo("Qualquer");

        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> albumService.atualizar(1L, dto));
        assertEquals("404 NOT_FOUND", ex.getStatusCode().toString());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BAD_REQUEST quando artista do update não existe")
    void deveFalharQuandoArtistaNaoExisteNoUpdate() {
        Album existente = album(1L, "Album", "capa.jpg", Set.of());
        AlbumDTO dto = new AlbumDTO();
        dto.setTitulo("Album");
        dto.setArtistaIds(Set.of(1L, 2L));

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(artistaRepository.findAllById(dto.getArtistaIds()))
                .thenReturn(List.of(artista(1L, "Artista", TipoArtista.CANTOR))); // retorna apenas um artista

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> albumService.atualizar(1L, dto));
        assertEquals("400 BAD_REQUEST", ex.getStatusCode().toString());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar álbum por ID e converter para DTO")
    void deveBuscarAlbumPorId() {
        Artista artista = artista(1L, "Linkin Park", TipoArtista.BANDA);
        Album album = album(1L, "Hybrid Theory", "capa.jpg", Set.of(artista));

        when(repository.findById(1L)).thenReturn(Optional.of(album));
        when(s3Service.gerarUrlPreAssinada("capa.jpg")).thenReturn("http://signed");

        AlbumDTO dto = albumService.buscarPorId(1L);

        assertEquals(1L, dto.getId());
        assertEquals("Hybrid Theory", dto.getTitulo());
        assertEquals(Set.of("Linkin Park"), dto.getNomesArtistas());
        assertEquals("http://signed", dto.getImagemCapa());
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND ao buscar álbum inexistente")
    void deveFalharAoBuscarAlbumInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> albumService.buscarPorId(1L));
        assertEquals("404 NOT_FOUND", ex.getStatusCode().toString());
    }

    private Album album(Long id, String titulo, String imagem, Set<Artista> artistas) {
        Album album = new Album();
        album.setId(id);
        album.setTitulo(titulo);
        album.setImagemCapa(imagem);
        album.setArtistas(new java.util.HashSet<>(artistas));
        return album;
    }

    private Artista artista(Long id, String nome, TipoArtista tipo) {
        Artista artista = new Artista();
        artista.setId(id);
        artista.setNome(nome);
        artista.setTipo(tipo);
        return artista;
    }
}
