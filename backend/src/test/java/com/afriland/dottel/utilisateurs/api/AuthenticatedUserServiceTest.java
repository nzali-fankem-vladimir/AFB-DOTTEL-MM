package com.afriland.dottel.utilisateurs.api;

import com.afriland.dottel.utilisateurs.exception.UtilisateurConnecteIntrouvableException;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private AuthenticatedUserService authenticatedUserService;

    @AfterEach
    void nettoyerContexteSecurite() {
        SecurityContextHolder.clearContext();
    }

    private void authentifierAvec(String email) {
        Jwt.Builder builder = Jwt.withTokenValue("token.jwt.simule")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("3f2504e0-4f89-11d3-9a0c-0305e82c3301");
        if (email != null) {
            builder.claim("email", email);
        }
        Jwt jwt = builder.build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @Test
    void utilisateurCourant_casNominal_resoutParEmail() {
        authentifierAvec("paul_atangana@afrilandfirstbank.com");
        Utilisateur utilisateur = Utilisateur.builder()
                .id(1L)
                .matricule("1562")
                .nom("ATANGANA")
                .prenom("Paul")
                .email("paul_atangana@afrilandfirstbank.com")
                .role(RoleEnum.DRH)
                .motDePasseHash("hashBcrypt")
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .build();
        when(utilisateurRepository.findByEmail("paul_atangana@afrilandfirstbank.com"))
                .thenReturn(Optional.of(utilisateur));

        Utilisateur resultat = authenticatedUserService.utilisateurCourant();

        assertThat(resultat.getMatricule()).isEqualTo("1562");
        assertThat(resultat.getRole()).isEqualTo(RoleEnum.DRH);
    }

    @Test
    void utilisateurCourant_emailInconnuEnBase_leveUtilisateurConnecteIntrouvableException() {
        authentifierAvec("inconnu@afrilandfirstbank.com");
        when(utilisateurRepository.findByEmail("inconnu@afrilandfirstbank.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticatedUserService.utilisateurCourant())
                .isInstanceOf(UtilisateurConnecteIntrouvableException.class);
    }

    @Test
    void utilisateurCourant_emailAbsentDuJeton_leveUtilisateurConnecteIntrouvableException() {
        authentifierAvec(null);
        when(utilisateurRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticatedUserService.utilisateurCourant())
                .isInstanceOf(UtilisateurConnecteIntrouvableException.class);
    }
}
