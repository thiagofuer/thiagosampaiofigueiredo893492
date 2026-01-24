package br.gov.mt.seplag.backendapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner presigner;

    @InjectMocks
    private S3Service s3Service;


    @Test
    @DisplayName("Deve configurar a requisição de assinatura com os parâmetros corretos do edital")
    void deveConfigurarRequisicaoCorretamente() {
        // GIVEN
        String fileName = "album_foto.jpg";
        // Capturador para interceptar o que o Service envia para a AWS
        ArgumentCaptor<GetObjectPresignRequest> capturador = ArgumentCaptor.forClass(GetObjectPresignRequest.class);

        // Mock do retorno para o fluxo não quebrar
        PresignedGetObjectRequest mockResponse = mock(PresignedGetObjectRequest.class);
        when(mockResponse.url()).thenReturn(mock(URL.class));
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockResponse);

        // WHEN
        s3Service.gerarUrlPreAssinada(fileName);

        // THEN
        verify(presigner).presignGetObject(capturador.capture()); // Captura a requisição enviada
        GetObjectPresignRequest requestEnviada = capturador.getValue();

        // Valida se gerou o link com duração de 30 minutos
        assertEquals(Duration.ofMinutes(30), requestEnviada.signatureDuration(), "O link deve expirar em exatamente 30 minutos conforme o requisito");
        // Valida se gerou o link com o nome correto do arquivo
        assertEquals(fileName, requestEnviada.getObjectRequest().key(), "O serviço deve solicitar o arquivo correto para assinatura");
    }

}