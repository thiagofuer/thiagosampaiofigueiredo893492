package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.dto.ArtistaDTO;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.service.ArtistaService;
import br.gov.mt.seplag.backendapi.config.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ArtistaControllerTest.TestConfig.class)
class ArtistaControllerTest {

    private static final String BASE_PATH = "/v1/artistas";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtistaService service;

    @BeforeEach
    void resetMocks() {
        reset(service);
    }

    @Test
    @DisplayName("Deve criar artista com sucesso quando payload é válido")
    void criar_deveRetornar201_quandoPayloadValido() throws Exception {
        ArtistaDTO requestDto = buildDto(null, "Michel Teló", TipoArtista.CANTOR);
        ArtistaDTO responseDto = buildDto(1L, requestDto.getNome(), requestDto.getTipo());

        when(service.salvar(any(ArtistaDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.nome").value(responseDto.getNome()))
                .andExpect(jsonPath("$.tipo").value(responseDto.getTipo().name()));

        verify(service).salvar(any(ArtistaDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 400 quando payload de criação é inválido")
    void criar_deveRetornar400_quandoPayloadInvalido() throws Exception {
        ArtistaDTO invalidDto = new ArtistaDTO();
        invalidDto.setTipo(TipoArtista.BANDA);

        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("Deve retornar artista quando buscar por id existente")
    void buscarPorId_deveRetornar200_quandoEncontrado() throws Exception {
        ArtistaDTO dto = buildDto(10L, "Banda X", TipoArtista.BANDA);
        when(service.buscarPorId(10L)).thenReturn(dto);

        mockMvc.perform(get(BASE_PATH + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.nome").value(dto.getNome()));

        verify(service).buscarPorId(10L);
    }

    @Test
    @DisplayName("Deve retornar 404 quando buscar artista inexistente")
    void buscarPorId_deveRetornar404_quandoNaoEncontrado() throws Exception {
        when(service.buscarPorId(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        mockMvc.perform(get(BASE_PATH + "/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(service).buscarPorId(999L);
    }

    @Test
    @DisplayName("Deve atualizar artista com sucesso quando id existe")
    void atualizar_deveRetornar200_quandoSucesso() throws Exception {
        ArtistaDTO requestDto = buildDto(null, "Novo Nome", TipoArtista.CANTOR);
        ArtistaDTO responseDto = buildDto(5L, requestDto.getNome(), requestDto.getTipo());

        when(service.atualizar(eq(5L), any(ArtistaDTO.class))).thenReturn(responseDto);

        mockMvc.perform(put(BASE_PATH + "/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.nome").value(responseDto.getNome()));

        verify(service).atualizar(eq(5L), any(ArtistaDTO.class));
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar artista inexistente")
    void atualizar_deveRetornar404_quandoIdInexistente() throws Exception {
        ArtistaDTO requestDto = buildDto(null, "Sem Registro", TipoArtista.BANDA);

        when(service.atualizar(eq(100L), any(ArtistaDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        mockMvc.perform(put(BASE_PATH + "/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(service).atualizar(eq(100L), any(ArtistaDTO.class));
    }

    @Test
    @DisplayName("Deve excluir artista com sucesso quando id existe")
    void excluir_deveRetornar204_quandoSucesso() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(service).excluir(7L);
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar excluir artista inexistente")
    void excluir_deveRetornar404_quandoIdInexistente() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"))
                .when(service).excluir(50L);

        mockMvc.perform(delete(BASE_PATH + "/{id}", 50L))
                .andExpect(status().isNotFound());

        verify(service).excluir(50L);
    }

    @Test
    @DisplayName("Deve listar artistas filtrados com sucesso")
    void listar_deveRetornarLista_quandoExistemResultados() throws Exception {
        ArtistaDTO dto1 = buildDto(1L, "Artista A", TipoArtista.CANTOR);
        ArtistaDTO dto2 = buildDto(2L, "Artista B", TipoArtista.BANDA);

        when(service.listarComFiltro("Artista", "desc")).thenReturn(Arrays.asList(dto1, dto2));

        mockMvc.perform(get(BASE_PATH)
                        .param("nome", "Artista")
                        .param("ordem", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()));

        verify(service).listarComFiltro("Artista", "desc");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum artista é encontrado")
    void listar_deveRetornarListaVazia_quandoSemResultados() throws Exception {
        when(service.listarComFiltro(null, "asc")).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).listarComFiltro(null, "asc");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        ArtistaService artistaService() {
            return mock(ArtistaService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    private ArtistaDTO buildDto(Long id, String nome, TipoArtista tipo) {
        ArtistaDTO dto = new ArtistaDTO();
        dto.setId(id);
        dto.setNome(nome);
        dto.setTipo(tipo);
        return dto;
    }
}
