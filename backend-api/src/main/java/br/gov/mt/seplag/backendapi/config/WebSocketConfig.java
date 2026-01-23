package br.gov.mt.seplag.backendapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefixo para mensagens que o servidor envia para o cliente (Ex: notificações)
        config.enableSimpleBroker("/topic");
        // Prefixo para mensagens enviadas do cliente para o servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Ponto de entrada da conexão
        registry.addEndpoint("/ws-api")
                .setAllowedOrigins("http://localhost:8080", "http://seplag.mt.gov.br")
                .withSockJS(); // Fallback para navegadores antigos
    }
}
