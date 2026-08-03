package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.api.BeneficiaireDotationDto;
import com.afriland.dottel.beneficiaires.api.BeneficiaireIdentiteDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Couverture de l'API du module beneficiaires (Sprint MM.2).
 *
 * Le point sensible est gradeDe() : il porte le comportement fail-closed de
 * RG-02 lors d'un ajustement, assertion qui vivait auparavant dans
 * ProcessusMensuelServiceTest via un stub direct du repository.
 */
@ExtendWith(MockitoExtension.class)
class BeneficiaireApiImplTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @InjectMocks
    private BeneficiaireApiImpl beneficiaireApi;

    @Test
    void listerActifsPourDotation_neRemonteQueLesQuatreChampsUtiles() {
        Beneficiaire nkolo = Beneficiaire.builder()
                .id(501L).matricule("3164").nomPrenoms("NKOLO Emmanuel").fonction("DA")
                .grade("Grade 5").uniteRattachement("Agence Akwa Douala").codeUnite("DLA01")
                .numCompteCourant("10005-00012-0034567891-12").actif(true).build();

        when(beneficiaireRepository.findByActifTrue()).thenReturn(List.of(nkolo));

        List<BeneficiaireDotationDto> actifs = beneficiaireApi.listerActifsPourDotation();

        assertThat(actifs).containsExactly(
                new BeneficiaireDotationDto(501L, "3164", "NKOLO Emmanuel", "DA"));
    }

    @Test
    void identitesParId_indexeParIdEtOmetLesIdsSansCorrespondance() {
        Beneficiaire essama = Beneficiaire.builder()
                .id(502L).matricule("5522").nomPrenoms("ESSAMA Solange")
                .fonction("CHEF_DEPARTEMENT").actif(true).build();

        when(beneficiaireRepository.findAllById(List.of(502L, 999L))).thenReturn(List.of(essama));

        Map<Long, BeneficiaireIdentiteDto> identites = beneficiaireApi.identitesParId(List.of(502L, 999L));

        assertThat(identites).containsExactly(
                Map.entry(502L, new BeneficiaireIdentiteDto(502L, "5522", "ESSAMA Solange")));
        // L'appelant obtient null pour un id absent, exactement comme avant MM.2.
        assertThat(identites.get(999L)).isNull();
    }

    @Test
    void gradeDe_beneficiaireAvecGrade_retourneLeGrade() {
        Beneficiaire atangana = Beneficiaire.builder()
                .id(521L).matricule("4275").nomPrenoms("ATANGANA Sylvie")
                .fonction("GFC").grade("Grade 2").actif(true).build();

        when(beneficiaireRepository.findById(521L)).thenReturn(Optional.of(atangana));

        assertThat(beneficiaireApi.gradeDe(521L)).contains("Grade 2");
    }

    // RG-02 fail-closed, cas 1 : le beneficiaire existe mais son grade n'est pas
    // renseigne (typiquement un import Excel, ou la colonne GRADE n'existe pas).
    @Test
    void gradeDe_gradeNonRenseigne_retourneVide() {
        Beneficiaire tchinda = Beneficiaire.builder()
                .id(511L).matricule("6180").nomPrenoms("TCHINDA Paul")
                .fonction("COMPTABLE").grade(null).actif(true).build();

        when(beneficiaireRepository.findById(511L)).thenReturn(Optional.of(tchinda));

        assertThat(beneficiaireApi.gradeDe(511L)).isEmpty();
    }

    // RG-02 fail-closed, cas 2 : beneficiaire introuvable. Aucune exception, sinon
    // un lot d'ajustements entier echouerait sur une seule ligne fautive (RG-11).
    @Test
    void gradeDe_beneficiaireIntrouvable_retourneVideSansLeverDException() {
        when(beneficiaireRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThat(beneficiaireApi.gradeDe(9999L)).isEmpty();
    }

    // Sprint MM.3, couplage C3 : comptage utilise avant desactivation d'une
    // fonction et comme garde-fou avant un renommage de code.
    @Test
    void compterActifsParFonction_delegueAuRepository() {
        when(beneficiaireRepository.countByFonctionAndActifTrue("GFC")).thenReturn(3L);

        assertThat(beneficiaireApi.compterActifsParFonction("GFC")).isEqualTo(3L);
    }

    // Sprint MM.3, couplage C3 : la cascade de renommage (deplacee depuis
    // FonctionEligibleService) doit toucher TOUS les beneficiaires portant
    // l'ancien code, y compris les inactifs -- pas seulement les actifs.
    @Test
    void renommerFonction_toucheLesBeneficiairesActifsEtInactifs() {
        Beneficiaire actif = Beneficiaire.builder().id(10L).matricule("1847").fonction("GFC").actif(true).build();
        Beneficiaire inactif = Beneficiaire.builder().id(11L).matricule("2093").fonction("GFC").actif(false).build();

        when(beneficiaireRepository.findByFonction("GFC")).thenReturn(List.of(actif, inactif));

        beneficiaireApi.renommerFonction("GFC", "GESTIONNAIRE_FDC");

        assertThat(actif.getFonction()).isEqualTo("GESTIONNAIRE_FDC");
        assertThat(inactif.getFonction()).isEqualTo("GESTIONNAIRE_FDC");
        org.mockito.Mockito.verify(beneficiaireRepository).saveAll(List.of(actif, inactif));
    }
}
