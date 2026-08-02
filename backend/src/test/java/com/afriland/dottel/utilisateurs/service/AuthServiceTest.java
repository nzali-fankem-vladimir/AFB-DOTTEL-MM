package com.afriland.dottel.utilisateurs.service;
import com.afriland.dottel.utilisateurs.service.AuthService;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.utilisateurs.exception.IdentifiantsInvalidesException;
import com.afriland.dottel.utilisateurs.exception.UtilisateurInactifException;
import com.afriland.dottel.utilisateurs.model.dto.auth.LoginRequestDto;
import com.afriland.dottel.utilisateurs.model.dto.auth.LoginResponseDto;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import com.afriland.dottel.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDto requeteLogin;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        requeteLogin = new LoginRequestDto();
        requeteLogin.setMatricule("1562");
        requeteLogin.setMotDePasse("MotDePasse123!");

        utilisateur = Utilisateur.builder()
                .id(1L)
                .matricule("1562")
                .nom("ATANGANA")
                .prenom("Paul")
                .email("paul.atangana@afrilandfirstbank.com")
                .role(RoleEnum.ARH)
                .motDePasseHash("hashBcrypt")
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .build();
    }

    @Test
    void authentifier_casNominal_retourneLoginResponseAvecToken() {
        when(utilisateurRepository.findByMatricule("1562")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("MotDePasse123!", "hashBcrypt")).thenReturn(true);
        when(jwtUtil.genererToken(utilisateur)).thenReturn("token.jwt.simule");

        LoginResponseDto reponse = authService.authentifier(requeteLogin);

        assertThat(reponse.getToken()).isEqualTo("token.jwt.simule");
        assertThat(reponse.getMatricule()).isEqualTo("1562");
        assertThat(reponse.getRole()).isEqualTo("ARH");
        assertThat(reponse.getNom()).isEqualTo("ATANGANA");
        assertThat(reponse.getPrenom()).isEqualTo("Paul");
        verify(auditService).enregistrer(1L, "CONNEXION", "utilisateurs", 1L, null, null);
    }

    @Test
    void authentifier_matriculeInconnu_leveIdentifiantsInvalidesExceptionSansAudit() {
        when(utilisateurRepository.findByMatricule("1562")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authentifier(requeteLogin))
                .isInstanceOf(IdentifiantsInvalidesException.class);
        // Aucun utilisateur trouve = aucun id valide pour audit_log.id_utilisateur
        // (NOT NULL + FK) -- ce cas ne peut pas etre trace.
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
    }

    @Test
    void authentifier_motDePasseIncorrect_leveIdentifiantsInvalidesExceptionEtTraceEchec() {
        when(utilisateurRepository.findByMatricule("1562")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("MotDePasse123!", "hashBcrypt")).thenReturn(false);

        assertThatThrownBy(() -> authService.authentifier(requeteLogin))
                .isInstanceOf(IdentifiantsInvalidesException.class);
        verify(auditService).enregistrer(1L, "CONNEXION_ECHOUEE", "utilisateurs", 1L, null,
                Map.of("matriculeTente", "1562", "motifEchec", "MOT_DE_PASSE_INCORRECT"));
    }

    @Test
    void authentifier_utilisateurInactif_leveUtilisateurInactifExceptionEtTraceEchec() {
        utilisateur.setActif(false);
        when(utilisateurRepository.findByMatricule("1562")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("MotDePasse123!", "hashBcrypt")).thenReturn(true);

        assertThatThrownBy(() -> authService.authentifier(requeteLogin))
                .isInstanceOf(UtilisateurInactifException.class);
        verify(auditService).enregistrer(1L, "CONNEXION_ECHOUEE", "utilisateurs", 1L, null,
                Map.of("matriculeTente", "1562", "motifEchec", "COMPTE_INACTIF"));
    }
}