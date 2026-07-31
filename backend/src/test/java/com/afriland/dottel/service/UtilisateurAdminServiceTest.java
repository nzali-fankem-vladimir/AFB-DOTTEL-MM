package com.afriland.dottel.service;

import com.afriland.dottel.exception.ActionAdminNonAutoriseeException;
import com.afriland.dottel.exception.EmailUtilisateurDejaUtiliseException;
import com.afriland.dottel.exception.MatriculeUtilisateurDejaUtiliseException;
import com.afriland.dottel.exception.RoleInvalideException;
import com.afriland.dottel.exception.UtilisateurIntrouvableException;
import com.afriland.dottel.model.dto.utilisateur.CreerUtilisateurRequestDto;
import com.afriland.dottel.model.dto.utilisateur.UtilisateurResponseDto;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.RoleEnum;
import com.afriland.dottel.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurAdminServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    private UtilisateurAdminService utilisateurAdminService;

    @BeforeEach
    void setUp() {
        utilisateurAdminService = new UtilisateurAdminService(utilisateurRepository, passwordEncoder, auditService);
    }

    private Utilisateur creerUtilisateur(Long id, String matricule, String nom, String prenom,
                                          String email, RoleEnum role, boolean actif) {
        return Utilisateur.builder()
                .id(id)
                .matricule(matricule)
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .role(role)
                .motDePasseHash("hash-existant")
                .actif(actif)
                .dateCreation(LocalDateTime.of(2026, 7, 20, 8, 0))
                .build();
    }

    @Test
    void rechercher_sansFiltre_retourneTousLesUtilisateurs() {
        Utilisateur ndongo = creerUtilisateur(1L, "4410", "NDONGO", "Patrice", "p.ndongo@afriland.cm",
                RoleEnum.ARH, true);
        Utilisateur atangana = creerUtilisateur(2L, "4411", "ATANGANA", "Solange", "s.atangana@afriland.cm",
                RoleEnum.DRH, true);
        when(utilisateurRepository.findAll()).thenReturn(List.of(ndongo, atangana));

        List<UtilisateurResponseDto> resultat = utilisateurAdminService.rechercher(null, null);

        assertThat(resultat).hasSize(2);
        assertThat(resultat).extracting(UtilisateurResponseDto::getMatricule)
                .containsExactlyInAnyOrder("4410", "4411");
    }

    @Test
    void rechercher_filtreParRole_retourneUniquementCeRole() {
        Utilisateur ndongo = creerUtilisateur(1L, "4410", "NDONGO", "Patrice", "p.ndongo@afriland.cm",
                RoleEnum.ARH, true);
        Utilisateur atangana = creerUtilisateur(2L, "4411", "ATANGANA", "Solange", "s.atangana@afriland.cm",
                RoleEnum.DRH, true);
        when(utilisateurRepository.findAll()).thenReturn(List.of(ndongo, atangana));

        List<UtilisateurResponseDto> resultat = utilisateurAdminService.rechercher(RoleEnum.DRH, null);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getMatricule()).isEqualTo("4411");
    }

    @Test
    void creer_casNominal_creeLeCompteActif() {
        CreerUtilisateurRequestDto requete = CreerUtilisateurRequestDto.builder()
                .matricule("5521")
                .nom("BELINGA")
                .prenom("Christelle")
                .email("c.belinga@afriland.cm")
                .role(RoleEnum.CRH)
                .motDePasse("Test1234")
                .build();

        when(utilisateurRepository.findByMatricule("5521")).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail("c.belinga@afriland.cm")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test1234")).thenReturn("hash-bcrypt");

        UtilisateurResponseDto reponse = utilisateurAdminService.creer(requete, 9L);

        assertThat(reponse.getMatricule()).isEqualTo("5521");
        assertThat(reponse.getNomPrenoms()).isEqualTo("BELINGA Christelle");
        assertThat(reponse.isActif()).isTrue();

        ArgumentCaptor<Utilisateur> captureur = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captureur.capture());
        assertThat(captureur.getValue().getMotDePasseHash()).isEqualTo("hash-bcrypt");
        assertThat(captureur.getValue().isActif()).isTrue();
    }

    @Test
    void creer_matriculeDejaUtilise_leve409() {
        CreerUtilisateurRequestDto requete = CreerUtilisateurRequestDto.builder()
                .matricule("5521")
                .nom("BELINGA")
                .prenom("Christelle")
                .email("c.belinga@afriland.cm")
                .role(RoleEnum.CRH)
                .motDePasse("Test1234")
                .build();

        when(utilisateurRepository.findByMatricule("5521"))
                .thenReturn(Optional.of(creerUtilisateur(3L, "5521", "FOUDA", "Armand",
                        "a.fouda@afriland.cm", RoleEnum.ARH, true)));

        assertThatThrownBy(() -> utilisateurAdminService.creer(requete, 9L))
                .isInstanceOf(MatriculeUtilisateurDejaUtiliseException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void creer_emailDejaUtilise_leve409() {
        CreerUtilisateurRequestDto requete = CreerUtilisateurRequestDto.builder()
                .matricule("5521")
                .nom("BELINGA")
                .prenom("Christelle")
                .email("c.belinga@afriland.cm")
                .role(RoleEnum.CRH)
                .motDePasse("Test1234")
                .build();

        when(utilisateurRepository.findByMatricule("5521")).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail("c.belinga@afriland.cm"))
                .thenReturn(Optional.of(creerUtilisateur(4L, "5599", "EYENGA", "Marcelline",
                        "c.belinga@afriland.cm", RoleEnum.CRH, true)));

        assertThatThrownBy(() -> utilisateurAdminService.creer(requete, 9L))
                .isInstanceOf(EmailUtilisateurDejaUtiliseException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void creer_motDePasseJamaisEnClairDansLAudit() {
        CreerUtilisateurRequestDto requete = CreerUtilisateurRequestDto.builder()
                .matricule("5521")
                .nom("BELINGA")
                .prenom("Christelle")
                .email("c.belinga@afriland.cm")
                .role(RoleEnum.CRH)
                .motDePasse("Test1234")
                .build();

        when(utilisateurRepository.findByMatricule("5521")).thenReturn(Optional.empty());
        when(utilisateurRepository.findByEmail("c.belinga@afriland.cm")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Test1234")).thenReturn("hash-bcrypt");

        utilisateurAdminService.creer(requete, 9L);

        ArgumentCaptor<Map<String, Object>> apresCaptureur = ArgumentCaptor.forClass(Map.class);
        verify(auditService).enregistrer(eq(9L), eq("CREATION_UTILISATEUR"), eq("utilisateurs"),
                isNull(), isNull(), apresCaptureur.capture());

        Map<String, Object> apres = apresCaptureur.getValue();
        assertThat(apres).doesNotContainKey("motDePasse");
        assertThat(apres).doesNotContainKey("motDePasseHash");
        assertThat(apres.values()).noneMatch(valeur -> "Test1234".equals(valeur) || "hash-bcrypt".equals(valeur));
    }

    @Test
    void changerStatut_casNominal_metAJourEtAudit() {
        Utilisateur tchinda = creerUtilisateur(5L, "6602", "TCHINDA", "Bertrand", "b.tchinda@afriland.cm",
                RoleEnum.CRH, true);
        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(tchinda));

        UtilisateurResponseDto reponse = utilisateurAdminService.changerStatut(5L, false, 9L);

        assertThat(reponse.isActif()).isFalse();
        verify(auditService).enregistrer(eq(9L), eq("CHANGEMENT_STATUT_UTILISATEUR"), eq("utilisateurs"),
                eq(5L), eq(Map.of("actif", true)), eq(Map.of("actif", false)));
    }

    @Test
    void changerStatut_utilisateurIntrouvable_leve404() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurAdminService.changerStatut(99L, false, 9L))
                .isInstanceOf(UtilisateurIntrouvableException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void changerStatut_autoDesactivation_leve409() {
        Utilisateur ndongo = creerUtilisateur(9L, "4410", "NDONGO", "Patrice", "p.ndongo@afriland.cm",
                RoleEnum.ADMIN, true);
        when(utilisateurRepository.findById(9L)).thenReturn(Optional.of(ndongo));

        assertThatThrownBy(() -> utilisateurAdminService.changerStatut(9L, false, 9L))
                .isInstanceOf(ActionAdminNonAutoriseeException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void changerStatut_dernierAdminActif_leve409() {
        Utilisateur atangana = creerUtilisateur(2L, "4411", "ATANGANA", "Solange", "s.atangana@afriland.cm",
                RoleEnum.ADMIN, true);
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(atangana));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.ADMIN)).thenReturn(List.of(atangana));

        assertThatThrownBy(() -> utilisateurAdminService.changerStatut(2L, false, 9L))
                .isInstanceOf(ActionAdminNonAutoriseeException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void changerRole_casNominal_metAJourEtAudit() {
        Utilisateur fouda = creerUtilisateur(3L, "5521", "FOUDA", "Armand", "a.fouda@afriland.cm",
                RoleEnum.EMPLOYE, true);
        when(utilisateurRepository.findById(3L)).thenReturn(Optional.of(fouda));

        UtilisateurResponseDto reponse = utilisateurAdminService.changerRole(3L, "CRH", 9L);

        assertThat(reponse.getRole()).isEqualTo(RoleEnum.CRH);
        verify(auditService).enregistrer(eq(9L), eq("CHANGEMENT_ROLE_UTILISATEUR"), eq("utilisateurs"),
                eq(3L), eq(Map.of("role", "EMPLOYE")), eq(Map.of("role", "CRH")));
    }

    @Test
    void changerRole_roleInvalide_leve400() {
        assertThatThrownBy(() -> utilisateurAdminService.changerRole(3L, "SUPER_ADMIN", 9L))
                .isInstanceOf(RoleInvalideException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void changerRole_autoRetrogradation_leve409() {
        Utilisateur ndongo = creerUtilisateur(9L, "4410", "NDONGO", "Patrice", "p.ndongo@afriland.cm",
                RoleEnum.ADMIN, true);
        when(utilisateurRepository.findById(9L)).thenReturn(Optional.of(ndongo));

        assertThatThrownBy(() -> utilisateurAdminService.changerRole(9L, "DRH", 9L))
                .isInstanceOf(ActionAdminNonAutoriseeException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void changerRole_dernierAdminActif_leve409() {
        Utilisateur atangana = creerUtilisateur(2L, "4411", "ATANGANA", "Solange", "s.atangana@afriland.cm",
                RoleEnum.ADMIN, true);
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(atangana));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.ADMIN)).thenReturn(List.of(atangana));

        assertThatThrownBy(() -> utilisateurAdminService.changerRole(2L, "DRH", 9L))
                .isInstanceOf(ActionAdminNonAutoriseeException.class);

        verify(utilisateurRepository, never()).save(any());
    }
}