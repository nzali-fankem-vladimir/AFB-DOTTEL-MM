package com.afriland.dottel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                // Content-Disposition expose : sans elle, le JS cote frontend (origine
                // distincte du backend) ne peut pas lire le nom de fichier suggere par
                // /beneficiaires/export et retombe sur un nom generique (decouvert au
                // test manuel du Sprint 6F.5).
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true);
    }
}