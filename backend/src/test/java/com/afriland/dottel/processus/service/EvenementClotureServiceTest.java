package com.afriland.dottel.processus.service;

import com.afriland.dottel.beneficiaires.api.BeneficiaireApi;
import com.afriland.dottel.beneficiaires.api.BeneficiaireClotureDto;
import com.afriland.dottel.processus.api.EvenementClotureDto;
import com.afriland.dottel.processus.api.LigneClotureDto;
import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvenementClotureServiceTest {

    @Mock
    private KafkaTemplate<String, EvenementClotureDto> kafkaTemplateEvenementCloture;

    @Mock
    private LigneEtatMensuelRepository ligneEtatMensuelRepository;

    @Mock
    private BeneficiaireApi beneficiaireApi;

    @InjectMocks
    private EvenementClotureService evenementClotureService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(evenementClotureService, "topicCloture", "dottel.processus.cloture");
        ReflectionTestUtils.setField(evenementClotureService, "chapitreDefaut", "37210100");
    }

    private ProcessusMensuel processusCloture() {
        return ProcessusMensuel.builder()
                .id(14L).moisPaiement(8).anneePaiement(2026).statut(StatutEnum.CLOTURE)
                .dateCloture(LocalDateTime.of(2026, 8, 9, 21, 45)).build();
    }

    private LigneEtatMensuel ligne(Long idBeneficiaire, String fonction, int montant, boolean inclus) {
        return LigneEtatMensuel.builder()
                .id(idBeneficiaire + 1000).idProcessus(14L).idBeneficiaire(idBeneficiaire)
                .fonctionRetenue(fonction).montantApplique(montant).inclusDansEtat(inclus).build();
    }

    private EvenementClotureDto evenementPublie() {
        ArgumentCaptor<EvenementClotureDto> captor = ArgumentCaptor.forClass(EvenementClotureDto.class);
        verify(kafkaTemplateEvenementCloture).send(eq("dottel.processus.cloture"), eq("14"), captor.capture());
        return captor.getValue();
    }

    @Test
    void publier_conserveLesChampsAgregesEtAjouteUneLigneParBeneficiaireInclus() {
        LigneEtatMensuel mbarga = ligne(510L, "GFC", 40000, true);
        LigneEtatMensuel essama = ligne(511L, "DA", 50000, true);

        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(14L))
                .thenReturn(List.of(mbarga, essama));
        when(beneficiaireApi.donneesClotureParId(anyCollection())).thenReturn(Map.of(
                510L, new BeneficiaireClotureDto(510L, "MBARGA Jean Paul", "4060", "10001",
                        "10001000123456789", "37210100"),
                511L, new BeneficiaireClotureDto(511L, "ESSAMA Marie-Claire", "4021", "10003",
                        "10003000987654321", "37210100")));

        evenementClotureService.publier(processusCloture(), 90000L);

        EvenementClotureDto evenement = evenementPublie();

        assertThat(evenement.getIdProcessus()).isEqualTo(14L);
        assertThat(evenement.getMoisPaiement()).isEqualTo(8);
        assertThat(evenement.getAnneePaiement()).isEqualTo(2026);
        assertThat(evenement.getMontantTotal()).isEqualTo(90000L);
        assertThat(evenement.getDateCloture()).isEqualTo(LocalDateTime.of(2026, 8, 9, 21, 45));

        assertThat(evenement.getLignes())
                .extracting(LigneClotureDto::getNomPrenoms, LigneClotureDto::getCodeUnite,
                        LigneClotureDto::getCodeAgence, LigneClotureDto::getNumCompteCourant,
                        LigneClotureDto::getFonctionRetenue, LigneClotureDto::getMontantAttribue)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple("MBARGA Jean Paul", "4060", "10001",
                                "10001000123456789", "GFC", 40000L),
                        org.assertj.core.api.Assertions.tuple("ESSAMA Marie-Claire", "4021", "10003",
                                "10003000987654321", "DA", 50000L));
    }

    // Un beneficiaire exclu n'a pas ete paye : une ecriture comptable le
    // concernant serait fausse.
    @Test
    void publier_beneficiaireExclu_neFigurePasDansLePayload() {
        LigneEtatMensuel incluse = ligne(510L, "GFC", 40000, true);

        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(14L))
                .thenReturn(List.of(incluse));
        when(beneficiaireApi.donneesClotureParId(anyCollection())).thenReturn(Map.of(
                510L, new BeneficiaireClotureDto(510L, "MBARGA Jean Paul", "4060", "10001",
                        "10001000123456789", "37210100")));

        evenementClotureService.publier(processusCloture(), 40000L);

        assertThat(evenementPublie().getLignes())
                .singleElement()
                .satisfies(ligne -> assertThat(ligne.getNomPrenoms()).isEqualTo("MBARGA Jean Paul"));
    }

    // Meme repli que DocumentService, sur la meme propriete : la donnee EHR
    // chapitre manque aux beneficiaires enroles avant son introduction.
    @Test
    void publier_chapitreAbsent_repliSurLaValeurParDefaut() {
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(14L))
                .thenReturn(List.of(ligne(512L, "COMPTABLE", 35000, true)));
        when(beneficiaireApi.donneesClotureParId(anyCollection())).thenReturn(Map.of(
                512L, new BeneficiaireClotureDto(512L, "NKOLO Sylvie", "4033", "10007",
                        "10007000456789123", null)));

        evenementClotureService.publier(processusCloture(), 35000L);

        assertThat(evenementPublie().getLignes()).singleElement()
                .satisfies(ligne -> assertThat(ligne.getChapitre()).isEqualTo("37210100"));
    }

    // Un processus porte plusieurs dizaines de beneficiaires : un appel par
    // ligne serait un N+1.
    @Test
    void publier_recupereLesBeneficiairesEnUnSeulAppel() {
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(14L)).thenReturn(List.of(
                ligne(510L, "GFC", 40000, true),
                ligne(511L, "DA", 50000, true),
                ligne(512L, "COMPTABLE", 35000, true)));
        when(beneficiaireApi.donneesClotureParId(anyCollection())).thenReturn(Map.of());

        evenementClotureService.publier(processusCloture(), 125000L);

        verify(beneficiaireApi, times(1)).donneesClotureParId(any());
    }
}
