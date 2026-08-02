package com.afriland.dottel.referentiel.service;

import com.afriland.dottel.referentiel.api.ResolutionGrilleDto;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Couverture de RG-01 et RG-04 pour l'API de resolution de grille (Sprint MM.2).
 *
 * Ces assertions vivaient auparavant dans ProcessusMensuelServiceTest, qui
 * stubbait directement les deux repositories. Le refactor MM.2 ayant deplace la
 * resolution ici, la couverture la suit : sans ces tests, la clause
 * "statut ACTIVE et dateFin IS NULL" ne serait plus verifiee nulle part pour le
 * chemin du processus mensuel.
 */
@ExtendWith(MockitoExtension.class)
class GrilleTarifaireApiImplTest {

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @InjectMocks
    private GrilleTarifaireApiImpl grilleTarifaireApi;

    // RG-04 : le montant vient de la grille ACTIVE sans date de fin, jamais d'une
    // valeur en dur. La verification porte aussi sur les ARGUMENTS de la requete :
    // c'est la seule chose qui distingue une grille courante d'une grille close.
    @Test
    void resoudrePourFonction_grilleActiveSansDateFin_retourneLeMontant() {
        FonctionEligible directeurAgence = FonctionEligible.builder()
                .id(5L).code("DA").libelle("Directeur d'Agence").actif(true).build();
        GrilleTarifaire grilleDa = GrilleTarifaire.builder()
                .id(1L).idFonctionEligible(5L).montantFcfa(50000)
                .statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(directeurAgence));
        when(grilleTarifaireRepository
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(5L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleDa));

        ResolutionGrilleDto resolution = grilleTarifaireApi.resoudrePourFonction("DA");

        assertThat(resolution.estResolue()).isTrue();
        assertThat(resolution.montantFcfa()).isEqualTo(50000);
        assertThat(resolution.motifExclusion()).isNull();

        verify(grilleTarifaireRepository)
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(5L, StatutGrilleEnum.ACTIVE);
    }

    // RG-01 : fonction absente du referentiel. La grille n'est jamais interrogee,
    // l'ordre de verification etant impose.
    @Test
    void resoudrePourFonction_fonctionInconnue_retourneLeMotifDedieSansInterrogerLaGrille() {
        when(fonctionEligibleRepository.findByCode("FONCTION_INEXISTANTE")).thenReturn(Optional.empty());

        ResolutionGrilleDto resolution = grilleTarifaireApi.resoudrePourFonction("FONCTION_INEXISTANTE");

        assertThat(resolution.estResolue()).isFalse();
        assertThat(resolution.montantFcfa()).isNull();
        assertThat(resolution.motifExclusion()).isEqualTo(ResolutionGrilleDto.MOTIF_FONCTION_INCONNUE);

        verify(grilleTarifaireRepository, never())
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(any(), any());
    }

    // RG-01 : fonction connue mais desactivee par l'ADMIN.
    @Test
    void resoudrePourFonction_fonctionDesactivee_retourneLeMotifDedieSansInterrogerLaGrille() {
        FonctionEligible comptableDesactive = FonctionEligible.builder()
                .id(23L).code("COMPTABLE").libelle("Comptable").actif(false).build();

        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptableDesactive));

        ResolutionGrilleDto resolution = grilleTarifaireApi.resoudrePourFonction("COMPTABLE");

        assertThat(resolution.estResolue()).isFalse();
        assertThat(resolution.motifExclusion()).isEqualTo(ResolutionGrilleDto.MOTIF_FONCTION_DESACTIVEE);

        verify(grilleTarifaireRepository, never())
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(any(), any());
    }

    // RG-04 : fonction active mais aucune grille ACTIVE courante -- le montant ne
    // doit surtout pas etre devine.
    @Test
    void resoudrePourFonction_aucuneGrilleActive_retourneLeMotifDedie() {
        FonctionEligible agentRecouvrement = FonctionEligible.builder()
                .id(20L).code("JURISTE").libelle("Agent de Recouvrement").actif(true).build();

        when(fonctionEligibleRepository.findByCode("JURISTE")).thenReturn(Optional.of(agentRecouvrement));
        when(grilleTarifaireRepository
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(20L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.empty());

        ResolutionGrilleDto resolution = grilleTarifaireApi.resoudrePourFonction("JURISTE");

        assertThat(resolution.estResolue()).isFalse();
        assertThat(resolution.montantFcfa()).isNull();
        assertThat(resolution.motifExclusion()).isEqualTo(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE);
    }

    // Les trois libelles remontent tels quels dans les reponses HTTP
    // (BeneficiaireExcluDto.motif, ResultatAjustementDto.motifRejet) : les figer
    // ici evite qu'une reformulation casse silencieusement le contrat API.
    @Test
    void motifsDExclusion_libellesConformesAuContratApi() {
        assertThat(ResolutionGrilleDto.MOTIF_FONCTION_INCONNUE)
                .isEqualTo("Fonction inconnue du référentiel fonction_eligible");
        assertThat(ResolutionGrilleDto.MOTIF_FONCTION_DESACTIVEE)
                .isEqualTo("Fonction désactivée");
        assertThat(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE)
                .isEqualTo("Aucune grille tarifaire ACTIVE pour cette fonction");
    }
}
