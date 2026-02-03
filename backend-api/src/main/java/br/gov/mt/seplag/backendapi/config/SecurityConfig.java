package br.gov.mt.seplag.backendapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SecurityFilter securityFilter,
                                           RateLimitFilter rateLimitFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) //Desabilitando csfr pois usaremos JWT (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //Desabilitando a sessão pois não é necessário para JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/static/**").permitAll() // Libera a página de teste do websocket
                        .requestMatchers("/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll() //Libera o endpoint da autenticação e swagger da necessidade de usar token
                        .requestMatchers("/ws-api/**").permitAll()
                        .requestMatchers("/actuator/*").permitAll()
                        .anyRequest().authenticated()
                )
                //Carrega as configurações do CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //Filtros: Rate Limit -> Security
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //a) Segurança: bloquear acesso ao endpoint a partir de domínios fora do domínio do serviço
        // Como não foi especificado qual o dominio, usei o localhost e o seplag
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://seplag.mt.gov.br"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}