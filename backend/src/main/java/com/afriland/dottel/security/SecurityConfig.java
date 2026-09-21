package com.afriland.dottel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RoleJwtAuthenticationConverter roleJwtAuthenticationConverter;
    private final Environment environment;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(RoleJwtAuthenticationConverter roleJwtAuthenticationConverter, Environment environment,
            CorsConfigurationSource corsConfigurationSource) {
        this.roleJwtAuthenticationConverter = roleJwtAuthenticationConverter;
        this.environment = environment;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(roleJwtAuthenticationConverter);

        boolean devProfile = environment.matchesProfiles("dev");

        http
                .csrf(csrf -> csrf.disable())
                // Bean CorsConfigurationSource explicite (CorsConfig) : sans lui, Spring
                // Security ne connait aucune configuration CORS (il n'exploite pas le
                // registre WebMvcConfigurer, qui s'execute plus bas dans la pile) et bloque
                // le preflight OPTIONS des endpoints authentifies avant meme d'atteindre
                // Spring MVC.
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Sprint MM.14 : le matcher "/auth/login" est retire -- cet
                    // endpoint n'existe plus depuis MM.7 (AuthController ne
                    // declare plus que /auth/logout, protege comme le reste).
                    // Le matcher etait inerte (aucune route ne le matchait
                    // plus) mais faisait croire a une route publique morte.
                    auth
                            .requestMatchers("/actuator/health").permitAll();
                    if (devProfile) {
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    // JwtDecoder n'est plus declare manuellement : Spring Boot le construit
    // automatiquement (JWKS + validation issuer) a partir de la propriete
    // spring.security.oauth2.resourceserver.jwt.issuer-uri (application.yml).

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}