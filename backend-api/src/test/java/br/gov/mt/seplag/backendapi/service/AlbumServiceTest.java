package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.dto.AlbumDTO;
import br.gov.mt.seplag.backendapi.model.Album;
import br.gov.mt.seplag.backendapi.repository.AlbumRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository repository;

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
}
