package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.client.RegionalClient;
import br.gov.mt.seplag.backendapi.dto.RegionalDTO;
import br.gov.mt.seplag.backendapi.model.Regional;
import br.gov.mt.seplag.backendapi.repository.RegionalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionalSyncServiceTest {

    @Mock
    private RegionalClient client;

    @Mock
    private RegionalRepository repository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RegionalSyncService syncService;

    @Test
    @DisplayName("Deve inativar regional antiga e criar nova quando o nome mudar no sistema externo")
    void deveVersionarRegionalQuandoNomeMudar() {
        // GIVEN
        Long idExterno = 10L;
        RegionalDTO dtoExterno = new RegionalDTO();
        dtoExterno.setId(idExterno);
        dtoExterno.setNome("Cuiabá - Nome Novo");

        when(client.buscarTodas()).thenReturn(List.of(dtoExterno));

        Regional regionalAtivaLocal = new Regional();
        regionalAtivaLocal.setId(1L);
        regionalAtivaLocal.setIdExterno(idExterno);
        regionalAtivaLocal.setNome("Cuiabá - Nome Antigo");
        regionalAtivaLocal.setAtivo(true);

        when(repository.findByIdExternoAndAtivoTrue(idExterno))
                .thenReturn(Optional.of(regionalAtivaLocal));

        // WHEN
        syncService.sincronizar();

        // THEN
        // Verifica se a regional antiga foi setada como inativa
        assertFalse(regionalAtivaLocal.isAtivo(), "A regional antiga deveria ter sido inativada");

        // Verifica se o repository salvou 2 vezes (1 para inativar a velha, 1 para criar a nova)
        verify(repository, times(2)).save(any(Regional.class));
        // Verifica se a notificação do websocket foi disparada
        verify(notificationService).enviarNotificacao(anyString());
    }
}