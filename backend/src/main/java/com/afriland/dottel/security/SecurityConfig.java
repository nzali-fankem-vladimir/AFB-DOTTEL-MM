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

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RoleJwtAuthenticationConverter roleJwtAuthenticationConverter;
    private final Environment environment;

    public SecurityConfig(RoleJwtAuthenticationConverter roleJwtAuthenticationConverter, Environment environment) {
        this.roleJwtAuthenticationConverter = roleJwtAuthenticationConverter;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(roleJwtAuthenticationConverter);

        boolean devProfile = environment.matchesProfiles("dev");

        http
                .csrf(csrf -> csrf.disable())
                // Sans cet appel, Spring Security n'a pas connaissance du CorsConfigurationSource
                // (CorsConfig) et bloque le preflight OPTIONS des endpoints authentifies avant
                // qu'il n'atteigne le traitement CORS de Spring MVC.
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/auth/login").permitAll()
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