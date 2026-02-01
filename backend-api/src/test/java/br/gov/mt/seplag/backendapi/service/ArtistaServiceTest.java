package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.dto.ArtistaDTO;
import br.gov.mt.seplag.backendapi.model.Album;
import br.gov.mt.seplag.backendapi.model.Artista;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.repository.ArtistaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository repository;

    @InjectMocks
    private ArtistaService service;

    @DisplayName("Deve salvar um artista válido")
    @Test
    void deveSalvarArtista_quandoDadosValidos() {
        ArtistaDTO dto = criarArtistaDTO("Linkin Park", TipoArtista.BANDA);
        Artista entidadeSalva = criarArtista(1L, "Linkin Park", TipoArtista.BANDA);

        when(repository.save(any(Artista.class))).thenReturn(entidadeSalva);

        ArtistaDTO resultado = service.salvar(dto);

        assertEquals(1L, resultado.getId());
        assertEquals(dto.getNome(), resultado.getNome());
        assertEquals(dto.getTipo(), resultado.getTipo());
        verify(repository).save(any(Artista.class));
    }

    @DisplayName("Deve buscar artista por ID existente")
    @Test
    void deveBuscarPorId_quandoExiste() {
        Artista artista = criarArtista(2L, "Pink Floyd", TipoArtista.BANDA);
        when(repository.findById(2L)).thenReturn(Optional.of(artista));

        ArtistaDTO resultado = service.buscarPorId(2L);

        assertEquals(2L, resultado.getId());
        assertEquals("Pink Floyd", resultado.getNome());
        assertEquals(TipoArtista.BANDA, resultado.getTipo());
    }

    @DisplayName("Deve lançar NOT_FOUND ao buscar artista inexistente")
    @Test
    void deveLancarNotFound_quandoBuscarPorIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.buscarPorId(99L));
        assertEquals("404 NOT_FOUND", ex.getStatusCode().toString());
    }

    @DisplayName("Deve atualizar artista existente")
    @Test
    void deveAtualizarArtista_quandoExiste() {
        Artista existente = criarArtista(3L, "Nome Antigo", TipoArtista.CANTOR);
        ArtistaDTO dto = criarArtistaDTO("Nome Novo", TipoArtista.BANDA);

        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ArtistaDTO atualizado = service.atualizar(3L, dto);

        assertEquals("Nome Novo", atualizado.getNome());
        assertEquals(TipoArtista.BANDA, atualizado.getTipo());
        verify(repository).save(existente);
    }

    @DisplayName("Deve lançar NOT_FOUND ao atualizar artista inexistente")
    @Test
    void deveLancarNotFound_quandoAtualizarArtistaInexistente() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        ArtistaDTO dto = criarArtistaDTO("Qualquer", TipoArtista.CANTOR);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.atualizar(3L, dto));

        assertEquals("404 NOT_FOUND", ex.getStatusCode().toString());
        verify(repository, never()).save(any());
    }

    @DisplayName("Deve excluir artista existente limpando relacionamentos")
    @Test
    void deveExcluirArtista_quandoExiste() {
        Artista artista = criarArtista(4L, "Solo", TipoArtista.CANTOR);
        Album album = new Album();
        album.setId(10L);
        artista.getAlbuns().add(album);

        when(repository.findById(4L)).thenReturn(Optional.of(artista));

        service.excluir(4L);

        assertTrue(artista.getAlbuns().isEmpty(), "Relacionamentos devem ser limpos antes de excluir");
        verify(repository).delete(artista);
    }

    @DisplayName("Deve lançar NOT_FOUND ao excluir artista inexistente")
    @Test
    void deveLancarNotFound_quandoExcluirArtistaInexistente() {
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.excluir(4L));
        verify(repository, never()).delete(any());
    }

    @DisplayName("Deve listar artistas pelo filtro de nome com ordenação DESC")
    @Test
    void deveListarComFiltro_quandoNomeInformadoEDirecaoDesc() {
        Artista artista = criarArtista(5L, "Metallica", TipoArtista.BANDA);
        when(repository.findByNomeContainingIgnoreCase(eq("metal"), any(Sort.class))).thenReturn(List.of(artista));

        List<ArtistaDTO> resultado = service.listarComFiltro("metal", "DESC");

        assertEquals(1, resultado.size());
        assertEquals("Metallica", resultado.get(0).getNome());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findByNomeContainingIgnoreCase(eq("metal"), sortCaptor.capture());

        Sort.Order order = sortCaptor.getValue().getOrderFor("nome");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
        verify(repository, never()).findAll(any(Sort.class));
    }

    @DisplayName("Deve listar artistas sem filtro usando ordenação ASC por padrão")
    @Test
    void deveListarComFiltro_quandoNomeNaoInformadoUsaOrdemAsc() {
        Artista artista = criarArtista(6L, "Anavitoria", TipoArtista.CANTOR);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(artista));

        List<ArtistaDTO> resultado = service.listarComFiltro("   ", null);

        assertEquals(1, resultado.size());
        assertEquals("Anavitoria", resultado.get(0).getNome());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findAll(sortCaptor.capture());

        Sort.Order order = sortCaptor.getValue().getOrderFor("nome");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
        verify(repository, never()).findByNomeContainingIgnoreCase(anyString(), any(Sort.class));
    }

    private Artista criarArtista(Long id, String nome, TipoArtista tipo) {
        Artista artista = new Artista();
        artista.setId(id);
        artista.setNome(nome);
        artista.setTipo(tipo);
        artista.setAlbuns(new java.util.HashSet<>());
        return artista;
    }

    private ArtistaDTO criarArtistaDTO(String nome, TipoArtista tipo) {
        ArtistaDTO dto = new ArtistaDTO();
        dto.setId(null);
        dto.setNome(nome);
        dto.setTipo(tipo);
        return dto;
    }
}
