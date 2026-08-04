package com.afriland.dottel.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleJwtAuthenticationConverterTest {

    private final RoleJwtAuthenticationConverter converter = new RoleJwtAuthenticationConverter();

    private Jwt.Builder jwtDeBase() {
        return Jwt.withTokenValue("token.jwt.simule")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("3f2504e0-4f89-11d3-9a0c-0305e82c3301");
    }

    @Test
    void convert_unSeulRoleDottelDansRealmAccess_retourneAutoriteCorrespondante() {
        Jwt jwt = jwtDeBase()
                .claim("realm_access", Map.of("roles", List.of("offline_access", "ARH", "default-roles-dottel-dev")))
                .build();

        Collection<GrantedAuthority> autorites = converter.convert(jwt);

        assertThat(autorites)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ARH");
    }

    @Test
    void convert_realmAccessAbsent_neLevePasExceptionEtRetourneListeVide() {
        // Documente le comportement ACTUEL du convertisseur : si le claim
        // "realm_access" est absent du jeton, convert() retourne une liste
        // d'autorites vide, sans lever d'exception.
        //
        // BACKLOG TECHNIQUE : ce comportement silencieux n'est pas un risque
        // immediat car le point d'attention 9 de CLAUDE.md impose un
        // @PreAuthorize avec role precis sur chaque endpoint metier (fail-closed
        // sur tous les endpoints proteges). Neanmoins, une vigilance particuliere
        // est requise a chaque nouveau controleur dans les prochains sprints,
        // pour ne jamais introduire un endpoint qui ne verifierait que
        // isAuthenticated() sans role explicite -- un tel endpoint deviendrait
        // accessible a tout utilisateur authentifie, meme sans claim realm_access.
        Jwt jwt = jwtDeBase().build();

        Collection<GrantedAuthority> autorites = converter.convert(jwt);

        assertThat(autorites).isEmpty();
    }

    @Test
    void convert_aucunRoleDottelParmiLesRolesKeycloak_retourneListeVide() {
        Jwt jwt = jwtDeBase()
                .claim("realm_access", Map.of("roles", List.of("offline_access", "uma_authorization")))
                .build();

        Collection<GrantedAuthority> autorites = converter.convert(jwt);

        assertThat(autorites).isEmpty();
    }

    @Test
    void convert_plusieursRolesDottelSimultanes_refuseEtRetourneListeVide() {
        // Decision actee le 2026-08-03 (Sprint MM.7, etape 6) : un cumul de
        // roles DOTTEL sur un meme jeton violerait RG-08 (separation des
        // taches). Traite comme une erreur de configuration du realm : aucune
        // autorite n'est accordee, plutot que de choisir arbitrairement un role
        // ou de cumuler les deux -- chaque @PreAuthorize echouera ensuite (403).
        Jwt jwt = jwtDeBase()
                .claim("realm_access", Map.of("roles", List.of("ARH", "DRH")))
                .build();

        Collection<GrantedAuthority> autorites = converter.convert(jwt);

        assertThat(autorites).isEmpty();
    }
}
