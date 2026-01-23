package br.gov.mt.seplag.backendapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void enviarNotificacao(String mensagem) {
        // Envia para todos os inscritos no tópico /topic/notificacoes
        messagingTemplate.convertAndSend("/topic/notificacoes", mensagem);
        log.info("Disparando mensagem via WebSocket: {}", mensagem);
    }
}
