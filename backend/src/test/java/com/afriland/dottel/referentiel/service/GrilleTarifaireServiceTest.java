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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrilleTarifaireServiceTest {

    @Mock
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private GrilleTarifaireService grilleTarifaireService;

    @BeforeEach
    void setUp() {
        grilleTarifaireService = new GrilleTarifaireService(
                grilleTarifaireRepository, fonctionEligibleRepository, eventPublisher);
    }

    private FonctionEligible creerFonctionEligible(Long id, String code) {
        return FonctionEligible.builder().id(id).code(code).libelle(code).actif(true).build();
    }

    private GrilleTarifaire creerGrilleEnAttente(Long id, Long idFonction, Integer montant) {
        return GrilleTarifaire.builder()
                .id(id)
                .idFonctionEligible(idFonction)
                .montantFcfa(montant)
                .dateDebut(LocalDate.of(2026, 9, 1))
                .statutValidation(StatutGrilleEnum.EN_ATTENTE_DRH)
                .idCreateur(9L)
                .dateCreation(LocalDateTime.of(2026, 7, 23, 8, 0))
                .build();
    }

    @Test
    void creer_casNominal_creeLaGrilleAuStatutAttendu() {
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.EN_ATTENTE_DRH)).thenReturn(Optional.empty());

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.creer(requete, 9L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);
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
    void creer_grilleDejaEnAttenteDrh_leve409() {
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 9, 1)).build();

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.EN_ATTENTE_DRH))
                .thenReturn(Optional.of(creerGrilleEnAttente(2L, 1L, 40000)));

        assertThatThrownBy(() -> grilleTarifaireService.creer(requete, 9L))
                .isInstanceOf(GrilleEnAttenteDrhExistanteException.class);

        verify(grilleTarifaireRepository, never()).save(any());
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
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.EN_ATTENTE_DRH)).thenReturn(Optional.empty());
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
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.EN_ATTENTE_DRH)).thenReturn(Optional.empty());
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleActive));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.creer(requete, 9L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);
        verify(grilleTarifaireRepository).save(any(GrilleTarifaire.class));
    }

    @Test
    void creer_dateDebutAnterieureMaisGrilleLaPlusRecenteEstRejetee_ignoreLaRejetee() {
        // Une grille REJETEE ne fait pas partie de l'historique reel : la
        // comparaison doit se faire contre la derniere grille non rejetee.
        CreerGrilleTarifaireRequestDto requete = CreerGrilleTarifaireRequestDto.builder()
                .codeFonction("GFC").montantFcfa(45000).dateDebut(LocalDate.of(2026, 3, 1)).build();
        GrilleTarifaire grilleRejetee = creerGrilleEnAttente(3L, 1L, 90000);
        grilleRejetee.setDateDebut(LocalDate.of(2026, 12, 1));
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.EN_ATTENTE_DRH)).thenReturn(Optional.empty());
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleRejetee, grilleActive));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.creer(requete, 9L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.EN_ATTENTE_DRH);
    }

    @Test
    void modifier_casNominal_metAJourLeMontant() {
        GrilleTarifaire grille = creerGrilleEnAttente(2L, 1L, 40000);
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
    void modifier_statutIncompatible_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttente(2L, 1L, 40000);
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

    @Test
    void valider_casNominal_activeLaNouvelleEtFermeLAncienne() {
        GrilleTarifaire nouvelleGrille = creerGrilleEnAttente(2L, 1L, 45000);
        GrilleTarifaire ancienneGrille = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(nouvelleGrille));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.of(ancienneGrille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, 12L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        assertThat(ancienneGrille.getDateFin()).isEqualTo(LocalDate.of(2026, 8, 31));
        verify(grilleTarifaireRepository).saveAndFlush(ancienneGrille);
        verify(grilleTarifaireRepository).save(nouvelleGrille);
    }

    @Test
    void valider_premiereGrillePourLaFonction_activeSansFermerRien() {
        GrilleTarifaire nouvelleGrille = creerGrilleEnAttente(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(nouvelleGrille));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                1L, StatutGrilleEnum.ACTIVE)).thenReturn(Optional.empty());
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, 12L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.ACTIVE);
        verify(grilleTarifaireRepository, org.mockito.Mockito.times(1)).save(any(GrilleTarifaire.class));
    }

    @Test
    void rejeter_motifPresent_passeAREJETEE() {
        GrilleTarifaire grille = creerGrilleEnAttente(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("REJETER").motifRejet("Montant incompatible avec la grille salariale en vigueur").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));
        when(fonctionEligibleRepository.findById(1L))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));

        GrilleTarifaireResponseDto resultat = grilleTarifaireService.validerOuRejeter(2L, requete, 12L);

        assertThat(resultat.getStatutValidation()).isEqualTo(StatutGrilleEnum.REJETEE);
        assertThat(grille.getMotifRejet()).isEqualTo("Montant incompatible avec la grille salariale en vigueur");
        verify(grilleTarifaireRepository).save(grille);
    }

    @Test
    void rejeter_motifAbsent_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttente(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("REJETER").motifRejet(" ").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, 12L))
                .isInstanceOf(GrilleTarifaireMotifRejetObligatoireException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void validerOuRejeter_grilleNonEnAttente_leve409() {
        GrilleTarifaire grille = creerGrilleActive(2L, 1L, 45000, LocalDate.of(2026, 9, 1));
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("VALIDER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, 12L))
                .isInstanceOf(GrilleNonEnAttenteDrhException.class);

        verify(grilleTarifaireRepository, never()).save(any());
    }

    @Test
    void validerOuRejeter_decisionInvalide_leve400() {
        GrilleTarifaire grille = creerGrilleEnAttente(2L, 1L, 45000);
        DecisionGrilleTarifaireRequestDto requete = DecisionGrilleTarifaireRequestDto.builder()
                .decision("ANNULER").build();

        when(grilleTarifaireRepository.findById(2L)).thenReturn(Optional.of(grille));

        assertThatThrownBy(() -> grilleTarifaireService.validerOuRejeter(2L, requete, 12L))
                .isInstanceOf(DecisionGrilleInvalideException.class);
    }

    @Test
    void rechercher_sansFiltre_retourneToutesLesGrillesAvecLibelleFonction() {
        GrilleTarifaire grilleGfc = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        GrilleTarifaire grilleConseiller = creerGrilleEnAttente(2L, 2L, 50000);

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
    void rechercher_filtreParStatut_neRetourneQueLesGrillesCorrespondantes() {
        GrilleTarifaire grilleActive = creerGrilleActive(1L, 1L, 40000, LocalDate.of(2026, 1, 1));
        GrilleTarifaire grilleEnAttente = creerGrilleEnAttente(2L, 1L, 45000);

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
        GrilleTarifaire grilleRejetee = creerGrilleEnAttente(2L, 1L, 45000);
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
    void historique_grilleRejetee_exposeLeMotifDeRejet() {
        GrilleTarifaire grilleRejetee = creerGrilleEnAttente(4L, 1L, 45000);
        grilleRejetee.setStatutValidation(StatutGrilleEnum.REJETEE);
        grilleRejetee.setMotifRejet("Montant supérieur au plafond prévu pour cette fonction.");

        when(fonctionEligibleRepository.findByCode("GFC"))
                .thenReturn(Optional.of(creerFonctionEligible(1L, "GFC")));
        when(grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(1L))
                .thenReturn(List.of(grilleRejetee));

        HistoriqueGrilleTarifaireResponseDto resultat = grilleTarifaireService.historique("GFC");

        assertThat(resultat.getGrilles().get(0).getMotifRejet())
                .isEqualTo("Montant supérieur au plafond prévu pour cette fonction.");
    }

    @Test
    void historique_fonctionInconnue_leve404() {
        when(fonctionEligibleRepository.findByCode("INCONNUE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> grilleTarifaireService.historique("INCONNUE"))
                .isInstanceOf(FonctionEligibleIntrouvableException.class);
    }

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
        GrilleTarifaire grille = creerGrilleEnAttente(2L, 1L, 45000);

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