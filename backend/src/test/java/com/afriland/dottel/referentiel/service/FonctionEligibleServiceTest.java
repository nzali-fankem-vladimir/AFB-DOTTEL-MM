package com.afriland.dottel.referentiel.service;
import com.afriland.dottel.audit.api.EvenementAudit;

import com.afriland.dottel.referentiel.exception.FonctionEligibleBeneficiairesActifsException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleCodeDejaUtiliseException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.CreerFonctionEligibleRequestDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.FonctionEligibleAdminResponseDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.FonctionEligibleResponseDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.ModifierFonctionEligibleRequestDto;
import com.afriland.dottel.beneficiaires.api.BeneficiaireApi;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private BeneficiaireApi beneficiaireApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FonctionEligibleService fonctionEligibleService;

    private FonctionEligible creerFonctionEligible(Long id, String code, boolean actif) {
        return FonctionEligible.builder().id(id).code(code).libelle(code).actif(actif).build();
    }

    // Sprint MM.3, couplage C2 : lecture seule utilisee par
    // EnrolementService.verifier() sans injecter FonctionEligibleRepository.
    @Test
    void libelle_fonctionConnue_retourneLeLibelle() {
        when(fonctionEligibleRepository.findByCode("DA"))
                .thenReturn(Optional.of(creerFonctionEligible(5L, "DA", true)));

        Optional<String> libelle = fonctionEligibleService.libelle("DA");

        assertThat(libelle).contains("DA");
    }

    @Test
    void libelle_fonctionInconnue_retourneOptionalVide() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        Optional<String> libelle = fonctionEligibleService.libelle("INCONNUE");

        assertThat(libelle).isEmpty();
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

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(1L);
        assertThat(evenement.action()).isEqualTo("CREATION_FONCTION_ELIGIBLE");
        assertThat(evenement.entiteCible()).isEqualTo("fonction_eligible");
        assertThat(evenement.avant()).isNull();
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
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(3L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.desactiver("GFC", 5L);

        assertThat(resultat.isActif()).isFalse();
        assertThat(resultat.getNombreBeneficiairesActifs()).isEqualTo(3L);
        assertThat(fonction.isActif()).isFalse();

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(5L);
        assertThat(evenement.action()).isEqualTo("DESACTIVATION_FONCTION_ELIGIBLE");
        assertThat(evenement.entiteCible()).isEqualTo("fonction_eligible");
        assertThat(evenement.idEntite()).isEqualTo(1L);
        assertThat(evenement.apres()).containsEntry("beneficiairesActifsConcernes", 3L);
    }

    @Test
    void desactiverFonction_sansBeneficiaireActif_desactiveSansBlocage() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(0L);

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
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(0L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.reactiver("GFC", 5L);

        assertThat(resultat.isActif()).isTrue();
        assertThat(fonction.isActif()).isTrue();

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(5L);
        assertThat(evenement.action()).isEqualTo("REACTIVATION_FONCTION_ELIGIBLE");
        assertThat(evenement.entiteCible()).isEqualTo("fonction_eligible");
        assertThat(evenement.idEntite()).isEqualTo(1L);
        assertThat(evenement.avant()).containsEntry("actif", false);
        assertThat(evenement.apres()).containsEntry("actif", true);
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
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(2L);

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.modifier("GFC", requete, 5L);

        assertThat(resultat.getLibelle()).isEqualTo("Gestionnaire de Fonds de Commerce (corrige)");
        assertThat(resultat.getCode()).isEqualTo("GFC");
        verify(beneficiaireApi, never()).renommerFonction(any(), any());
    }

    // Couplage C3 (Sprint MM.3) : la cascade elle-meme (renommer les Beneficiaire
    // rattaches, y compris inactifs) est desormais testee cote beneficiaires
    // (BeneficiaireApiImpl). Ce test verifie uniquement que FonctionEligibleService
    // delegue au bon appel, avec les bons codes, sans injecter BeneficiaireRepository.
    @Test
    void modifierFonction_codeSansBeneficiaireActif_delegueLaCascadeAuModuleBeneficiaires() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .nouveauCode("GESTIONNAIRE_FDC").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(0L);
        when(fonctionEligibleRepository.findByCode("GESTIONNAIRE_FDC")).thenReturn(Optional.empty());

        FonctionEligibleAdminResponseDto resultat = fonctionEligibleService.modifier("GFC", requete, 5L);

        assertThat(resultat.getCode()).isEqualTo("GESTIONNAIRE_FDC");
        verify(beneficiaireApi).renommerFonction("GFC", "GESTIONNAIRE_FDC");

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(5L);
        assertThat(evenement.action()).isEqualTo("MODIFICATION_FONCTION_ELIGIBLE");
        assertThat(evenement.entiteCible()).isEqualTo("fonction_eligible");
        assertThat(evenement.idEntite()).isEqualTo(1L);
        assertThat(evenement.avant()).containsEntry("code", "GFC");
        assertThat(evenement.apres()).containsEntry("code", "GESTIONNAIRE_FDC");
    }

    @Test
    void modifierFonction_codeAvecBeneficiairesActifs_leve409() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .nouveauCode("GESTIONNAIRE_FDC").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(2L);

        assertThatThrownBy(() -> fonctionEligibleService.modifier("GFC", requete, 5L))
                .isInstanceOf(FonctionEligibleBeneficiairesActifsException.class);

        verify(beneficiaireApi, never()).renommerFonction(any(), any());
        verify(fonctionEligibleRepository, never()).save(any());
    }

    @Test
    void modifierFonction_nouveauCodeDejaUtilise_leve409() {
        FonctionEligible fonction = creerFonctionEligible(1L, "GFC", true);
        ModifierFonctionEligibleRequestDto requete = ModifierFonctionEligibleRequestDto.builder()
                .nouveauCode("DA").build();

        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(fonction));
        when(beneficiaireApi.compterActifsParFonction("GFC")).thenReturn(0L);
        when(fonctionEligibleRepository.findByCode("DA"))
                .thenReturn(Optional.of(creerFonctionEligible(2L, "DA", true)));

        assertThatThrownBy(() -> fonctionEligibleService.modifier("GFC", requete, 5L))
                .isInstanceOf(FonctionEligibleCodeDejaUtiliseException.class);

        verify(beneficiaireApi, never()).renommerFonction(any(), any());
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