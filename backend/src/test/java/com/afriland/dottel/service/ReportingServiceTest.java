package com.afriland.dottel.service;

import com.afriland.dottel.model.dto.reporting.DashboardResponseDto;
import com.afriland.dottel.model.dto.reporting.HistoriqueResponseDto;
import com.afriland.dottel.model.entity.ProcessusMensuel;
import com.afriland.dottel.model.enums.StatutEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.repository.ProcessusMensuelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private ProcessusMensuelRepository processusMensuelRepository;

    @Mock
    private LigneEtatMensuelRepository ligneEtatMensuelRepository;

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(beneficiaireRepository, processusMensuelRepository,
                ligneEtatMensuelRepository);
    }

    private ProcessusMensuel creerProcessus(Long id, StatutEnum statut, Integer anneePaiement) {
        return creerProcessus(id, statut, anneePaiement, 6);
    }

    private ProcessusMensuel creerProcessus(Long id, StatutEnum statut, Integer anneePaiement, Integer moisPaiement) {
        return ProcessusMensuel.builder()
                .id(id)
                .moisPaiement(moisPaiement)
                .anneePaiement(anneePaiement)
                .statut(statut)
                .dateCreation(LocalDateTime.of(anneePaiement, moisPaiement, 1, 8, 0))
                .idCreateur(10L)
                .build();
    }

    @Test
    void tableauDeBord_casNominal_retourneLesBonsChiffres() {
        int anneeCourante = Year.now().getValue();
        ProcessusMensuel processusRecent = creerProcessus(5L, StatutEnum.EN_ATTENTE_CRH, anneeCourante);

        when(beneficiaireRepository.countByActifTrue()).thenReturn(42L);
        when(processusMensuelRepository.findFirstByOrderByDateCreationDesc())
                .thenReturn(Optional.of(processusRecent));
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(5L)).thenReturn(1_850_000L);
        when(processusMensuelRepository.findFirstByStatutNotInOrderByDateCreationDesc(any()))
                .thenReturn(Optional.of(processusRecent));
        when(processusMensuelRepository.countByStatutAndAnneePaiement(StatutEnum.CLOTURE, anneeCourante))
                .thenReturn(3L);

        DashboardResponseDto resultat = reportingService.tableauDeBord();

        assertThat(resultat.getNombreBeneficiairesActifs()).isEqualTo(42L);
        assertThat(resultat.getMontantTotalMensuel()).isEqualTo(1_850_000L);
        assertThat(resultat.getProcessusEnCours()).isNotNull();
        assertThat(resultat.getProcessusEnCours().getId()).isEqualTo(5L);
        assertThat(resultat.getProcessusEnCours().getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(resultat.getProcessusClotureesCetteAnnee()).isEqualTo(3L);
    }

    @Test
    void tableauDeBord_aucunProcessusEnCours_retourneNull() {
        int anneeCourante = Year.now().getValue();
        ProcessusMensuel dernierProcessusCloture = creerProcessus(7L, StatutEnum.CLOTURE, anneeCourante);

        when(beneficiaireRepository.countByActifTrue()).thenReturn(15L);
        when(processusMensuelRepository.findFirstByOrderByDateCreationDesc())
                .thenReturn(Optional.of(dernierProcessusCloture));
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(7L)).thenReturn(950_000L);
        when(processusMensuelRepository.findFirstByStatutNotInOrderByDateCreationDesc(any()))
                .thenReturn(Optional.empty());
        when(processusMensuelRepository.countByStatutAndAnneePaiement(StatutEnum.CLOTURE, anneeCourante))
                .thenReturn(4L);

        DashboardResponseDto resultat = reportingService.tableauDeBord();

        assertThat(resultat.getProcessusEnCours()).isNull();
    }

    @Test
    void tableauDeBord_aucunBeneficiaireActif_retourneZero() {
        int anneeCourante = Year.now().getValue();

        when(beneficiaireRepository.countByActifTrue()).thenReturn(0L);
        when(processusMensuelRepository.findFirstByOrderByDateCreationDesc())
                .thenReturn(Optional.empty());
        when(processusMensuelRepository.findFirstByStatutNotInOrderByDateCreationDesc(any()))
                .thenReturn(Optional.empty());
        when(processusMensuelRepository.countByStatutAndAnneePaiement(StatutEnum.CLOTURE, anneeCourante))
                .thenReturn(0L);

        DashboardResponseDto resultat = reportingService.tableauDeBord();

        assertThat(resultat.getNombreBeneficiairesActifs()).isEqualTo(0L);
        assertThat(resultat.getMontantTotalMensuel()).isEqualTo(0L);
        assertThat(resultat.getProcessusEnCours()).isNull();
        assertThat(resultat.getProcessusClotureesCetteAnnee()).isEqualTo(0L);
    }

    @Test
    void historique_casNominal_retourneLesProcessusClotures() {
        ProcessusMensuel processusJuin = creerProcessus(11L, StatutEnum.CLOTURE, 2026, 6);
        ProcessusMensuel processusMai = creerProcessus(10L, StatutEnum.CLOTURE, 2026, 5);

        when(processusMensuelRepository.findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(
                StatutEnum.CLOTURE, 2026)).thenReturn(List.of(processusJuin, processusMai));
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(11L)).thenReturn(2_100_000L);
        when(ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(11L)).thenReturn(28L);
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(10L)).thenReturn(1_950_000L);
        when(ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(10L)).thenReturn(26L);

        HistoriqueResponseDto resultat = reportingService.historique(2026);

        assertThat(resultat.getLignes()).hasSize(2);
        assertThat(resultat.getLignes().get(0).getMoisPaiement()).isEqualTo(6);
        assertThat(resultat.getLignes().get(0).getMontantTotal()).isEqualTo(2_100_000L);
        assertThat(resultat.getLignes().get(0).getNombreBeneficiaires()).isEqualTo(28L);
        assertThat(resultat.getLignes().get(1).getMoisPaiement()).isEqualTo(5);
    }

    @Test
    void historique_aucunProcessusPourLannee_retourneListeVide() {
        when(processusMensuelRepository.findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(
                StatutEnum.CLOTURE, 2025)).thenReturn(List.of());

        HistoriqueResponseDto resultat = reportingService.historique(2025);

        assertThat(resultat.getLignes()).isEmpty();
    }
}