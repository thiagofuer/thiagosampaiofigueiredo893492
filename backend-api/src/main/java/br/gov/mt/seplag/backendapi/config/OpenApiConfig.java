package br.gov.mt.seplag.backendapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão de Artistas e Álbuns - SEPLAG-MT")
                        .version("v1")
                        .description("Serviço desenvolvido para o Processo Seletivo Simplificado (PSS) da SEPLAG-MT."));
    }
}