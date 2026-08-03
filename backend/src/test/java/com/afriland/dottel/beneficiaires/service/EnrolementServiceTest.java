package com.afriland.dottel.beneficiaires.service;
import com.afriland.dottel.utilisateurs.api.AuthenticatedUserService;
import com.afriland.dottel.audit.api.EvenementAudit;

import com.afriland.dottel.referentiel.api.GrilleTarifaireApi;
import com.afriland.dottel.referentiel.api.ResolutionGrilleDto;
import com.afriland.dottel.referentiel.exception.GrilleTarifaireIntrouvableException;
import com.afriland.dottel.beneficiaires.exception.MatriculeDejaEnroleException;
import com.afriland.dottel.beneficiaires.exception.MatriculeInconnuException;
import com.afriland.dottel.beneficiaires.exception.NonEligibleException;
import com.afriland.dottel.beneficiaires.model.dto.ehr.EmployeEhrDto;
import com.afriland.dottel.beneficiaires.model.dto.enrolement.ConfirmerEnrolementRequestDto;
import com.afriland.dottel.beneficiaires.model.dto.enrolement.ConfirmerEnrolementResponseDto;
import com.afriland.dottel.beneficiaires.model.dto.enrolement.EnrolementVerificationResponseDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.referentiel.api.EligibiliteService;
import com.afriland.dottel.referentiel.api.FonctionEligibleApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private FonctionEligibleApi fonctionEligibleService;

    @Mock
    private GrilleTarifaireApi grilleTarifaireApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("3164")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(fonctionEligibleService.libelle("DA")).thenReturn(Optional.of("Directeur d'Agence"));

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
        when(fonctionEligibleService.libelle("AGENT_GUICHET")).thenReturn(Optional.empty());

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
        when(beneficiaireRepository.existsByMatricule("7508")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("7508")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("CONTROLEUR_COMPTABLE", "NON GRADE")).thenReturn(false);
        when(fonctionEligibleService.libelle("CONTROLEUR_COMPTABLE")).thenReturn(Optional.of("Contrôleur Comptable"));

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
        Utilisateur utilisateurConnecte = Utilisateur.builder().id(42L).matricule("3164").build();

        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("3164")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(grilleTarifaireApi.resoudrePourFonction("DA")).thenReturn(ResolutionGrilleDto.resolue(50000));
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

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(42L);
        assertThat(evenement.action()).isEqualTo("ENROLEMENT");
        assertThat(evenement.entiteCible()).isEqualTo("beneficiaires");
        assertThat(evenement.idEntite()).isEqualTo(100L);
        assertThat(evenement.avant()).isNull();
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
        when(beneficiaireRepository.existsByMatricule("3164")).thenReturn(false);
        when(ehrIntegrationService.rechercherEmploye("3164")).thenReturn(Optional.of(employeEhr));
        when(eligibiliteService.verifierEligibilite("DA", null)).thenReturn(true);
        when(grilleTarifaireApi.resoudrePourFonction("DA"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE));

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