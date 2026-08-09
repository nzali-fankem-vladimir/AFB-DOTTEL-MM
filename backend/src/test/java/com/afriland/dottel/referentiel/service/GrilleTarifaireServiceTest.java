package com.afriland.dottel.referentiel.service;
import com.afriland.dottel.audit.api.EvenementAudit;

import com.afriland.dottel.referentiel.exception.DateDebutGrilleAnterieureException;
import com.afriland.dottel.referentiel.exception.DecisionGrilleInvalideException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleEnAttenteDrhExistanteException;
import com.afriland.dottel.referentiel.exception.GrilleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleNonActiveException;
import com.afriland.dottel.referentiel.exception.GrilleNonEnAttenteDrhException;
import com.afriland.dottel.referentiel.exception.GrilleNonModifiableException;
import com.afriland.dottel.referentiel.exception.GrilleTarifaireMotifRejetObligatoireException;
import com.afriland.dottel.referentiel.exception.RoleDecisionGrilleNonAutoriseException;
import com.afriland.dottel.referentiel.exception.SeparationTachesGrilleViolationException;
import com.afriland.dottel.referentiel.model.dto.grille.CreerGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.DecisionGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireListeLigneDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.HistoriqueGrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.ModifierGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrilleTarifaireServiceTest {

    // Acteurs du workflow a trois acteurs (Sprint MM.12). ARH_CREATEUR porte
    // l'id 9L, celui utilise comme idCreateur par les fabriques de grille
    // ci-dessous : c'est ce qui permet aux tests RG-08 de rejouer le scenario
    // reel (creation en ARH, puis promotion du MEME compte en CRH).
    private static final Utilisateur ARH_CREATEUR = acteur(9L, "MBARGA", RoleEnum.ARH);
    private static final Utilisateur CRH = acteur(12L, "ESSAMA", RoleEnum.CRH);
    private static final Utilisateur DRH = acteur(15L, "ATANGANA", RoleEnum.DRH);

    private static Utilisateur acteur(Long id, String nom, RoleEnum role) {
        return Utilisateur.builder().id(id).nom(nom).role(role).build();
    }

    @Mock
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private GrilleTarifaireService grilleTarifaireService;

    @BeforeEach
    void setUp() {
        // SeparationTachesGrilleService est instancie pour de vrai, pas mocke :
        // c'est l'implementation de RG-08/RG-05 sur les grilles, la mocker
        // reviendrait a ne jamais tester la regle que ce sprint ajoute.
        grilleTarifaireService = new GrilleTarifaireService(
                grilleTarifaireRepository, fonctionEligibleRepository,
                new SeparationTachesGrilleService(), eventPublisher);
    }

    private FonctionEligible creerFonctionEligible(Long id, String code) {
        return FonctionEligible.builder().id(id).code(code).libelle(code).actif(true).build();
    }

    // Grille au premier etage d'attente : ce que produit desormais creer().
    private GrilleTarifaire creerGrilleEnAttenteCrh(Long id, Long idFonction, Integer montant) {
        return GrilleTarifaire.builder()
                .id(id)
                .idFonctionEligible(idFonction)
                .montantFcfa(montant)
                .dateDebut(LocalDate.of(2026, 9, 1))
                .statutValidation(StatutGrilleEnum.EN_ATTENTE_CRH)
                .idCreateur(9L)
                .dateCreation(LocalDateTime.of(2026, 7, 23, 8, 0))
                .build();
    }

    // Grille au second etage : le CRH a deja statue, d'ou idDecideurCrh
    // renseigne -- c'est contre lui que porte RG-08 a l'etape DRH.
    private GrilleTarifaire creerGrilleEnAttenteDrh(Long id, Long idFonction, Integer montant) {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(id, idFonction, montant);
        grille.setStatutValidation(StatutGrilleEnum.EN_ATTENTE_DRH);
        grille.setIdDecideurCrh(CRH.getId());
        grille.setDateDecisionCrh(LocalDateTime.of(2026, 7, 24, 9, 0));
        return grille;
    }

    private GrilleTarifaire creerGrilleActive(Long id, Long idFonction, Integer montant, LocalDate dateDebut) {
        return GrilleTarifaire.builder()
                .id(id)
                .idFonctionEligible(idFonction)
                .montantFcfa(montant)
                .dateDebut(dateDebut)
                .statutValidation(StatutGrilleEnum.ACTIVE)
                .idCreateur(9L)
                .dateCreation(LocalDateTime.of(2026, 1, 1, 8, 0))
                .build();
    }

    private void aucuneGrilleEnAttente(Long idFonction) {
        when(grilleTarifaireRepository.existsByIdFonctionEligibleAndStatutValidationIn(
                eq(idFonction), anyCollection())).thenReturn(false);
    }

    // =================================================================
    // creer()
    // =================================================================

    @Test
    void creer_casNominal_creeLaGrilleAuStatutEnAttenteCrh() {
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        aucuneGrilleEnAttente(1L);

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.creer(requete, 9L);

        // Sprint MM.12 : le premier etage est desormais le CRH, plus la DRH.
        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_CRH);
        assertThat(resultat.getMontantFcfa()).isEqualTo(45000);
        assertThat(resultat.getCodeFonction()).isEqualTo("GFC");
        verify(grilleTarifaireRepository).save(any(GrilleTarifaire.class));
    }

    @Test
    void creer_fonctionInconnue_leve404() {
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("INCONNUE").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();

        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grilleTarifaireService.creer(requete, 9L))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void creer_grilleDejaEnAttente_leve409() {
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.existsByIdFonctionEligibleAndStatutValidationIn(
                eq(1L), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> grilleTarifaireService.creer(requete, 9L))
                .isInstanceOf(GrilleEnAttenteDrhExistanteException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void creer_leGardeFou409CouvreLesDeuxStatutsDAttente() {
        // Sprint MM.12 : avec deux etages d'attente, ne verifier que
        // EN_ATTENTE_DRH laisserait creer une seconde grille pour une fonction
        // dont une grille attend deja le CRH (et inversement).
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.existsByIdFonctionEligibleAndStatutValidationIn(
                eq(1L), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> grilleTarifaireService.creer(requete, 9L))
                .isInstanceOf(GrilleEnAttenteDrhExistanteException.class);

        ArgumentCaptor<Collection<StatutGrilleEnum>> statutsCaptor = ArgumentCaptor.captor();
        verify(grilleTarifaireRepository)
                .existsByIdFonctionEligibleAndStatutValidationIn(eq(1L), statutsCaptor.capture());
        assertThat(statutsCaptor.getValue())
                .containsExactlyInAnyOrder(StatutGrilleEnum.EN_ATTENTE_CRH, StatutGrilleEnum.EN_ATTENTE_DRH);
    }

    @Test
    void creer_dateDebutAnterieureAGrilleClotureeExistante_leve409() {
        // Constat manuel : apres desactivation d'une grille (Sprint 6F.7bis),
        // rien n'empechait de creer une nouvelle grille avec une dateDebut
        // anterieure a l'historique existant (ex. grille clôturee courant
        // jusqu'a 2026-07-30, nouvelle grille proposee avec dateDebut
        // 2024-01-30).
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(1000).dateDebut(LocalDate.of(2024, 1, 30)).build();
        GrilleTarifaire grilleCloturee = creerGrilleActive(1L, 1L, 70000, LocalDate.of(2025, 1, 1));
        grilleCloturee.setDateFin(LocalDate.of(2026, 7, 30));

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        aucuneGrilleEnAttente(1L);
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleCloturee));

        assertThatThrownBy(() -> grilleTarifaireService.creer(requete, 9L))
                .isInstanceOf(DateDebutGrilleAnterieureException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void creer_dateDebutPosterieureAGrilleActiveExistante_creeLaGrille() {
        // Cas normal de mise a jour tarifaire (dateDebut posterieure a la
        // grille encore ouverte) : doit rester autorise.
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        aucuneGrilleEnAttente(1L);
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleActive));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.creer(requete, 9L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_CRH);
        verify(grilleTarifaireRepository).save(any(GrilleTarifaire.class));
    }

    @Test
    void creer_dateDebutAnterieureMaisGrilleLaPlusRecenteEstRejetee_ignoreLaRejetee() {
        // Une grille REJETEE ne fait pas partie de l'historique reel : la
        // comparaison doit se faire contre la derniere grille non rejetee.
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 3, 1)).build();
        GrilleTarifaire grilleRejetee = creerGrilleEnAttenteCrh(3L, 1L, 90000);
        grilleRejetee.setDateDebut(LocalDate.of(2026, 12, 1));
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        aucuneGrilleEnAttente(1L);
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleRejetee, grilleActive));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.creer(requete, 9L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_CRH);
    }

    // =================================================================
    // modifier()
    // =================================================================

    @Test
    void modifier_casNominal_metAJourLeMontant() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 40000);
        ModifierGrilleTarifaireRequestDto requete = ModifierGrilleTarifaireRequestDto.builder()
                .montantFcfa(45000).build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.modifier(2L, requete, 9L);

        assertThat(resultat.getMontantFcfa()).isEqualTo(45000);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());

        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(9L);
        assertThat(evenement.action()).isEqualTo("MODIFICATION_GRILLE_TARIFAIRE");
        assertThat(evenement.entiteCible()).isEqualTo("grille_tarifaire");
        assertThat(evenement.idEntite()).isEqualTo(2L);
        assertThat(evenement.avant()).containsEntry("montantFcfa", 40000);
        assertThat(evenement.apres()).containsEntry("montantFcfa", 45000);
    }

    @Test
    void modifier_grilleDejaValideeParLeCrh_leve400() {
        // Arbitrage MM.12 du 2026-08-09 : le montant est GELE des que le CRH a
        // statue. Sans cette borne, l'ARH modifierait un montant deja valide par
        // le CRH et la DRH validerait un chiffre que le CRH n'a jamais vu.
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 40000);
        ModifierGrilleTarifaireRequestDto requete = ModifierGrilleTarifaireRequestDto.builder()
                .montantFcfa(45000).build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.modifier(2L, requete, 9L))
                .isInstanceOf(GrilleNonModifiableException.class);

        verify(grilleTarifaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void modifier_statutIncompatible_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 40000);
        grille.setStatutValidation(StatutGrilleEnum.ACTIVE);
        ModifierGrilleTarifaireRequestDto requete = ModifierGrilleTarifaireRequestDto.builder()
                .montantFcfa(45000).build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.modifier(2L, requete, 9L))
                .isInstanceOf(GrilleNonModifiableException.class);

        verify(grilleTarifaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void modifier_grilleIntrouvable_leve404() {
        ModifierGrilleTarifaireRequestDto requete = ModifierGrilleTarifaireRequestDto.builder()
                .montantFcfa(45000).build();

        when(grilleTarifaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grilleTarifaireService.modifier(99L, requete, 9L))
                .isInstanceOf(GrilleIntrouvableException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    // =================================================================
    // Etape CRH (Sprint MM.12)
    // =================================================================

    @Test
    void validerCrh_casNominal_passeLaGrilleEnAttenteDrh() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, CRH);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);
        assertThat(grille.getIdDecideurCrh()).isEqualTo(CRH.getId());
        assertThat(grille.getDateDecisionCrh()).isNotNull();
        // RG-10 : une validation CRH ne met AUCUNE grille en vigueur.
        assertThat(grille.getIdValidateur()).isNull();
        verify(grilleTarifaireRepository, never()).saveAndFlush(any());
    }

    @Test
    void validerCrh_traceUnAuditDedie() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        grilleTarifaireService.validerOuRejeter(2L, requete, CRH);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());

        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.action()).isEqualTo("DECISION_GRILLE_TARIFAIRE_CRH");
        assertThat(evenement.idUtilisateur()).isEqualTo(CRH.getId());
        assertThat(evenement.avant()).containsEntry("statutValidation", "EN_ATTENTE_CRH");
        assertThat(evenement.apres()).containsEntry("statutValidation", "EN_ATTENTE_DRH");
    }

    @Test
    void rejeterCrh_motifPresent_passeAREJETEEEtRenseigneLeDecideur() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("REJETER").motifRejet("Montant hors enveloppe validée par la direction").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, CRH);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.REJETEE);
        assertThat(grille.getMotifRejet()).isEqualTo("Montant hors enveloppe validée par la direction");
        // Invariant dont depend origineRejet : le couple CRH est renseigne AU
        // REJET aussi, pas seulement a la validation.
        assertThat(grille.getIdDecideurCrh()).isEqualTo(CRH.getId());
        assertThat(grille.getIdValidateur()).isNull();
    }

    @Test
    void rejeterCrh_motifAbsent_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("REJETER").motifRejet(" ").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, CRH))
                .isInstanceOf(GrilleTarifaireMotifRejetObligatoireException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void validerCrh_acteurEstLArhCreateur_leve403() {
        // RG-08, scenario protege : le compte a cree la grille en tant qu'ARH
        // puis a ete promu CRH via PATCH /admin/utilisateurs/{id}/role.
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        Utilisateur arhPromuCrh = acteur(ARH_CREATEUR.getId(), "MBARGA", RoleEnum.CRH);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, arhPromuCrh))
                .isInstanceOf(SeparationTachesGrilleViolationException.class);

        verify(grilleTarifaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void validerCrh_acteurDrh_leve403() {
        // RG-05 : le @PreAuthorize du controleur laisse passer CRH et DRH ; sans
        // verifierRoleAttendu(), une DRH sauterait purement l'etape CRH.
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, DRH))
                .isInstanceOf(RoleDecisionGrilleNonAutoriseException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    // =================================================================
    // Etape DRH
    // =================================================================

    @Test
    void validerDrh_casNominal_activeLaNouvelleEtFermeLAncienne() {
        GrilleTarifaire nouvelleGrille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        GrilleTarifaire ancienneGrille = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(nouvelleGrille));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.of(ancienneGrille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, DRH);

        // RG-10 preservee : une seule grille ACTIVE sans date_fin par fonction.
        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        assertThat(ancienneGrille.getDateFin()).isEqualTo(LocalDate.of(2026, 8, 31));
        verify(grilleTarifaireRepository).saveAndFlush(ancienneGrille);
        verify(grilleTarifaireRepository).save(nouvelleGrille);
    }

    @Test
    void validerDrh_premiereGrillePourLaFonction_activeSansFermerRien() {
        GrilleTarifaire nouvelleGrille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(nouvelleGrille));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.empty());
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, DRH);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        verify(grilleTarifaireRepository, org.mockito.Mockito.times(1)).save(any(GrilleTarifaire.class));
    }

    @Test
    void validerDrh_traceUnAuditDedie() {
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.empty());
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        grilleTarifaireService.validerOuRejeter(2L, requete, DRH);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());

        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.action()).isEqualTo("DECISION_GRILLE_TARIFAIRE_DRH");
        assertThat(evenement.avant()).containsEntry("statutValidation", "EN_ATTENTE_DRH");
        assertThat(evenement.apres()).containsEntry("statutValidation", "ACTIVE");
    }

    @Test
    void rejeterDrh_motifPresent_passeAREJETEE() {
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("REJETER").motifRejet("Montant incompatible avec la grille salariale en vigueur").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, DRH);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.REJETEE);
        assertThat(grille.getMotifRejet()).isEqualTo("Montant incompatible avec la grille salariale en vigueur");
        assertThat(grille.getIdValidateur()).isEqualTo(DRH.getId());
        verify(grilleTarifaireRepository).save(grille);
    }

    @Test
    void rejeterDrh_motifAbsent_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("REJETER").motifRejet(" ").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, DRH))
                .isInstanceOf(GrilleTarifaireMotifRejetObligatoireException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void validerDrh_acteurEstLeCrhAyantValide_leve403() {
        // RG-08 a l'etage DRH : le decideur precedent est le CRH.
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        Utilisateur crhPromuDrh = acteur(CRH.getId(), "ESSAMA", RoleEnum.DRH);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, crhPromuDrh))
                .isInstanceOf(SeparationTachesGrilleViolationException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void validerDrh_grilleHeriteeSansDecideurCrh_resteValidable() {
        // Migration V7 : les grilles deja EN_ATTENTE_DRH avant MM.12 sont
        // laissees en l'etat et n'ont donc pas de decideur CRH. RG-08 n'a rien
        // a comparer -- les bloquer les rendrait definitivement invalidables.
        GrilleTarifaire grilleHeritee = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        grilleHeritee.setIdDecideurCrh(null);
        grilleHeritee.setDateDecisionCrh(null);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grilleHeritee));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.empty());
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, DRH);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
    }

    @Test
    void validerDrh_acteurCrh_leve403() {
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        // Acteur CRH distinct du decideur CRH enregistre : c'est bien RG-05 qui
        // doit repondre, pas RG-08.
        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(
                2L, requete, acteur(77L, "NKOLO", RoleEnum.CRH)))
                .isInstanceOf(RoleDecisionGrilleNonAutoriseException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    // =================================================================
    // Cycle complet et gardes de statut
    // =================================================================

    @Test
    void cycleComplet_arhPuisCrhPuisDrh_aboutitAActive() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.empty());

        GrilleTarifaireResponseDto apresCrh = grilleTarifaireService.validerOuRejeter(
                2L, DecisionGrilleTarifaireRequestDto.builder().decision("VALIDER").build(), CRH);
        assertThat(apresCrh.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);

        GrilleTarifaireResponseDto apresDrh = grilleTarifaireService.validerOuRejeter(
                2L, DecisionGrilleTarifaireRequestDto.builder().decision("VALIDER").build(), DRH);
        assertThat(apresDrh.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);

        assertThat(grille.getIdDecideurCrh()).isEqualTo(CRH.getId());
        assertThat(grille.getIdValidateur()).isEqualTo(DRH.getId());
    }

    @Test
    void validerOuRejeter_grilleNonEnAttente_leve409() {
        GrilleTarifaire grille = creerGrilleActive(2L, 1L, 45000, LocalDate.of(2026, 9, 1));
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, DRH))
                .isInstanceOf(GrilleNonEnAttenteDrhException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void validerOuRejeter_decisionInvalide_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("ANNULER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, DRH))
                .isInstanceOf(DecisionGrilleInvalideException.class);
    }

    // =================================================================
    // rechercher() / historique()
    // =================================================================

    @Test
    void rechercher_sansFiltre_retourneToutesLesGrillesAvecLibelleFonction() {
        GrilleTarifaire grilleGfc = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        GrilleTarifaire grilleConseiller = creerGrilleEnAttenteCrh(2L, 2L, 50000);

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("Gestionnaire de Fonds de Commerce").actif(true).build(),
                FonctionEligible.builder().id(2L).code("CONSEILLER").libelle("Conseiller").actif(true).build()));
        when(grilleTarifaireRepository.findAll()).thenReturn(List.of(grilleGfc, grilleConseiller));

        List<GrilleTarifaireListeLigneDto> resultat = grilleTarifaireService.rechercher(null, null);

        assertThat(resultat).hasSize(2);
        assertThat(resultat).extracting(GrilleTarifaireListeLigneDto::getLibelleFonction)
                .containsExactlyInAnyOrder("Gestionnaire de Fonds de Commerce", "Conseiller");
    }

    @Test
    void rechercher_filtreParStatutEnAttenteCrh_neRetourneQueLesGrillesCorrespondantes() {
        // Alimente GET /grilles-tarifaires/en-attente-crh, l'ecran de validation
        // du CRH (Sprint MM.12).
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        GrilleTarifaire grilleEnAttenteCrh = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        GrilleTarifaire grilleEnAttenteDrh = creerGrilleEnAttenteDrh(3L, 1L, 47000);

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("Gestionnaire de Fonds de Commerce").actif(true).build()));
        when(grilleTarifaireRepository.findAll())
                .thenReturn(List.of(grilleActive, grilleEnAttenteCrh, grilleEnAttenteDrh));

        List<GrilleTarifaireListeLigneDto> resultat =
                grilleTarifaireService.rechercher(null, StatutGrilleEnum.EN_ATTENTE_CRH);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void rechercher_filtreParStatut_neRetourneQueLesGrillesCorrespondantes() {
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        GrilleTarifaire grilleEnAttente = creerGrilleEnAttenteDrh(2L, 1L, 45000);

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("Gestionnaire de Fonds de Commerce").actif(true).build()));
        when(grilleTarifaireRepository.findAll()).thenReturn(List.of(grilleActive, grilleEnAttente));

        List<GrilleTarifaireListeLigneDto> resultat =
                grilleTarifaireService.rechercher(null, StatutGrilleEnum.EN_ATTENTE_DRH);

        assertThat(resultat).hasSize(1);
        assertThat(resultat.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void rechercher_grilleRejetee_exposeLeMotifDeRejet() {
        GrilleTarifaire grilleRejetee = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        grilleRejetee.setMotifRejet("Montant incompatible avec la grille salariale en vigueur");

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("Gestionnaire de Fonds de Commerce").actif(true).build()));
        when(grilleTarifaireRepository.findAll()).thenReturn(List.of(grilleRejetee));

        List<GrilleTarifaireListeLigneDto> resultat = grilleTarifaireService.rechercher(null, null);

        assertThat(resultat.get(0).getMotifRejet())
                .isEqualTo("Montant incompatible avec la grille salariale en vigueur");
    }

    @Test
    void rechercher_rejetParLeCrh_exposeOrigineRejetCrh() {
        GrilleTarifaire grilleRejetee = creerGrilleEnAttenteCrh(2L, 1L, 45000);
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        grilleRejetee.setMotifRejet("Montant hors enveloppe");
        grilleRejetee.setIdDecideurCrh(CRH.getId());

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("GFC").actif(true).build()));
        when(grilleTarifaireRepository.findAll()).thenReturn(List.of(grilleRejetee));

        List<GrilleTarifaireListeLigneDto> resultat = grilleTarifaireService.rechercher(null, null);

        assertThat(resultat.get(0).getOrigineRejet()).isEqualTo("CRH");
    }

    @Test
    void rechercher_rejetParLaDrh_exposeOrigineRejetDrh() {
        // Apres un rejet DRH les DEUX couples de decision sont renseignes (le
        // CRH avait valide avant) : la derivation doit tester la DRH en premier.
        GrilleTarifaire grilleRejetee = creerGrilleEnAttenteDrh(2L, 1L, 45000);
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        grilleRejetee.setMotifRejet("Enveloppe annuelle deja consommee");
        grilleRejetee.setIdValidateur(DRH.getId());

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("GFC").actif(true).build()));
        when(grilleTarifaireRepository.findAll()).thenReturn(List.of(grilleRejetee));

        List<GrilleTarifaireListeLigneDto> resultat = grilleTarifaireService.rechercher(null, null);

        assertThat(resultat.get(0).getOrigineRejet()).isEqualTo("DRH");
    }

    @Test
    void rechercher_grilleNonRejetee_nExposeAucuneOrigineDeRejet() {
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        grilleActive.setIdValidateur(DRH.getId());

        when(fonctionEligibleRepository.findAll()).thenReturn(List.of(
                FonctionEligible.builder().id(1L).code("GFC").libelle("GFC").actif(true).build()));
        when(grilleTarifaireRepository.findAll()).thenReturn(List.of(grilleActive));

        List<GrilleTarifaireListeLigneDto> resultat = grilleTarifaireService.rechercher(null, null);

        assertThat(resultat.get(0).getOrigineRejet()).isNull();
    }

    @Test
    void rechercher_filtreParFonctionInconnue_leve404() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grilleTarifaireService.rechercher("INCONNUE", null))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);
    }

    @Test
    void historique_retourneToutesLesGrillesTrieesParDate() {
        GrilleTarifaire grilleRecente = creerGrilleActive(3L, 1L, 45000, LocalDate.of(2026, 9, 1));
        GrilleTarifaire grilleAncienne = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        grilleAncienne.setDateFin(LocalDate.of(2026, 8, 31));

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleRecente, grilleAncienne));

        HistoriqueGrilleTarifaireResponseDto resultat = grilleTarifaireService.historique("GFC");

        assertThat(resultat.getCodeFonction()).isEqualTo("GFC");
        assertThat(resultat.getGrilles()).hasSize(2);
        assertThat(resultat.getGrilles().get(0).getDateDebut()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(resultat.getGrilles().get(1).getDateDebut()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void historique_grilleRejetee_exposeLeMotifEtLOrigineDuRejet() {
        GrilleTarifaire grilleRejetee = creerGrilleEnAttenteCrh(4L, 1L, 45000);
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        grilleRejetee.setMotifRejet("Montant supérieur au plafond prévu pour cette fonction.");
        grilleRejetee.setIdDecideurCrh(CRH.getId());

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleRejetee));

        HistoriqueGrilleTarifaireResponseDto resultat = grilleTarifaireService.historique("GFC");

        assertThat(resultat.getGrilles().get(0).getMotifRejet())
                .isEqualTo("Montant supérieur au plafond prévu pour cette fonction.");
        assertThat(resultat.getGrilles().get(0).getOrigineRejet()).isEqualTo("CRH");
    }

    @Test
    void historique_fonctionInconnue_leve404() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grilleTarifaireService.historique("INCONNUE"))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);
    }

    // =================================================================
    // desactiver()
    // =================================================================

    @Test
    void desactiverGrille_grilleActive_positionneDateFin() {
        GrilleTarifaire grille = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));

        when(grilleTarifaireRepository.findById(1L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.desactiver(1L, 12L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        assertThat(grille.getDateFin()).isNotNull();
        verify(grilleTarifaireRepository).save(grille);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());

        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(12L);
        assertThat(evenement.action()).isEqualTo("DESACTIVATION_GRILLE_TARIFAIRE");
        assertThat(evenement.entiteCible()).isEqualTo("grille_tarifaire");
        assertThat(evenement.idEntite()).isEqualTo(1L);
        assertThat(evenement.apres()).containsKey("dateFin");
    }

    @Test
    void desactiverGrille_grilleNonActive_leve409() {
        GrilleTarifaire grille = creerGrilleEnAttenteCrh(2L, 1L, 45000);

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.desactiver(2L, 12L))
                .isInstanceOf(GrilleNonActiveException.class);

        verify(grilleTarifaireRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void desactiverGrille_grilleActiveMaisDejaFermee_leve409() {
        // Une grille ACTIVE dont dateFin est deja renseignee est l'ancienne
        // grille d'un remplacement (validerOuRejeter) -- RG-04 ne la considere
        // plus comme active (ACTIVE + dateFin IS NULL). La desactivation ne
        // doit pas pouvoir la re-fermer.
        GrilleTarifaire grille = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        grille.setDateFin(LocalDate.of(2026, 6, 30));

        when(grilleTarifaireRepository.findById(1L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.desactiver(1L, 12L))
                .isInstanceOf(GrilleNonActiveException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void desactiverGrille_grilleIntrouvable_leve404() {
        when(grilleTarifaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grilleTarifaireService.desactiver(99L, 12L))
                .isInstanceOf(GrilleIntrouvableException.class);
    }
}
