package com.afriland.dottel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Sprint MM.14 (revue de conformite Swagger demandee apres reparation du
// bump springdoc 3.1.0). Jusqu'ici aucun bean OpenAPI n'existait : springdoc
// tournait sur ses seuls defauts, ce qui produisait deux lacunes reelles,
// verifiees sur /api/v3/api-docs :
//
//   1. Aucun schema de securite declare (components.securitySchemes vide,
//      security globale absente) -- Swagger UI n'affichait donc pas de
//      bouton "Authorize", et tout "Try it out" sur un endpoint protege
//      echouait en 401 sans qu'il soit possible d'y joindre un jeton.
//      C'est exactement le symptome remonte sur POST /auth/logout.
//   2. info.title/version valaient "OpenAPI definition" / "v0", les
//      defauts springdoc bruts -- aucune conformite avec le contrat
//      documente dans docs/reference/contrats_api_dotations_v3.md.
//
// Les 42 chemins et leur couverture, eux, etaient deja corrects (verifies
// un par un contre le contrat V3.7) : ce correctif ne change aucun chemin,
// il rend la documentation exploitable et alignee sur son propre contrat.
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_BEARER = "bearerAuth";

    @Bean
    public OpenAPI dottelOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AFB DOTTEL -- Dotations Téléphoniques Mensuelles")
                        .version("3.7")
                        .description("Module de digitalisation du processus de paiement des "
                                + "dotations téléphoniques mensuelles d'Afriland First Bank "
                                + "(AFRILAND HORIZON 2030). Authentification Keycloak (JWT "
                                + "vérifié par JWKS, jeton obtenu hors de cette API) sauf "
                                + "mention contraire. Voir docs/reference/"
                                + "contrats_api_dotations_v3.md pour le détail métier par "
                                + "endpoint (rôles autorisés, règles de gestion)."))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Jeton Keycloak (Authorization Code + PKCE). "
                                        + "Absent de Swagger : ce contrôleur n'émet plus de "
                                        + "jeton depuis le Sprint MM.7, il s'obtient via le "
                                        + "frontend (redirection Keycloak).")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_BEARER));
    }

    // Documente, sur CHAQUE operation, les deux codes d'erreur que le modele
    // de securite peut produire uniformement (SecurityConfig.anyRequest()
    // .authenticated() + @PreAuthorize par endpoint) : 401 si le jeton est
    // absent/invalide/expire, 403 si le role ne correspond pas. Les codes
    // metier (400/404/409, variables par endpoint) restent documentes en
    // prose dans contrats_api_dotations_v3.md plutot qu'ici -- les generaliser
    // a tous les endpoints serait inexact pour ceux qui ne les produisent
    // jamais. 401/403, eux, sont bien globaux : c'est le nom que
    // GlobalExceptionHandler leur donne (CLAUDE.md section 8 : "Codes HTTP :
    // 200, 201, 400, 401, 403, 404, 409").
    @Bean
    public GlobalOperationCustomizer securityResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            operation.getResponses()
                    .addApiResponse("401", new ApiResponse()
                            .description("Non authentifié -- jeton absent, invalide ou expiré."))
                    .addApiResponse("403", new ApiResponse()
                            .description("Authentifié mais rôle non autorisé pour cette opération."));
            return operation;
        };
    }
}
