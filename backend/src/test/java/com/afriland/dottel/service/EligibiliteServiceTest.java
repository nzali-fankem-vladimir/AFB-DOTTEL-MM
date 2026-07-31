package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.service.rules.EligibiliteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EligibiliteServiceTest {

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @InjectMocks
    private EligibiliteService eligibiliteService;

    @Test
    void verifierEligibilite_fonctionActiveNonControle_retourneEligible() {
        FonctionEligible chefAgence = FonctionEligible.builder()
                .code("DA")
                .libelle("Directeur d'Agence")
                .actif(true)
                .build();
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(chefAgence));

        boolean resultat = eligibiliteService.verifierEligibilite("DA", null);

        assertThat(resultat).isTrue();
    }

    @Test
    void verifierEligibilite_fonctionInactive_retourneNonEligible() {
        FonctionEligible fonctionSupprimee = FonctionEligible.builder()
                .code("JURISTE")
                .libelle("Agent de Recouvrement")
                .actif(false)
                .build();
        when(fonctionEligibleRepository.findByCode("JURISTE")).thenReturn(Optional.of(fonctionSupprimee));

        boolean resultat = eligibiliteService.verifierEligibilite("JURISTE", null);

        assertThat(resultat).isFalse();
    }

    @Test
    void verifierEligibilite_fonctionInconnue_retourneNonEligible() {
        when(fonctionEligibleRepository.findByCode("STAGIAIRE_INEXISTANT")).thenReturn(Optional.empty());

        boolean resultat = eligibiliteService.verifierEligibilite("STAGIAIRE_INEXISTANT", null);

        assertThat(resultat).isFalse();
    }

    @Test
    void verifierEligibilite_corpsControleNonGrade_retourneNonEligible() {
        FonctionEligible controleurComptable = FonctionEligible.builder()
                .code("CONTROLEUR_COMPTABLE")
                .libelle("Contrôleur Comptable")
                .actif(true)
                .build();
        when(fonctionEligibleRepository.findByCode("CONTROLEUR_COMPTABLE")).thenReturn(Optional.of(controleurComptable));

        boolean resultat = eligibiliteService.verifierEligibilite("CONTROLEUR_COMPTABLE", "NON GRADE");

        assertThat(resultat).isFalse();
    }

    @Test
    void verifierEligibilite_corpsControleAvecGrade_retourneEligible() {
        FonctionEligible inspecteurGeneral = FonctionEligible.builder()
                .code("CORPS_CONTROLE_IG")
                .libelle("Inspecteur Général")
                .actif(true)
                .build();
        when(fonctionEligibleRepository.findByCode("CORPS_CONTROLE_IG")).thenReturn(Optional.of(inspecteurGeneral));

        boolean resultat = eligibiliteService.verifierEligibilite("CORPS_CONTROLE_IG", "GR10");

        assertThat(resultat).isTrue();
    }

    @Test
    void verifierEligibilite_corpsControleGradeNull_retourneNonEligible() {
        FonctionEligible comptable = FonctionEligible.builder()
                .code("COMPTABLE")
                .libelle("Comptable")
                .actif(true)
                .build();
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptable));

        boolean resultat = eligibiliteService.verifierEligibilite("COMPTABLE", null);

        assertThat(resultat).isFalse();
    }

    @Test
    void verifierEligibilite_corpsControleGradeVide_retourneNonEligible() {
        FonctionEligible comptable = FonctionEligible.builder()
                .code("COMPTABLE")
                .libelle("Comptable")
                .actif(true)
                .build();
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptable));

        assertThat(eligibiliteService.verifierEligibilite("COMPTABLE", "")).isFalse();
        assertThat(eligibiliteService.verifierEligibilite("COMPTABLE", "   ")).isFalse();
    }

    @Test
    void verifierEligibilite_corpsControleGradeCasseDifferente_retourneNonEligible() {
        FonctionEligible comptable = FonctionEligible.builder()
                .code("COMPTABLE")
                .libelle("Comptable")
                .actif(true)
                .build();
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptable));

        boolean resultat = eligibiliteService.verifierEligibilite("COMPTABLE", "non grade");

        assertThat(resultat).isFalse();
    }
}