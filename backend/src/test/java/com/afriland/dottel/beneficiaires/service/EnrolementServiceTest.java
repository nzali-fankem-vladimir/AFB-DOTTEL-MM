package com.afriland.dottel.beneficiaires.service;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.referentiel.exception.GrilleTarifaireIntrouvableException;
import com.afriland.dottel.beneficiaires.exception.MatriculeDejaEnroleException;
import com.afriland.dottel.beneficiaires.exception.MatriculeInconnuException;
import com.afriland.dottel.beneficiaires.exception.NonEligibleException;
import com.afriland.dottel.beneficiaires.model.dto.ehr.EmployeEhrDto;
import com.afriland.dottel.beneficiaires.model.dto.enrolement.ConfirmerEnrolementRequestDto;
import com.afriland.dottel.beneficiaires.model.dto.enrolement.ConfirmerEnrolementResponseDto;
import com.afriland.dottel.beneficiaires.model.dto.enrolement.EnrolementVerificationResponseDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.referentiel.service.EligibiliteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrolementServiceTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private EhrIntegrationService ehrIntegrationService;

    @Mock
    private EligibiliteService eligibiliteService;

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private EnrolementService enrolementService;

    @Test
    void verifier_matriculeEligible_retourneReponseAvecEligibleTrue() {
        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("3164")
                .nom("NKOLO")
                .prenom("Emmanuel")
                .fonction("DA")
                .grade(null)
                .uniteRattachement("Agence Bafoussam Centre")
                .codeUnite("BFS-CTR")
                .numCompteCourant("10013164008")
                .build();
        FonctionEligible directeurAgence = FonctionEligible.builder()
                .code("DA")
                .libelle("Directeur d'Agence")
                .actif(true)
                .build();

        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("3164")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(directeurAgence));

        EnrolementVerificationResponseDto reponse = enrolementService.verifier("3164");

        assertThat(reponse.getMatricule()).isEqualTo("3164");
        assertThat(reponse.getNom()).isEqualTo("NKOLO");
        assertThat(reponse.getLibelleFonction()).isEqualTo("Directeur d'Agence");
        assertThat(reponse.isEligible()).isTrue();
    }

    @Test
    void verifier_matriculeDejaEnrole_leve409() {
        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(true);

        assertThatThrownBy(() -> enrolementService.verifier("3164"))
                .isInstanceOf(MatriculeDejaEnroleException.class);
    }

    @Test
    void verifier_matriculeInconnuDansEhr_leve404() {
        when(beneficiaireRepository.existsByMatricule("9999")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrolementService.verifier("9999"))
                .isInstanceOf(MatriculeInconnuException.class);
    }

    @Test
    void verifier_fonctionInactive_retourneEligibleFalse() {
        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("6497")
                .nom("FOUDA")
                .prenom("Alain")
                .fonction("AGENT_GUICHET")
                .grade(null)
                .uniteRattachement("Agence Ngaoundere")
                .codeUnite("NGD-AG01")
                .numCompteCourant("10016497001")
                .build();

        when(beneficiaireRepository.existsByMatricule("6497")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("6497")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("AGENT_GUICHET", null)).thenReturn(false);
        when(fonctionEligibleRepository.findByCode("AGENT_GUICHET")).thenReturn(Optional.empty());

        EnrolementVerificationResponseDto reponse = enrolementService.verifier("6497");

        assertThat(reponse.isEligible()).isFalse();
        assertThat(reponse.getLibelleFonction()).isNull();
    }

    @Test
    void verifier_corpsControleNonGrade_retourneEligibleFalse() {
        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("7508")
                .nom("BELINGA")
                .prenom("Christelle")
                .fonction("CONTROLEUR_COMPTABLE")
                .grade("NON GRADE")
                .uniteRattachement("Direction Controle Douala")
                .codeUnite("DLA-CTL")
                .numCompteCourant("10017508004")
                .build();
        FonctionEligible controleurComptable = FonctionEligible.builder()
                .code("CONTROLEUR_COMPTABLE")
                .libelle("Contrôleur Comptable")
                .actif(true)
                .build();

        when(beneficiaireRepository.existsByMatricule("7508")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("7508")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("CONTROLEUR_COMPTABLE", "NON GRADE")).thenReturn(false);
        when(fonctionEligibleRepository.findByCode("CONTROLEUR_COMPTABLE")).thenReturn(Optional.of(controleurComptable));

        EnrolementVerificationResponseDto reponse = enrolementService.verifier("7508");

        assertThat(reponse.isEligible()).isFalse();
        assertThat(reponse.getLibelleFonction()).isEqualTo("Contrôleur Comptable");
    }

    @Test
    void confirmer_matriculeEligibleAvecGrilleActive_creeBeneficiaireEtRetourneReponse() {
        ConfirmerEnrolementRequestDto requete = new ConfirmerEnrolementRequestDto();
        requete.setMatricule("3164");
        requete.setGrade(null);

        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("3164")
                .nom("NKOLO")
                .prenom("Emmanuel")
                .fonction("DA")
                .grade(null)
                .uniteRattachement("Agence Bafoussam Centre")
                .codeUnite("BFS-CTR")
                .numCompteCourant("10013164008")
                .build();
        FonctionEligible directeurAgence = FonctionEligible.builder()
                .id(5L)
                .code("DA")
                .libelle("Directeur d'Agence")
                .actif(true)
                .build();
        GrilleTarifaire grilleActive = GrilleTarifaire.builder()
                .id(1L)
                .idFonctionEligible(5L)
                .montantFcfa(50000)
                .statutValidation(StatutGrilleEnum.ACTIVE)
                .build();
        Utilisateur utilisateurConnecte = Utilisateur.builder().id(42L).matricule("3164").build();

        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("3164")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(directeurAgence));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(5L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleActive));
        when(beneficiaireRepository.save(any(Beneficiaire.class))).thenAnswer(invocation -> {
            Beneficiaire beneficiaire = invocation.getArgument(0);
            beneficiaire.setId(100L);
            return beneficiaire;
        });
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte);

        ConfirmerEnrolementResponseDto reponse = enrolementService.confirmer(requete);

        assertThat(reponse.getId()).isEqualTo(100L);
        assertThat(reponse.getMatricule()).isEqualTo("3164");
        assertThat(reponse.getNomPrenoms()).isEqualTo("NKOLO Emmanuel");
        assertThat(reponse.getFonction()).isEqualTo("DA");
        assertThat(reponse.getDateEnrolement()).isEqualTo(LocalDate.now());

        verify(auditService).enregistrer(eq(42L), eq("ENROLEMENT"), eq("beneficiaires"), eq(100L),
                isNull(), ArgumentMatchers.<Map<String, Object>>any());
    }

    @Test
    void confirmer_matriculeDejaEnrole_leve409() {
        ConfirmerEnrolementRequestDto requete = new ConfirmerEnrolementRequestDto();
        requete.setMatricule("3164");

        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(true);

        assertThatThrownBy(() -> enrolementService.confirmer(requete))
                .isInstanceOf(MatriculeDejaEnroleException.class);
    }

    @Test
    void confirmer_matriculeInconnuDansEhr_leve404() {
        ConfirmerEnrolementRequestDto requete = new ConfirmerEnrolementRequestDto();
        requete.setMatricule("9999");

        when(beneficiaireRepository.existsByMatricule("9999")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrolementService.confirmer(requete))
                .isInstanceOf(MatriculeInconnuException.class);
    }

    @Test
    void confirmer_nonEligible_leve403() {
        ConfirmerEnrolementRequestDto requete = new ConfirmerEnrolementRequestDto();
        requete.setMatricule("6497");

        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("6497")
                .nom("FOUDA")
                .prenom("Alain")
                .fonction("AGENT_GUICHET")
                .grade(null)
                .uniteRattachement("Agence Ngaoundere")
                .codeUnite("NGD-AG01")
                .numCompteCourant("10016497001")
                .build();

        when(beneficiaireRepository.existsByMatricule("6497")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("6497")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("AGENT_GUICHET", null)).thenReturn(false);

        assertThatThrownBy(() -> enrolementService.confirmer(requete))
                .isInstanceOf(NonEligibleException.class);

        verify(beneficiaireRepository, never()).save(any());
    }

    @Test
    void confirmer_grilleTarifaireAbsente_leve400() {
        ConfirmerEnrolementRequestDto requete = new ConfirmerEnrolementRequestDto();
        requete.setMatricule("3164");

        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("3164")
                .nom("NKOLO")
                .prenom("Emmanuel")
                .fonction("DA")
                .grade(null)
                .uniteRattachement("Agence Bafoussam Centre")
                .codeUnite("BFS-CTR")
                .numCompteCourant("10013164008")
                .build();
        FonctionEligible directeurAgence = FonctionEligible.builder()
                .id(5L)
                .code("DA")
                .libelle("Directeur d'Agence")
                .actif(true)
                .build();

        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("3164")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(directeurAgence));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(5L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrolementService.confirmer(requete))
                .isInstanceOf(GrilleTarifaireIntrouvableException.class);

        verify(beneficiaireRepository, never()).save(any());
    }

    @Test
    void confirmer_gradeDtoIgnoreMemeSiDifferentDeLehr_utiliseGradeEhr() {
        ConfirmerEnrolementRequestDto requete = new ConfirmerEnrolementRequestDto();
        requete.setMatricule("7508");
        requete.setGrade("Grade 4");

        EmployeEhrDto employeEhr = EmployeEhrDto.builder()
                .matricule("7508")
                .nom("BELINGA")
                .prenom("Christelle")
                .fonction("CONTROLEUR_COMPTABLE")
                .grade("NON GRADE")
                .uniteRattachement("Direction Controle Douala")
                .codeUnite("DLA-CTL")
                .numCompteCourant("10017508004")
                .build();

        when(beneficiaireRepository.existsByMatricule("7508")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("7508")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("CONTROLEUR_COMPTABLE", "NON GRADE")).thenReturn(false);

        assertThatThrownBy(() -> enrolementService.confirmer(requete))
                .isInstanceOf(NonEligibleException.class);

        verify(eligibiliteService).verifierEligibilite("CONTROLEUR_COMPTABLE", "NON GRADE");
        verify(beneficiaireRepository, never()).save(any());
    }
}