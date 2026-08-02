package com.afriland.dottel.reporting.service;

import com.afriland.dottel.reporting.model.dto.reporting.DashboardResponseDto;
import com.afriland.dottel.reporting.model.dto.reporting.HistoriqueLigneDto;
import com.afriland.dottel.reporting.model.dto.reporting.HistoriqueResponseDto;
import com.afriland.dottel.reporting.model.dto.reporting.ProcessusEnCoursDto;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.processus.repository.ProcessusMensuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private static final EnumSet<StatutEnum> STATUTS_HORS_EN_COURS = EnumSet.of(StatutEnum.CLOTURE, StatutEnum.RETOURNE);

    private final BeneficiaireRepository beneficiaireRepository;
    private final ProcessusMensuelRepository processusMensuelRepository;
    private final LigneEtatMensuelRepository ligneEtatMensuelRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDto tableauDeBord() {
        long nombreBeneficiairesActifs = beneficiaireRepository.countByActifTrue();

        long montantTotalMensuel = processusMensuelRepository.findFirstByOrderByDateCreationDesc()
                .map(processus -> ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(processus.getId()))
                .orElse(0L);

        ProcessusEnCoursDto processusEnCours = processusMensuelRepository
                .findFirstByStatutNotInOrderByDateCreationDesc(STATUTS_HORS_EN_COURS)
                .map(this::versProcessusEnCoursDto)
                .orElse(null);

        long processusClotureesCetteAnnee = processusMensuelRepository
                .countByStatutAndAnneePaiement(StatutEnum.CLOTURE, Year.now().getValue());

        return DashboardResponseDto.builder()
                .nombreBeneficiairesActifs(nombreBeneficiairesActifs)
                .montantTotalMensuel(montantTotalMensuel)
                .processusEnCours(processusEnCours)
                .processusClotureesCetteAnnee(processusClotureesCetteAnnee)
                .build();
    }

    private ProcessusEnCoursDto versProcessusEnCoursDto(ProcessusMensuel processus) {
        return ProcessusEnCoursDto.builder()
                .id(processus.getId())
                .statut(processus.getStatut())
                .build();
    }

    @Transactional(readOnly = true)
    public HistoriqueResponseDto historique(Integer annee) {
        List<ProcessusMensuel> processusClotures = annee != null
                ? processusMensuelRepository.findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(
                        StatutEnum.CLOTURE, annee)
                : processusMensuelRepository.findByStatutOrderByAnneePaiementDescMoisPaiementDesc(StatutEnum.CLOTURE);

        List<HistoriqueLigneDto> lignes = processusClotures.stream()
                .map(this::versHistoriqueLigneDto)
                .toList();

        return HistoriqueResponseDto.builder().lignes(lignes).build();
    }

    private HistoriqueLigneDto versHistoriqueLigneDto(ProcessusMensuel processus) {
        long montantTotal = ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(processus.getId());
        long nombreBeneficiaires = ligneEtatMensuelRepository.countByIdProcessusAndInclusDansEtatTrue(processus.getId());

        return HistoriqueLigneDto.builder()
                .moisPaiement(processus.getMoisPaiement())
                .anneePaiement(processus.getAnneePaiement())
                .statut(processus.getStatut())
                .montantTotal(montantTotal)
                .nombreBeneficiaires(nombreBeneficiaires)
                .build();
    }
}