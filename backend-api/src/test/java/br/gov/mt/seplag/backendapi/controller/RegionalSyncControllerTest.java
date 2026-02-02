package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.config.JwtService;
import br.gov.mt.seplag.backendapi.service.RegionalSyncService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionalSyncController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RegionalSyncControllerTest.TestConfig.class)
class RegionalSyncControllerTest {

    private static final String BASE_PATH = "/v1/admin/regionais/sincronizar";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegionalSyncService regionalSyncService;

    @BeforeEach
    void resetMocks() {
        reset(regionalSyncService);
    }

    @Test
    @DisplayName("Deve disparar sincronização regional com sucesso e retornar status 200")
    void dispararSincronizacao_deveRetornar200_quandoSucesso() throws Exception {
        doNothing().when(regionalSyncService).sincronizar();

        mockMvc.perform(post(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string("Sincronização efetuada com sucesso."));

        verify(regionalSyncService).sincronizar();
    }

    @Test
    @DisplayName("Deve retornar 500 quando ocorrer erro ao disparar sincronização")
    void dispararSincronizacao_deveRetornar500_quandoErro(){
        doThrow(new RuntimeException("Falha externa")).when(regionalSyncService).sincronizar();

        ServletException exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(post(BASE_PATH)).andReturn());

        assertEquals("Falha externa", exception.getCause().getMessage());

        verify(regionalSyncService).sincronizar();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        RegionalSyncService regionalSyncService() {
            return mock(RegionalSyncService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }
}
