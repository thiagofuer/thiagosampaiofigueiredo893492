package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.config.JwtService;
import br.gov.mt.seplag.backendapi.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArquivoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ArquivoControllerTest.TestConfig.class)
class ArquivoControllerTest {

    private static final String BASE_PATH = "/v1/arquivos/upload";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private S3Service s3Service;

    @BeforeEach
    void resetMocks() {
        reset(s3Service);
    }

    @Test
    @DisplayName("Deve fazer upload com sucesso quando o arquivo é válido")
    void upload_deveRetornar200_quandoSucesso() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes()
        );

        when(s3Service.uploadFile(any())).thenReturn("http://url-foto.jpg");

        mockMvc.perform(multipart(BASE_PATH).file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("http://url-foto.jpg"));

        verify(s3Service).uploadFile(any());
    }

    @Test
    @DisplayName("Deve retornar 500 quando o serviço lançar IOException")
    void upload_deveRetornar500_quandoServicoLancarIOException() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "file",
                "erro.png",
                MediaType.IMAGE_PNG_VALUE,
                "dados".getBytes()
        );

        when(s3Service.uploadFile(any())).thenThrow(new IOException("Falha S3"));

        mockMvc.perform(multipart(BASE_PATH).file(arquivo))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value("Falha ao processar upload: Falha S3"));

        verify(s3Service).uploadFile(any());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        S3Service s3Service() {
            return mock(S3Service.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }
}
