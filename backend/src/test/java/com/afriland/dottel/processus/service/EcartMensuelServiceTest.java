package com.afriland.dottel.processus.service;

import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.processus.repository.ProcessusMensuelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EcartMensuelServiceTest {

    @Mock
    private ProcessusMensuelRepository processusMensuelRepository;

    @Mock
    private LigneEtatMensuelRepository ligneEtatMensuelRepository;

    private EcartMensuelService ecartMensuelService;

    private ProcessusMensuel processusCourant(int mois, int annee) {
        return ProcessusMensuel.builder().id(2L).moisPaiement(mois).anneePaiement(annee).build();
    }

    @Test
    void calculerEcart_aucunProcessusPrecedent_retourneNull() {
        ecartMensuelService = new EcartMensuelService(processusMensuelRepository, ligneEtatMensuelRepository);
        ProcessusMensuel processus = processusCourant(3, 2026);
        LigneEtatMensuel ligneCourante = LigneEtatMensuel.builder()
                .idBeneficiaire(10L).fonctionRetenue("GFC").montantApplique(40000).build();

        when(processusMensuelRepository.findByMoisPaiementAndAnneePaiementAndRattrapageFalse(2, 2026))
                .thenReturn(Optional.empty());

        Integer ecart = ecartMensuelService.calculerEcart(processus, ligneCourante);

        assertThat(ecart).isNull();
    }

    @Test
    void calculerEcart_fonctionIdentiqueAuMoisPrecedent_retourneNull() {
        ecartMensuelService = new EcartMensuelService(processusMensuelRepository, ligneEtatMensuelRepository);
        ProcessusMensuel processus = processusCourant(3, 2026);
        ProcessusMensuel processusPrecedent = ProcessusMensuel.builder().id(1L).moisPaiement(2).anneePaiement(2026).build();
        LigneEtatMensuel ligneCourante = LigneEtatMensuel.builder()
                .idBeneficiaire(10L).fonctionRetenue("GFC").montantApplique(40000).build();
        LigneEtatMensuel lignePrecedente = LigneEtatMensuel.builder()
                .idBeneficiaire(10L).fonctionRetenue("GFC").montantApplique(40000).build();

        when(processusMensuelRepository.findByMoisPaiementAndAnneePaiementAndRattrapageFalse(2, 2026))
                .thenReturn(Optional.of(processusPrecedent));
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(1L, 10L))
                .thenReturn(Optional.of(lignePrecedente));

        Integer ecart = ecartMensuelService.calculerEcart(processus, ligneCourante);

        assertThat(ecart).isNull();
    }

    @Test
    void calculerEcart_fonctionDifferenteDuMoisPrecedent_retourneDeltaMontant() {
        // Cas metier de reference : un GFC devenu Directeur d'Agence.
        ecartMensuelService = new EcartMensuelService(processusMensuelRepository, ligneEtatMensuelRepository);
        ProcessusMensuel processus = processusCourant(1, 2026);
        ProcessusMensuel processusPrecedent = ProcessusMensuel.builder().id(1L).moisPaiement(12).anneePaiement(2025).build();
        LigneEtatMensuel ligneCourante = LigneEtatMensuel.builder()
                .idBeneficiaire(10L).fonctionRetenue("DA").montantApplique(50000).build();
        LigneEtatMensuel lignePrecedente = LigneEtatMensuel.builder()
                .idBeneficiaire(10L).fonctionRetenue("GFC").montantApplique(40000).build();

        when(processusMensuelRepository.findByMoisPaiementAndAnneePaiementAndRattrapageFalse(12, 2025))
                .thenReturn(Optional.of(processusPrecedent));
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(1L, 10L))
                .thenReturn(Optional.of(lignePrecedente));

        Integer ecart = ecartMensuelService.calculerEcart(processus, ligneCourante);

        assertThat(ecart).isEqualTo(10000);
    }
}