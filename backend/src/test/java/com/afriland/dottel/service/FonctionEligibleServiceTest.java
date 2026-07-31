package com.afriland.dottel.service;

import com.afriland.dottel.exception.FonctionEligibleBeneficiairesActifsException;
import com.afriland.dottel.exception.FonctionEligibleCodeDejaUtiliseException;
import com.afriland.dottel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.model.dto.fonctioneligible.CreerFonctionEligibleRequestDto;
import com.afriland.dottel.model.dto.fonctioneligible.FonctionEligibleAdminResponseDto;
import com.afriland.dottel.model.dto.fonctioneligible.FonctionEligibleResponseDto;
import com.afriland.dottel.model.dto.fonctioneligible.ModifierFonctionEligibleRequestDto;
import com.afriland.dottel.model.entity.Beneficiaire;
import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.model.entity.GrilleTarifaire;
import com.afriland.dottel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.repository.GrilleTarifaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FonctionEligibleServiceTest {

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private FonctionEligibleService fonctionEligibleService;

    private FonctionEligible creerFonctionEligible(Long id, String code, boolean actif) {
        return FonctionEligible.builder().id(id).code(code).libelle(code).actif(actif).build();
    }

    @Test
    void listerActives_retourneCodeEtLibelleDesFonctionsActives() {
        FonctionEligible da = FonctionEligible.builder().code("DA").libelle("Directeur d'Agence").actif(true).build();
        FonctionEligible gfc = FonctionEligible.builder().code("GFC").libelle("Gestionnaire de Fonds de Commerce").actif(true).build();
        when(fonctionEligibleRepository.findByActifTrueOrderByLibelleAsc()).thenReturn(List.of(da, gfc));

        List<FonctionEligibleResponseDto> resultat = fonctionEligibleService.listerActives();

        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getCode()).isEqualTo("DA");
        assertThat(resultat.get(0).getLibelle()).isEqualTo("Directeur d'Agence");
    }

    @Test
    void listerActives_aucuneFonctionActive_retourneListeVide() {
        when(fonctionEligibleRepository.findByActifTrueOrderByLibelleAsc()).thenReturn(Collections.emptyList());

        List<FonctionEligibleResponseDto> resultat = fonctionEligibleService.listerActives();

        assertThat(resultat).isEmpty();
    }

    @Test
    void creerFonction_casNominal_creeAvecGrilleInitiale() {
        CreerFonctionEligibleRequestDto requete = CreerFonctionEligibleRequestDto.builder()
                .code("CHARGE_INNOVATION").libelle("Chargé Innovation")
                .montantFcfa(40000).dateDebut(LocalDate.of(2026, 8, 1)).build();

        when(fonctionEligibleRepository.findByCode("CHARGE_INNOVATION")).thenReturn(Optional.empty());

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.creer(requete, 1L);

        assertThat(resultat.getCode()).isEqualTo("CHARGE_INNOVATION");
        assertThat(resultat.isActif()).isTrue();
        assertThat(resultat.getNombreBeneficiairesActifs()).isZero();

        verify(fonctionEligibleRepository).save(any(FonctionEligible.class));

        ArgumentCaptor<GrilleTarifaire> grilleCaptor = ArgumentCaptor.forClass(GrilleTarifaire.class);
        verify(grilleTarifaireRepository).save(grilleCaptor.capture());
        assertThat(grilleCaptor.getValue().getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        assertThat(grilleCaptor.getValue().getMontantFcfa()).isEqualTo(40000);

        verify(auditService).enregistrer(eq(1L), eq("CREATION_FONCTION_ELIGIBLE"), eq("fonction_eligible"),
                any(), eq(null), any());
    }

    @Test
    void creerFonction_codeDejaExistant_leve409() {
        CreerFonctionEligibleRequestDto requete = CreerFonctionEligibleRequestDto.builder()
                .code("GFC").libelle("Gestionnaire de Fonds de Commerce")
                .montantFcfa(40000).dateDebut(LocalDate.of(2026, 8, 1)).build();

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC", true)));

        assertThatThrownBy(() -> fonctionEligibleService.creer(requete, 1L))
                .isInstanceOf(FonctionEligibleCodeDejaUtiliseException.class);

        verify(fonctionEligibleRepository, never()).save(any());
        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void desactiverFonction_avecBeneficiairesActifs_autoriseEtTraceLeCompte() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(3L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.desactiver("GFC", 5L);

        assertThat(resultat.isActif()).isFalse();
        assertThat(resultat.getNombreBeneficiairesActifs()).isEqualTo(3L);
        assertThat(fonction.isActif()).isFalse();

        ArgumentCaptor<Map<String, Object>> apresCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditService).enregistrer(eq(5L), eq("DESACTIVATION_FONCTION_ELIGIBLE"), eq("fonction_eligible"),
                eq(1L), any(), apresCaptor.capture());
        assertThat(apresCaptor.getValue()).containsEntry("beneficiairesActifsConcernes", 3L);
    }

    @Test
    void desactiverFonction_sansBeneficiaireActif_desactiveSansBlocage() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(0L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.desactiver("GFC", 5L);

        assertThat(resultat.isActif()).isFalse();
        assertThat(resultat.getNombreBeneficiairesActifs()).isZero();
        verify(fonctionEligibleRepository).save(fonction);
    }

    @Test
    void desactiverFonction_codeInconnu_leve404() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fonctionEligibleService.desactiver("INCONNUE", 5L))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);
    }

    @Test
    void reactiverFonction_casNominal_remetActifATrue() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", false);

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(0L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.reactiver("GFC", 5L);

        assertThat(resultat.isActif()).isTrue();
        assertThat(fonction.isActif()).isTrue();

        ArgumentCaptor<Map<String, Object>> avantCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, Object>> apresCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditService).enregistrer(eq(5L), eq("REACTIVATION_FONCTION_ELIGIBLE"), eq("fonction_eligible"),
                eq(1L), avantCaptor.capture(), apresCaptor.capture());
        assertThat(avantCaptor.getValue()).containsEntry("actif", false);
        assertThat(apresCaptor.getValue()).containsEntry("actif", true);
    }

    @Test
    void reactiverFonction_codeInexistant_leve404() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fonctionEligibleService.reactiver("INCONNUE", 5L))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);

        verify(fonctionEligibleRepository, never()).save(any());
    }

    @Test
    void modifierFonction_libelleSeul_toujoursAutorise() {
        // Constat manuel : le libelle est une donnee d'affichage pure, sans
        // reference ailleurs -- sa modification n'est jamais bloquee, meme si
        // des beneficiaires actifs sont rattaches.
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .libelle("Gestionnaire de Fonds de Commerce (corrige)").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(2L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.modifier("GFC", requete, 5L);

        assertThat(resultat.getLibelle()).isEqualTo("Gestionnaire de Fonds de Commerce (corrige)");
        assertThat(resultat.getCode()).isEqualTo("GFC");
        verify(beneficiaireRepository, never()).findByFonction(any());
    }

    @Test
    void modifierFonction_codeSansBeneficiaireActif_renommeEtCascadeLesInactifs() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        Beneficiaire inactif = Beneficiaire.builder().id(10L).matricule("1847").fonction("GFC").actif(false).build();
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .nouveauCode("GESTIONNAIRE_FDC").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(0L);
        when(fonctionEligibleRepository.findByCode("GESTIONNAIRE_FDC")).thenReturn(Optional.empty());
        when(beneficiaireRepository.findByFonction("GFC")).thenReturn(List.of(inactif));

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.modifier("GFC", requete, 5L);

        assertThat(resultat.getCode()).isEqualTo("GESTIONNAIRE_FDC");
        assertThat(inactif.getFonction()).isEqualTo("GESTIONNAIRE_FDC");
        verify(beneficiaireRepository).saveAll(List.of(inactif));

        ArgumentCaptor<Map<String, Object>> avantCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, Object>> apresCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditService).enregistrer(eq(5L), eq("MODIFICATION_FONCTION_ELIGIBLE"), eq("fonction_eligible"),
                eq(1L), avantCaptor.capture(), apresCaptor.capture());
        assertThat(avantCaptor.getValue()).containsEntry("code", "GFC");
        assertThat(apresCaptor.getValue()).containsEntry("code", "GESTIONNAIRE_FDC");
    }

    @Test
    void modifierFonction_codeAvecBeneficiairesActifs_leve409() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .nouveauCode("GESTIONNAIRE_FDC").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(2L);

        assertThatThrownBy(() -> fonctionEligibleService.modifier("GFC", requete, 5L))
                .isInstanceOf(FonctionEligibleBeneficiairesActifsException.class);

        verify(beneficiaireRepository, never()).findByFonction(any());
        verify(fonctionEligibleRepository, never()).save(any());
    }

    @Test
    void modifierFonction_nouveauCodeDejaUtilise_leve409() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .nouveauCode("DA").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(0L);
        when(fonctionEligibleRepository.findByCode("DA"))
                .thenReturn(Optional.of(creerFonctionEligible(2L, "DA", true)));

        assertThatThrownBy(() -> fonctionEligibleService.modifier("GFC", requete, 5L))
                .isInstanceOf(FonctionEligibleCodeDejaUtiliseException.class);

        verify(beneficiaireRepository, never()).findByFonction(any());
    }

    @Test
    void modifierFonction_codeInconnu_leve404() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .libelle("Peu importe").build();

        assertThatThrownBy(() -> fonctionEligibleService.modifier("INCONNUE", requete, 5L))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);
    }
}