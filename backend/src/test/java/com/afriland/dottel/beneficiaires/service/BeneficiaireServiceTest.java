package com.afriland.dottel.beneficiaires.service;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.audit.api.EvenementAudit;

import com.afriland.dottel.beneficiaires.exception.BeneficiaireIntrouvableException;
import com.afriland.dottel.beneficiaires.exception.NonEligibleException;
import com.afriland.dottel.beneficiaires.model.dto.beneficiaire.BeneficiaireResponseDto;
import com.afriland.dottel.beneficiaires.model.dto.beneficiaire.ModifierBeneficiaireRequestDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.referentiel.api.GrilleTarifaireApi;
import com.afriland.dottel.referentiel.api.ResolutionGrilleDto;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.referentiel.service.EligibiliteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaireServiceTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private GrilleTarifaireApi grilleTarifaireApi;

    @Mock
    private EligibiliteService eligibiliteService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private BeneficiaireService beneficiaireService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        beneficiaireService = new BeneficiaireService(
                beneficiaireRepository, grilleTarifaireApi,
                eligibiliteService, eventPublisher, authenticatedUserService);
    }

    private Beneficiaire creerBeneficiaire(String matricule, String nomPrenoms, String fonction, boolean actif) {
        return Beneficiaire.builder()
                .id(1L)
                .matricule(matricule)
                .nomPrenoms(nomPrenoms)
                .fonction(fonction)
                .uniteRattachement("Agence Bastos")
                .codeUnite("BAS")
                .numCompteCourant("11012345678901")
                .dateEnrolement(LocalDate.of(2026, 1, 15))
                .actif(actif)
                .build();
    }

    private Utilisateur creerUtilisateurArh() {
        return Utilisateur.builder().id(9L).matricule("1042").nom("ONANA").prenom("Serge").build();
    }

    @Test
    void modifier_changementFonction_recalculeLeMontant() {
        Beneficiaire pierre = creerBeneficiaire("2097", "Pierre TCHINDA", "CONSEILLER", true);
        ModifierBeneficiaireRequestDto requete = ModifierBeneficiaireRequestDto.builder().fonction("DA").build();

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(pierre));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(grilleTarifaireApi.resoudrePourFonction("DA")).thenReturn(ResolutionGrilleDto.resolue(50000));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(creerUtilisateurArh());

        BeneficiaireResponseDto resultat = beneficiaireService.modifier(1L, requete);

        assertThat(resultat.getFonction()).isEqualTo("DA");
        assertThat(resultat.getMontantCourant()).isEqualTo(50000);
    }

    @Test
    void modifier_beneficiaireIntrouvable_leve404() {
        ModifierBeneficiaireRequestDto requete = ModifierBeneficiaireRequestDto.builder().fonction("DA").build();

        when(beneficiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaireService.modifier(99L, requete))
                .isInstanceOf(BeneficiaireIntrouvableException.class);
    }

    @Test
    void modifier_modificationPartielle_neTouchePasLesAutresChamps() {
        Beneficiaire marie = creerBeneficiaire("2098", "Marie FOUDA", "GFC", true);
        marie.setGrade("CADRE");
        marie.setUniteRattachement("Agence Bonanjo");
        ModifierBeneficiaireRequestDto requete = ModifierBeneficiaireRequestDto.builder()
                .uniteRattachement("Agence Akwa").build();

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(marie));
        when(grilleTarifaireApi.resoudrePourFonction("GFC")).thenReturn(ResolutionGrilleDto.resolue(40000));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(creerUtilisateurArh());

        BeneficiaireResponseDto resultat = beneficiaireService.modifier(1L, requete);

        assertThat(resultat.getFonction()).isEqualTo("GFC");
        assertThat(marie.getGrade()).isEqualTo("CADRE");
        assertThat(marie.getUniteRattachement()).isEqualTo("Agence Akwa");
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
    }

    @Test
    void modifier_auditAvecDeltaAvantEtApres_verifieLesDeuxCotes() {
        Beneficiaire jean = creerBeneficiaire("2099", "Jean ESSAMA", "CONSEILLER", true);
        jean.setUniteRattachement("Agence Bafoussam");
        ModifierBeneficiaireRequestDto requete = ModifierBeneficiaireRequestDto.builder()
                .uniteRattachement("Agence Douala Bali").build();

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(jean));
        when(grilleTarifaireApi.resoudrePourFonction("CONSEILLER")).thenReturn(ResolutionGrilleDto.resolue(50000));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(creerUtilisateurArh());

        beneficiaireService.modifier(1L, requete);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(9L);
        assertThat(evenement.action()).isEqualTo("MODIFICATION_BENEFICIAIRE");
        assertThat(evenement.entiteCible()).isEqualTo("beneficiaires");
        assertThat(evenement.idEntite()).isEqualTo(1L);
        assertThat(evenement.avant()).containsEntry("uniteRattachement", "Agence Bafoussam");
        assertThat(evenement.apres()).containsEntry("uniteRattachement", "Agence Douala Bali");
    }

    @Test
    void modifier_fonctionNonEligible_leveException() {
        Beneficiaire sylvie = creerBeneficiaire("2100", "Sylvie NKOLO", "DA", true);
        sylvie.setGrade("NON GRADE");
        ModifierBeneficiaireRequestDto requete = ModifierBeneficiaireRequestDto.builder()
                .fonction("CORPS_CONTROLE_IG").build();

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(sylvie));
        when(eligibiliteService.verifierEligibilite("CORPS_CONTROLE_IG", "NON GRADE")).thenReturn(false);

        assertThatThrownBy(() -> beneficiaireService.modifier(1L, requete))
                .isInstanceOf(NonEligibleException.class);

        verify(beneficiaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void modifier_gradeEnvoyeDansLaRequete_estIgnoreEtNaAucunImpact() {
        Beneficiaire marie = creerBeneficiaire("2101", "Marie FOUDA", "GFC", true);
        marie.setGrade("CADRE");
        ModifierBeneficiaireRequestDto requete = ModifierBeneficiaireRequestDto.builder()
                .grade("DIRECTEUR").build();

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(marie));
        when(grilleTarifaireApi.resoudrePourFonction("GFC")).thenReturn(ResolutionGrilleDto.resolue(40000));

        beneficiaireService.modifier(1L, requete);

        assertThat(marie.getGrade()).isEqualTo("CADRE");
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void rechercher_sansFiltre_retourneTousLesActifs() {
        Beneficiaire sylvie = creerBeneficiaire("2093", "Sylvie NKOLO", "GFC", true);
        Beneficiaire jean = creerBeneficiaire("2094", "Jean ESSAMA", "COMPTABLE", true);
        Pageable pageable = PageRequest.of(0, 20);

        when(beneficiaireRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(sylvie, jean), pageable, 2));
        when(grilleTarifaireApi.resoudrePourFonction("GFC")).thenReturn(ResolutionGrilleDto.resolue(40000));
        when(grilleTarifaireApi.resoudrePourFonction("COMPTABLE")).thenReturn(ResolutionGrilleDto.resolue(35000));

        Page<BeneficiaireResponseDto> resultat = beneficiaireService.rechercher(null, null, null, pageable);

        assertThat(resultat.getContent()).hasSize(2);
        assertThat(resultat.getContent()).extracting(BeneficiaireResponseDto::getMatricule)
                .containsExactly("2093", "2094");
    }

    @Test
    void rechercher_filtreParFonction_retourneUniquementCetteFonction() {
        Beneficiaire sylvie = creerBeneficiaire("2093", "Sylvie NKOLO", "GFC", true);
        Pageable pageable = PageRequest.of(0, 20);

        when(beneficiaireRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(sylvie), pageable, 1));
        when(grilleTarifaireApi.resoudrePourFonction("GFC")).thenReturn(ResolutionGrilleDto.resolue(40000));

        Page<BeneficiaireResponseDto> resultat = beneficiaireService.rechercher("GFC", null, null, pageable);

        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().get(0).getFonction()).isEqualTo("GFC");
    }

    @Test
    void rechercher_beneficiaireSansGrilleActive_montantCourantNull() {
        Beneficiaire pierre = creerBeneficiaire("2095", "Pierre TCHINDA", "CONSEILLER", true);
        Pageable pageable = PageRequest.of(0, 20);

        when(beneficiaireRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(pierre), pageable, 1));
        when(grilleTarifaireApi.resoudrePourFonction("CONSEILLER"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE));

        Page<BeneficiaireResponseDto> resultat = beneficiaireService.rechercher(null, null, null, pageable);

        assertThat(resultat.getContent().get(0).getMontantCourant()).isNull();
    }

    // Regression RG-01 (decouverte lors de l'unification C2, sprint MM.3) :
    // l'ancienne implementation de BeneficiaireService.resoudreMontantCourant()
    // ignorait FonctionEligible.actif et ne regardait que l'existence d'une grille
    // ACTIVE. Une fonction desactivee dont la grille serait restee ACTIVE (aucune
    // dateFin renseignee) remontait alors un montant, en contradiction avec RG-01.
    // L'unification vers GrilleTarifaireApi corrige ce point : le referentiel
    // verifie actif avant d'interroger la grille (voir GrilleTarifaireApiImplTest),
    // et BeneficiaireService doit desormais se contenter de propager sa decision.
    @Test
    void resoudreMontantCourant_fonctionDesactiveeMemeAvecGrilleRestéeActive_montantCourantNull() {
        when(grilleTarifaireApi.resoudrePourFonction("COMPTABLE"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_FONCTION_DESACTIVEE));

        Integer montant = beneficiaireService.resoudreMontantCourant("COMPTABLE");

        assertThat(montant).isNull();
    }

    @Test
    void desactiver_casNominal_metActifAFalse() {
        Beneficiaire sylvie = creerBeneficiaire("2093", "Sylvie NKOLO", "GFC", true);

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(sylvie));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(creerUtilisateurArh());

        beneficiaireService.desactiver(1L);

        assertThat(sylvie.isActif()).isFalse();
        verify(beneficiaireRepository).save(sylvie);
    }

    @Test
    void desactiver_beneficiaireIntrouvable_leve404() {
        when(beneficiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaireService.desactiver(99L))
                .isInstanceOf(BeneficiaireIntrouvableException.class);

        verify(beneficiaireRepository, never()).save(any());
    }

    @Test
    void desactiver_appelleAuditAvecDelta() {
        Beneficiaire jean = creerBeneficiaire("2094", "Jean ESSAMA", "COMPTABLE", true);

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(jean));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(creerUtilisateurArh());

        beneficiaireService.desactiver(1L);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(9L);
        assertThat(evenement.action()).isEqualTo("DESACTIVATION_BENEFICIAIRE");
        assertThat(evenement.entiteCible()).isEqualTo("beneficiaires");
        assertThat(evenement.idEntite()).isEqualTo(1L);

        assertThat(evenement.avant()).containsEntry("actif", true);
        assertThat(evenement.apres()).containsEntry("actif", false);
    }

    @Test
    void reactiver_casNominal_metActifATrue() {
        Beneficiaire sylvie = creerBeneficiaire("2093", "Sylvie NKOLO", "GFC", false);

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(sylvie));
        when(eligibiliteService.verifierEligibilite("GFC", sylvie.getGrade())).thenReturn(true);
        when(grilleTarifaireApi.resoudrePourFonction("GFC")).thenReturn(ResolutionGrilleDto.resolue(40000));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(creerUtilisateurArh());

        beneficiaireService.reactiver(1L);

        assertThat(sylvie.isActif()).isTrue();
        verify(beneficiaireRepository).save(sylvie);
    }

    @Test
    void reactiver_beneficiaireIntrouvable_leve404() {
        when(beneficiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> beneficiaireService.reactiver(99L))
                .isInstanceOf(BeneficiaireIntrouvableException.class);

        verify(beneficiaireRepository, never()).save(any());
    }

    @Test
    void reactiver_fonctionDevenueNonEligible_leve403SansReactiver() {
        Beneficiaire jean = creerBeneficiaire("2094", "Jean ESSAMA", "COMPTABLE", false);
        jean.setGrade("NON GRADE");

        when(beneficiaireRepository.findById(1L)).thenReturn(Optional.of(jean));
        when(eligibiliteService.verifierEligibilite("COMPTABLE", "NON GRADE")).thenReturn(false);

        assertThatThrownBy(() -> beneficiaireService.reactiver(1L))
                .isInstanceOf(NonEligibleException.class);

        assertThat(jean.isActif()).isFalse();
        verify(beneficiaireRepository, never()).save(any());
    }

    @Test
    void rechercher_pagination_respecteTailleEtPage() {
        Beneficiaire marie = creerBeneficiaire("2096", "Marie FOUDA", "ATTACHE_COMMERCIAL", true);
        Pageable pageable = PageRequest.of(1, 5);

        when(beneficiaireRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(marie), pageable, 6));
        when(grilleTarifaireApi.resoudrePourFonction("ATTACHE_COMMERCIAL"))
                .thenReturn(ResolutionGrilleDto.resolue(30000));

        Page<BeneficiaireResponseDto> resultat = beneficiaireService.rechercher(null, null, null, pageable);

        assertThat(resultat.getNumber()).isEqualTo(1);
        assertThat(resultat.getSize()).isEqualTo(5);
        assertThat(resultat.getTotalElements()).isEqualTo(6);
    }
}