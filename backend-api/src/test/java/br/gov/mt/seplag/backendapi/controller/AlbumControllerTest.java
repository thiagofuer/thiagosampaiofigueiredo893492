package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.config.JwtService;
import br.gov.mt.seplag.backendapi.config.SecurityFilter;
import br.gov.mt.seplag.backendapi.dto.AlbumDTO;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.service.AlbumService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlbumController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AlbumControllerTest.MockBeansConfig.class)
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlbumService albumService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(albumService);
    }

    @DisplayName("Deve listar álbuns paginados aplicando filtros")
    @Test
    void listar_deveRetornarPaginaComConteudo_quandoExistemAlbuns() throws Exception {
        AlbumDTO dto = criarAlbumDTO(1L, "The Dark Side", Set.of("Pink Floyd"));
        Page<AlbumDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        given(albumService.listarPaginado(eq(TipoArtista.BANDA), eq("rock"), any())).willReturn(page);

        mockMvc.perform(get("/v1/albuns")
                        .param("tipo", TipoArtista.BANDA.name())
                        .param("termo", "rock")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].titulo").value("The Dark Side"))
                .andExpect(jsonPath("$.content[0].nomesArtistas[0]").value("Pink Floyd"));

        verify(albumService).listarPaginado(eq(TipoArtista.BANDA), eq("rock"), any());
    }

    @DisplayName("Deve retornar página vazia quando não houver álbuns")
    @Test
    void listar_deveRetornarPaginaVazia_quandoNaoExistemAlbuns() throws Exception {
        Page<AlbumDTO> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(albumService.listarPaginado(isNull(), isNull(), any())).willReturn(page);

        mockMvc.perform(get("/v1/albuns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(albumService).listarPaginado(isNull(), isNull(), any());
    }

    @DisplayName("Deve criar álbum com sucesso")
    @Test
    void criar_deveRetornarCreated_quandoSucesso() throws Exception {
        AlbumDTO entrada = criarAlbumDTO(null, "Meteora", null);
        AlbumDTO salvo = criarAlbumDTO(10L, "Meteora", Set.of("Linkin Park"));
        given(albumService.salvar(any(AlbumDTO.class))).willReturn(salvo);

        mockMvc.perform(post("/v1/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.titulo").value("Meteora"));

        verify(albumService).salvar(any(AlbumDTO.class));
    }

    @DisplayName("Deve rejeitar criação quando título está vazio")
    @Test
    void criar_deveRetornarBadRequest_quandoTituloInvalido() throws Exception {
        String payload = "{\"titulo\":\"\"}";

        mockMvc.perform(post("/v1/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(albumService);
    }

    @DisplayName("Deve buscar álbum por ID existente")
    @Test
    void buscarPorId_deveRetornarAlbum_quandoExiste() throws Exception {
        AlbumDTO dto = criarAlbumDTO(5L, "Hybrid Theory", Set.of("Linkin Park"));
        given(albumService.buscarPorId(5L)).willReturn(dto);

        mockMvc.perform(get("/v1/albuns/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.titulo").value("Hybrid Theory"));

        verify(albumService).buscarPorId(5L);
    }

    @DisplayName("Deve retornar 404 ao buscar álbum inexistente")
    @Test
    void buscarPorId_deveRetornarNotFound_quandoNaoExiste() throws Exception {
        given(albumService.buscarPorId(99L)).willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Nao encontrado"));

        mockMvc.perform(get("/v1/albuns/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Deve atualizar álbum existente")
    @Test
    void atualizar_deveRetornarOk_quandoSucesso() throws Exception {
        AlbumDTO entrada = criarAlbumDTO(null, "Minutes to Midnight", Set.of(1L));
        AlbumDTO atualizado = criarAlbumDTO(7L, "Minutes to Midnight", Set.of("Linkin Park"));
        given(albumService.atualizar(eq(7L), any(AlbumDTO.class))).willReturn(atualizado);

        mockMvc.perform(put("/v1/albuns/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.titulo").value("Minutes to Midnight"));

        verify(albumService).atualizar(eq(7L), any(AlbumDTO.class));
    }

    @DisplayName("Deve retornar 404 ao atualizar álbum inexistente")
    @Test
    void atualizar_deveRetornarNotFound_quandoNaoExiste() throws Exception {
        AlbumDTO entrada = criarAlbumDTO(null, "Minutes", null);
        given(albumService.atualizar(eq(15L), any(AlbumDTO.class))).willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Nao encontrado"));

        mockMvc.perform(put("/v1/albuns/{id}", 15L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Deve excluir álbum existente")
    @Test
    void excluir_deveRetornarNoContent_quandoSucesso() throws Exception {
        doNothing().when(albumService).excluir(3L);

        mockMvc.perform(delete("/v1/albuns/{id}", 3L))
                .andExpect(status().isNoContent());

        verify(albumService).excluir(3L);
    }

    @DisplayName("Deve retornar 404 ao excluir álbum inexistente")
    @Test
    void excluir_deveRetornarNotFound_quandoNaoExiste() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Nao encontrado")).when(albumService).excluir(20L);

        mockMvc.perform(delete("/v1/albuns/{id}", 20L))
                .andExpect(status().isNotFound());
    }

    private AlbumDTO criarAlbumDTO(Long id, String titulo, Set<?> artistas) {
        AlbumDTO dto = new AlbumDTO();
        dto.setId(id);
        dto.setTitulo(titulo);
        dto.setDataCadastro(LocalDateTime.of(2024, 1, 1, 12, 0));
        if (artistas != null && !artistas.isEmpty()) {
            if (artistas.iterator().next() instanceof String) {
                dto.setNomesArtistas((Set<String>) artistas);
            } else {
                dto.setArtistaIds((Set<Long>) artistas);
            }
        }
        dto.setImagemCapa("capa.jpg");
        return dto;
    }

    @TestConfiguration
    static class MockBeansConfig {
        @Bean
        AlbumService albumService() {
            return Mockito.mock(AlbumService.class);
        }

        @Bean
        SecurityFilter securityFilter() {
            return Mockito.mock(SecurityFilter.class);
        }

        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }
    }
}
