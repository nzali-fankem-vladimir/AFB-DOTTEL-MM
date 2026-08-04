package com.afriland.dottel.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

@Component
public class RoleJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleJwtAuthenticationConverter.class);

    // RG-08 (separation des taches) : un utilisateur DOTTEL ne doit porter
    // qu'un seul de ces 5 roles a la fois. Keycloak expose realm_access.roles
    // sous forme de tableau -- rien n'empeche techniquement un cumul cote
    // realm (federation AD). ROLES_DOTTEL restreint la lecture aux 5 roles
    // metier connus, en ignorant les roles techniques Keycloak
    // (offline_access, uma_authorization, default-roles-*, ...).
    private static final Set<String> ROLES_DOTTEL = Set.of("EMPLOYE", "ARH", "CRH", "DRH", "ADMIN");

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> rolesBrutes)) {
            return List.of();
        }

        List<String> rolesDottel = rolesBrutes.stream()
                .map(String::valueOf)
                .filter(ROLES_DOTTEL::contains)
                .distinct()
                .toList();

        if (rolesDottel.isEmpty()) {
            return List.of();
        }

        if (rolesDottel.size() > 1) {
            // Decision actee le 2026-08-03 (Sprint MM.7, etape 6) : un cumul de
            // roles DOTTEL est traite comme une erreur de configuration du realm,
            // jamais comme un cas normal -- cela violerait RG-08. Aucune autorite
            // n'est accordee : chaque @PreAuthorize echoue ensuite avec 403,
            // plutot que de choisir arbitrairement un role ou de cumuler les deux.
            LOGGER.warn("Jeton Keycloak avec plusieurs roles DOTTEL {} pour le sujet {} -- "
                    + "acces refuse, verifier la configuration du realm", rolesDottel, jwt.getSubject());
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + rolesDottel.get(0)));
    }
}
