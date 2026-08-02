package com.afriland.dottel.processus.service;

import com.afriland.dottel.processus.exception.EtapeWorkflowIntrouvableException;
import com.afriland.dottel.processus.exception.RoleEtapeNonAutoriseException;
import com.afriland.dottel.processus.exception.SeparationTachesViolationException;
import com.afriland.dottel.processus.model.entity.EtapeWorkflow;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.processus.model.enums.NomEtapeEnum;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.processus.model.enums.StatutEtapeEnum;
import com.afriland.dottel.processus.repository.EtapeWorkflowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeparationTachesServiceTest {

    @Mock
    private EtapeWorkflowRepository etapeWorkflowRepository;

    private SeparationTachesService separationTachesService;

    // MBARGA (ARH, id 1) a valide l'etape precedente, ESSAMA (CRH, id 2) verifie l'etape suivante.
    private EtapeWorkflow etapeArhValideeParMbarga() {
        return EtapeWorkflow.builder()
                .id(1L)
                .idProcessus(10L)
                .idActeur(1L)
                .ordreEtape(1)
                .nomEtape(NomEtapeEnum.VALIDATION_ARH)
                .statutEtape(StatutEtapeEnum.VALIDEE)
                .dateAction(LocalDateTime.now())
                .build();
    }

    // --- RG-05 : role attendu par etape declenchee ---

    @Test
    void verifierRoleAttendu_roleCorrespondALEtape_neLevePasException() {
        separationTachesService = new SeparationTachesService(etapeWorkflowRepository);

        Utilisateur mbargaArh = Utilisateur.builder().id(1L).matricule("1847").nom("MBARGA").prenom("Jean Paul")
                .role(RoleEnum.ARH).actif(true).build();
        Utilisateur essamaCrh = Utilisateur.builder().id(2L).matricule("2093").nom("ESSAMA").prenom("Marie Claire")
                .role(RoleEnum.CRH).actif(true).build();
        Utilisateur atanganaDrh = Utilisateur.builder().id(3L).matricule("1562").nom("ATANGANA").prenom("Paul")
                .role(RoleEnum.DRH).actif(true).build();

        assertThatCode(() -> {
            separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_ARH, mbargaArh);
            separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_CRH, essamaCrh);
            separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_DRH, atanganaDrh);
        }).doesNotThrowAnyException();
    }

    // Cas exact de la faille RG-05 relevee en recette : la DRH ATANGANA tente
    // de valider l'etape ARH (branche declenchee par un statut RETOURNE).
    @Test
    void verifierRoleAttendu_drhSurEtapeArh_leve403() {
        separationTachesService = new SeparationTachesService(etapeWorkflowRepository);

        Utilisateur atanganaDrh = Utilisateur.builder().id(3L).matricule("1562").nom("ATANGANA").prenom("Paul")
                .role(RoleEnum.DRH).actif(true).build();

        assertThatThrownBy(() -> separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_ARH, atanganaDrh))
                .isInstanceOf(RoleEtapeNonAutoriseException.class)
                .hasMessageContaining("VALIDATION_ARH")
                .hasMessageContaining("ARH");
    }

    @Test
    void verifierRoleAttendu_crhSurEtapeDrh_leve403() {
        separationTachesService = new SeparationTachesService(etapeWorkflowRepository);

        Utilisateur essamaCrh = Utilisateur.builder().id(2L).matricule("2093").nom("ESSAMA").prenom("Marie Claire")
                .role(RoleEnum.CRH).actif(true).build();

        assertThatThrownBy(() -> separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_DRH, essamaCrh))
                .isInstanceOf(RoleEtapeNonAutoriseException.class);
    }

    // --- RG-08 : separation des taches ---

    @Test
    void verifier_acteursDifferents_neLevePasException() {
        separationTachesService = new SeparationTachesService(etapeWorkflowRepository);

        when(etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(10L))
                .thenReturn(List.of(etapeArhValideeParMbarga()));

        assertThatCode(() -> separationTachesService.verifier(10L, 2L, NomEtapeEnum.VALIDATION_ARH))
                .doesNotThrowAnyException();
    }

    @Test
    void verifier_memeActeur_leve403() {
        separationTachesService = new SeparationTachesService(etapeWorkflowRepository);

        when(etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(10L))
                .thenReturn(List.of(etapeArhValideeParMbarga()));

        assertThatThrownBy(() -> separationTachesService.verifier(10L, 1L, NomEtapeEnum.VALIDATION_ARH))
                .isInstanceOf(SeparationTachesViolationException.class);
    }

    @Test
    void verifier_etapePrecedenteIntrouvable_leveExceptionExplicite() {
        separationTachesService = new SeparationTachesService(etapeWorkflowRepository);

        when(etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(10L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> separationTachesService.verifier(10L, 2L, NomEtapeEnum.VALIDATION_ARH))
                .isInstanceOf(EtapeWorkflowIntrouvableException.class);
    }
}