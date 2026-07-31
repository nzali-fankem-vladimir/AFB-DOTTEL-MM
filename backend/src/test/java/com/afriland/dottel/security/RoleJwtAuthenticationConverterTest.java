package com.afriland.dottel.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class RoleJwtAuthenticationConverterTest {

    private final RoleJwtAuthenticationConverter converter = new RoleJwtAuthenticationConverter();

    @Test
    void convert_roleValide_retourneAutoriteRoleArh() {
        Jwt jwt = Jwt.withTokenValue("token.jwt.simule")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("1562")
                .claim("role", "ARH")
                .build();

        Collection<GrantedAuthority> autorites = converter.convert(jwt);

        assertThat(autorites)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ARH");
    }

    @Test
    void convert_roleAbsent_neLevePasExceptionEtRetourneListeVide() {
        // Documente le comportement ACTUEL du convertisseur : si le claim "role"
        // est absent du jeton, convert() retourne une liste d'autorites vide,
        // sans lever d'exception.
        //
        // BACKLOG TECHNIQUE : ce comportement silencieux n'est pas un risque
        // immediat car le point d'attention 9 de CLAUDE.md impose un
        // @PreAuthorize avec role precis sur chaque endpoint metier (fail-closed
        // sur tous les endpoints proteges). Neanmoins, une vigilance particuliere
        // est requise a chaque nouveau controleur dans les prochains sprints,
        // pour ne jamais introduire un endpoint qui ne verifierait que
        // isAuthenticated() sans role explicite -- un tel endpoint deviendrait
        // accessible a tout utilisateur authentifie, meme sans claim role.
        Jwt jwt = Jwt.withTokenValue("token.jwt.simule")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("1562")
                .claim("sub", "1562")
                .build();

        Collection<GrantedAuthority> autorites = converter.convert(jwt);

        assertThat(autorites).isEmpty();
    }
}