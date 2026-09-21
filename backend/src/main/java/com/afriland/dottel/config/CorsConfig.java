package com.afriland.dottel.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Expose un bean CorsConfigurationSource explicite, plutot qu'un simple
// WebMvcConfigurer#addCorsMappings : SecurityConfig#securityFilterChain
// appelle .cors(...) sur la chaine de filtres Spring Security, qui s'execute
// AVANT la couche Spring MVC et ignore donc le registre WebMvcConfigurer.
// Sans ce bean, le preflight OPTIONS des endpoints authentifies n'obtient
// aucun header Access-Control-Allow-Origin et le navigateur bloque la
// requete (constate au demarrage local, Sprint 7.3-mm).
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Content-Disposition expose : sans elle, le JS cote frontend (origine
        // distincte du backend) ne peut pas lire le nom de fichier suggere par
        // /beneficiaires/export et retombe sur un nom generique (decouvert au
        // test manuel du Sprint 6F.5).
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}