package com.afriland.dottel.service;

import com.afriland.dottel.exception.MotifRejetObligatoireException;
import com.afriland.dottel.exception.PieceJointeIntrouvableException;
import com.afriland.dottel.exception.ProcessusMensuelExisteDejaException;
import com.afriland.dottel.exception.ProcessusMensuelIntrouvableException;
import com.afriland.dottel.exception.ProcessusMensuelNonModifiableException;
import com.afriland.dottel.exception.RoleEtapeNonAutoriseException;
import com.afriland.dottel.exception.SeparationTachesViolationException;
import com.afriland.dottel.model.dto.processus.AjustementLigneEtatDto;
import com.afriland.dottel.model.dto.processus.DeclencherProcessusRequestDto;
import com.afriland.dottel.model.dto.processus.PatchProcessusRequestDto;
import com.afriland.dottel.model.dto.processus.PatchProcessusResponseDto;
import com.afriland.dottel.model.dto.processus.PieceJointeMetadonneesResponseDto;
import com.afriland.dottel.model.dto.processus.ProcessusDetailResponseDto;
import com.afriland.dottel.model.dto.processus.ProcessusListItemDto;
import com.afriland.dottel.model.dto.processus.ProcessusMensuelResponseDto;
import com.afriland.dottel.model.dto.processus.ResultatAjustementDto;
import com.afriland.dottel.model.dto.processus.RetournerProcessusRequestDto;
import com.afriland.dottel.model.dto.processus.RetournerProcessusResponseDto;
import com.afriland.dottel.model.dto.processus.ValiderProcessusResponseDto;
import com.afriland.dottel.model.entity.Beneficiaire;
import com.afriland.dottel.model.entity.EtapeWorkflow;
import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.model.entity.GrilleTarifaire;
import com.afriland.dottel.model.entity.LigneEtatMensuel;
import com.afriland.dottel.model.entity.PieceJointe;
import com.afriland.dottel.model.entity.ProcessusMensuel;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.NomEtapeEnum;
import com.afriland.dottel.model.enums.RoleEnum;
import com.afriland.dottel.model.enums.StatutEnum;
import com.afriland.dottel.model.enums.StatutEtapeEnum;
import com.afriland.dottel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.EtapeWorkflowRepository;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.repository.PieceJointeRepository;
import com.afriland.dottel.repository.ProcessusMensuelRepository;
import com.afriland.dottel.repository.UtilisateurRepository;
import com.afriland.dottel.service.rules.EligibiliteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessusMensuelServiceTest {

    @Mock
    private ProcessusMensuelRepository processusMensuelRepository;

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private FonctionEligibleRepository fonctionEligibleRepository;

    @Mock
    private GrilleTarifaireRepository grilleTarifaireRepository;

    @Mock
    private LigneEtatMensuelRepository ligneEtatMensuelRepository;

    @Mock
    private EtapeWorkflowRepository etapeWorkflowRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PieceJointeRepository pieceJointeRepository;

    @Mock
    private AuditService auditService;

    // Attention : un mock Mockito renvoie false par defaut sur un boolean. Tout
    // test d'ajustement qui traverse la revalidation RG-02 doit donc stubber
    // explicitement verifierEligibilite(...) -> true, sinon la ligne est rejetee.
    @Mock
    private EligibiliteService eligibiliteService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private DocumentService documentService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SeparationTachesService separationTachesService;

    @Mock
    private EvenementClotureService evenementClotureService;

    @InjectMocks
    private ProcessusMensuelService processusMensuelService;

    @Test
    void lister_casNominal_retourneLesProcessusTriesRecentDabord() {
        ProcessusMensuel juillet2026 = ProcessusMensuel.builder()
                .id(12L).moisPaiement(7).anneePaiement(2026).statut(StatutEnum.EN_ATTENTE_DRH).build();
        ProcessusMensuel juin2026 = ProcessusMensuel.builder()
                .id(11L).moisPaiement(6).anneePaiement(2026).statut(StatutEnum.CLOTURE).build();

        when(processusMensuelRepository.findAllByOrderByAnneePaiementDescMoisPaiementDesc())
                .thenReturn(List.of(juillet2026, juin2026));

        List<ProcessusListItemDto> resultat = processusMensuelService.lister(null, null);

        assertThat(resultat).extracting(
                ProcessusListItemDto::getId, ProcessusListItemDto::getMoisPaiement, ProcessusListItemDto::getStatut
        ).containsExactly(
                tuple(12L, 7, StatutEnum.EN_ATTENTE_DRH),
                tuple(11L, 6, StatutEnum.CLOTURE)
        );
    }

    @Test
    void lister_aucunProcessus_retourneUneListeVide() {
        when(processusMensuelRepository.findAllByOrderByAnneePaiementDescMoisPaiementDesc())
                .thenReturn(List.of());

        List<ProcessusListItemDto> resultat = processusMensuelService.lister(null, null);

        assertThat(resultat).isEmpty();
    }

    @Test
    void lister_filtreParStatutSeul_appelleLaRequeteDediee() {
        ProcessusMensuel processus = ProcessusMensuel.builder()
                .id(11L).moisPaiement(6).anneePaiement(2026).statut(StatutEnum.CLOTURE).build();

        when(processusMensuelRepository.findByStatutOrderByAnneePaiementDescMoisPaiementDesc(StatutEnum.CLOTURE))
                .thenReturn(List.of(processus));

        List<ProcessusListItemDto> resultat = processusMensuelService.lister(StatutEnum.CLOTURE, null);

        assertThat(resultat).extracting(ProcessusListItemDto::getId).containsExactly(11L);
        verify(processusMensuelRepository, never()).findAllByOrderByAnneePaiementDescMoisPaiementDesc();
    }

    @Test
    void lister_filtreParAnneeSeule_appelleLaRequeteDediee() {
        ProcessusMensuel processus = ProcessusMensuel.builder()
                .id(12L).moisPaiement(7).anneePaiement(2026).statut(StatutEnum.EN_ATTENTE_DRH).build();

        when(processusMensuelRepository.findByAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(2026))
                .thenReturn(List.of(processus));

        List<ProcessusListItemDto> resultat = processusMensuelService.lister(null, 2026);

        assertThat(resultat).extracting(ProcessusListItemDto::getId).containsExactly(12L);
        verify(processusMensuelRepository, never()).findAllByOrderByAnneePaiementDescMoisPaiementDesc();
    }

    @Test
    void lister_filtreParStatutEtAnnee_appelleLaRequeteCombinee() {
        ProcessusMensuel processus = ProcessusMensuel.builder()
                .id(13L).moisPaiement(8).anneePaiement(2026).statut(StatutEnum.EN_ATTENTE_CRH).build();

        when(processusMensuelRepository.findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(
                StatutEnum.EN_ATTENTE_CRH, 2026)).thenReturn(List.of(processus));

        List<ProcessusListItemDto> resultat = processusMensuelService.lister(StatutEnum.EN_ATTENTE_CRH, 2026);

        assertThat(resultat).extracting(ProcessusListItemDto::getId).containsExactly(13L);
    }

    @Test
    void declencher_casNominal_creeLeProcessusEtLesLignes() {
        DeclencherProcessusRequestDto requete = new DeclencherProcessusRequestDto();
        requete.setMoisPaiement(7);
        requete.setAnneePaiement(2026);

        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        Beneficiaire nkolo = Beneficiaire.builder()
                .id(501L)
                .matricule("3164")
                .nomPrenoms("NKOLO Emmanuel")
                .fonction("DA")
                .actif(true)
                .build();
        Beneficiaire essama = Beneficiaire.builder()
                .id(502L)
                .matricule("5522")
                .nomPrenoms("ESSAMA Solange")
                .fonction("CHEF_DEPARTEMENT")
                .actif(true)
                .build();

        FonctionEligible directeurAgence = FonctionEligible.builder().id(5L).code("DA").libelle("Directeur d'Agence").actif(true).build();
        FonctionEligible chefDepartement = FonctionEligible.builder().id(8L).code("CHEF_DEPARTEMENT").libelle("Chef de Département").actif(true).build();

        GrilleTarifaire grilleDa = GrilleTarifaire.builder().id(1L).idFonctionEligible(5L).montantFcfa(50000).statutValidation(StatutGrilleEnum.ACTIVE).build();
        GrilleTarifaire grilleChefDept = GrilleTarifaire.builder().id(2L).idFonctionEligible(8L).montantFcfa(40000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(7, 2026)).thenReturn(false);
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> {
            ProcessusMensuel processus = invocation.getArgument(0);
            processus.setId(900L);
            return processus;
        });
        when(beneficiaireRepository.findByActifTrue()).thenReturn(List.of(nkolo, essama));
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(directeurAgence));
        when(fonctionEligibleRepository.findByCode("CHEF_DEPARTEMENT")).thenReturn(Optional.of(chefDepartement));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(5L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleDa));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(8L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleChefDept));
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessusMensuelResponseDto reponse = processusMensuelService.declencher(requete);

        assertThat(reponse.getId()).isEqualTo(900L);
        assertThat(reponse.getMoisPaiement()).isEqualTo(7);
        assertThat(reponse.getAnneePaiement()).isEqualTo(2026);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.EN_COURS_ARH);
        assertThat(reponse.getNombreBeneficiaires()).isEqualTo(2);
        assertThat(reponse.getBeneficiairesExclus()).isEmpty();

        ArgumentCaptor<LigneEtatMensuel> ligneCaptor = ArgumentCaptor.forClass(LigneEtatMensuel.class);
        verify(ligneEtatMensuelRepository, org.mockito.Mockito.times(2)).save(ligneCaptor.capture());
        List<LigneEtatMensuel> lignesSauvegardees = ligneCaptor.getAllValues();
        assertThat(lignesSauvegardees).allMatch(LigneEtatMensuel::getInclusDansEtat);
        assertThat(lignesSauvegardees).extracting(LigneEtatMensuel::getMontantApplique).containsExactlyInAnyOrder(50000, 40000);

        verify(auditService).enregistrer(org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq("DECLENCHEMENT_PROCESSUS"),
                org.mockito.ArgumentMatchers.eq("processus_mensuel"),
                org.mockito.ArgumentMatchers.eq(900L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void declencher_moisAnneeDejaExistant_leve409() {
        DeclencherProcessusRequestDto requete = new DeclencherProcessusRequestDto();
        requete.setMoisPaiement(7);
        requete.setAnneePaiement(2026);

        when(processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(7, 2026)).thenReturn(true);

        assertThatThrownBy(() -> processusMensuelService.declencher(requete))
                .isInstanceOf(ProcessusMensuelExisteDejaException.class);

        verify(processusMensuelRepository, never()).save(any());
        verify(beneficiaireRepository, never()).findByActifTrue();
    }

    @Test
    void declencher_aucunBeneficiaireActif_creeProcessusVide() {
        DeclencherProcessusRequestDto requete = new DeclencherProcessusRequestDto();
        requete.setMoisPaiement(8);
        requete.setAnneePaiement(2026);

        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        when(processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(8, 2026)).thenReturn(false);
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> {
            ProcessusMensuel processus = invocation.getArgument(0);
            processus.setId(901L);
            return processus;
        });
        when(beneficiaireRepository.findByActifTrue()).thenReturn(List.of());

        ProcessusMensuelResponseDto reponse = processusMensuelService.declencher(requete);

        assertThat(reponse.getId()).isEqualTo(901L);
        assertThat(reponse.getNombreBeneficiaires()).isZero();
        assertThat(reponse.getBeneficiairesExclus()).isEmpty();

        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(auditService).enregistrer(org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq("DECLENCHEMENT_PROCESSUS"),
                org.mockito.ArgumentMatchers.eq("processus_mensuel"),
                org.mockito.ArgumentMatchers.eq(901L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void declencher_beneficiaireSansGrilleActive_appliqueLaDecisionEtape3() {
        DeclencherProcessusRequestDto requete = new DeclencherProcessusRequestDto();
        requete.setMoisPaiement(9);
        requete.setAnneePaiement(2026);

        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        Beneficiaire tchinda = Beneficiaire.builder()
                .id(503L)
                .matricule("6633")
                .nomPrenoms("TCHINDA Paul")
                .fonction("JURISTE")
                .actif(true)
                .build();

        FonctionEligible agentRecouvrement = FonctionEligible.builder().id(20L).code("JURISTE").libelle("Agent de Recouvrement").actif(true).build();

        when(processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(9, 2026)).thenReturn(false);
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> {
            ProcessusMensuel processus = invocation.getArgument(0);
            processus.setId(902L);
            return processus;
        });
        when(beneficiaireRepository.findByActifTrue()).thenReturn(List.of(tchinda));
        when(fonctionEligibleRepository.findByCode("JURISTE")).thenReturn(Optional.of(agentRecouvrement));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(20L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.empty());
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessusMensuelResponseDto reponse = processusMensuelService.declencher(requete);

        assertThat(reponse.getNombreBeneficiaires()).isZero();
        assertThat(reponse.getBeneficiairesExclus()).hasSize(1);
        assertThat(reponse.getBeneficiairesExclus().get(0).getMatricule()).isEqualTo("6633");
        assertThat(reponse.getBeneficiairesExclus().get(0).getMotif())
                .isEqualTo("Aucune grille tarifaire ACTIVE pour cette fonction");

        ArgumentCaptor<LigneEtatMensuel> ligneCaptor = ArgumentCaptor.forClass(LigneEtatMensuel.class);
        verify(ligneEtatMensuelRepository).save(ligneCaptor.capture());
        LigneEtatMensuel ligneSauvegardee = ligneCaptor.getValue();
        assertThat(ligneSauvegardee.getInclusDansEtat()).isFalse();
        assertThat(ligneSauvegardee.getMontantApplique()).isZero();
        assertThat(ligneSauvegardee.getFonctionRetenue()).isEqualTo("JURISTE");
    }

    @Test
    void ajuster_exclureBeneficiaire_metInclusDansEtatAFalse() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2001L)
                .idProcessus(900L)
                .idBeneficiaire(501L)
                .montantApplique(50000)
                .inclusDansEtat(true)
                .fonctionRetenue("DA")
                .build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(501L);
        ajustement.setInclusDansEtat(false);

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 501L)).thenReturn(Optional.of(ligne));
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        assertThat(reponse.getId()).isEqualTo(900L);
        assertThat(reponse.getResultats()).hasSize(1);
        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getIdBeneficiaire()).isEqualTo(501L);
        assertThat(resultat.getApplique()).isTrue();
        assertThat(resultat.getMotifRejet()).isNull();

        ArgumentCaptor<LigneEtatMensuel> ligneCaptor = ArgumentCaptor.forClass(LigneEtatMensuel.class);
        verify(ligneEtatMensuelRepository).save(ligneCaptor.capture());
        assertThat(ligneCaptor.getValue().getInclusDansEtat()).isFalse();

        verify(auditService).enregistrer(eq(10L), eq("AJUSTEMENT_LIGNE_ETAT_MENSUEL"), eq("ligne_etat_mensuel"),
                eq(2001L), anyMap(), anyMap());
    }

    @Test
    void ajuster_reintegrerBeneficiaireExclu_metInclusDansEtatATrue() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2002L)
                .idProcessus(900L)
                .idBeneficiaire(502L)
                .montantApplique(40000)
                .inclusDansEtat(false)
                .fonctionRetenue("CHEF_DEPARTEMENT")
                .build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(502L);
        ajustement.setInclusDansEtat(true);

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        // Une reintegration revalide desormais RG-01/RG-04/RG-02 sur la fonction
        // courante de la ligne : le referentiel, la grille et le grade sont donc
        // interroges, contrairement a la version d'origine de ce test.
        Beneficiaire essama = Beneficiaire.builder().id(502L).matricule("5522")
                .nomPrenoms("ESSAMA Solange").fonction("CHEF_DEPARTEMENT").grade("Grade 3").actif(true).build();
        FonctionEligible chefDepartement = FonctionEligible.builder().id(8L).code("CHEF_DEPARTEMENT")
                .libelle("Chef de Département").actif(true).build();
        GrilleTarifaire grilleChefDept = GrilleTarifaire.builder().id(2L).idFonctionEligible(8L)
                .montantFcfa(40000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 502L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("CHEF_DEPARTEMENT")).thenReturn(Optional.of(chefDepartement));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(8L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleChefDept));
        when(beneficiaireRepository.findById(502L)).thenReturn(Optional.of(essama));
        when(eligibiliteService.verifierEligibilite("CHEF_DEPARTEMENT", "Grade 3")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getApplique()).isTrue();
        assertThat(resultat.getMotifRejet()).isNull();

        ArgumentCaptor<LigneEtatMensuel> ligneCaptor = ArgumentCaptor.forClass(LigneEtatMensuel.class);
        verify(ligneEtatMensuelRepository).save(ligneCaptor.capture());
        assertThat(ligneCaptor.getValue().getInclusDansEtat()).isTrue();
        assertThat(ligneCaptor.getValue().getMontantApplique()).isEqualTo(40000);

        verify(auditService).enregistrer(eq(10L), eq("AJUSTEMENT_LIGNE_ETAT_MENSUEL"), eq("ligne_etat_mensuel"),
                eq(2002L), anyMap(), anyMap());
    }

    @Test
    void ajuster_modifierFonctionRetenue_recalculeLeMontant() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2003L)
                .idProcessus(900L)
                .idBeneficiaire(503L)
                .montantApplique(40000)
                .inclusDansEtat(true)
                .fonctionRetenue("CHEF_DEPARTEMENT")
                .build();

        FonctionEligible directeur = FonctionEligible.builder().id(6L).code("DIRECTEUR")
                .libelle("Directeur Central/Succursale/Régional").actif(true).build();
        GrilleTarifaire grilleDirecteur = GrilleTarifaire.builder().id(3L).idFonctionEligible(6L)
                .montantFcfa(60000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(503L);
        ajustement.setFonctionRetenue("DIRECTEUR");

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 503L)).thenReturn(Optional.of(ligne));
        Beneficiaire tchinda = Beneficiaire.builder().id(503L).matricule("6180")
                .nomPrenoms("TCHINDA Paul").fonction("CHEF_DEPARTEMENT").grade("Grade 5").actif(true).build();

        when(fonctionEligibleRepository.findByCode("DIRECTEUR")).thenReturn(Optional.of(directeur));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(6L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleDirecteur));
        when(beneficiaireRepository.findById(503L)).thenReturn(Optional.of(tchinda));
        when(eligibiliteService.verifierEligibilite("DIRECTEUR", "Grade 5")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getApplique()).isTrue();
        assertThat(resultat.getMotifRejet()).isNull();

        ArgumentCaptor<LigneEtatMensuel> ligneCaptor = ArgumentCaptor.forClass(LigneEtatMensuel.class);
        verify(ligneEtatMensuelRepository).save(ligneCaptor.capture());
        assertThat(ligneCaptor.getValue().getFonctionRetenue()).isEqualTo("DIRECTEUR");
        assertThat(ligneCaptor.getValue().getMontantApplique()).isEqualTo(60000);
    }

    @Test
    void ajuster_processusDejaValide_leve409() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_ATTENTE_CRH).build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(501L);
        ajustement.setInclusDansEtat(false);

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));

        assertThatThrownBy(() -> processusMensuelService.ajuster(900L, requete))
                .isInstanceOf(ProcessusMensuelNonModifiableException.class);

        verify(ligneEtatMensuelRepository, never()).findByIdProcessusAndIdBeneficiaire(any(), any());
    }

    @Test
    void ajuster_ligneInexistantePourCeProcessus_appliqueFalseAvecMotifRejet() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(9999L);
        ajustement.setInclusDansEtat(false);

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 9999L)).thenReturn(Optional.empty());

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getIdBeneficiaire()).isEqualTo(9999L);
        assertThat(resultat.getApplique()).isFalse();
        assertThat(resultat.getMotifRejet()).isEqualTo("Aucune ligne d'état mensuel pour ce bénéficiaire dans ce processus");

        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
    }

    @Test
    void ajuster_auditAvecDeltaAvantEtApres_verifieLesDeuxCotes() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2004L)
                .idProcessus(900L)
                .idBeneficiaire(504L)
                .montantApplique(35000)
                .inclusDansEtat(true)
                .fonctionRetenue("COORDONNATEUR")
                .build();

        FonctionEligible chefDivision = FonctionEligible.builder().id(17L).code("CHEF_DIVISION")
                .libelle("Chef de Division").actif(true).build();
        GrilleTarifaire grilleChefDivision = GrilleTarifaire.builder().id(9L).idFonctionEligible(17L)
                .montantFcfa(35000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(504L);
        ajustement.setFonctionRetenue("CHEF_DIVISION");
        ajustement.setInclusDansEtat(false);

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 504L)).thenReturn(Optional.of(ligne));
        Beneficiaire fouda = Beneficiaire.builder().id(504L).matricule("4390")
                .nomPrenoms("FOUDA Bertrand").fonction("COORDONNATEUR").grade("Grade 4").actif(true).build();

        when(fonctionEligibleRepository.findByCode("CHEF_DIVISION")).thenReturn(Optional.of(chefDivision));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(17L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleChefDivision));
        when(beneficiaireRepository.findById(504L)).thenReturn(Optional.of(fouda));
        when(eligibiliteService.verifierEligibilite("CHEF_DIVISION", "Grade 4")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processusMensuelService.ajuster(900L, requete);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> avantCaptor = ArgumentCaptor.forClass(Map.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> apresCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditService).enregistrer(eq(10L), eq("AJUSTEMENT_LIGNE_ETAT_MENSUEL"), eq("ligne_etat_mensuel"),
                eq(2004L), avantCaptor.capture(), apresCaptor.capture());

        Map<String, Object> avant = avantCaptor.getValue();
        Map<String, Object> apres = apresCaptor.getValue();

        assertThat(avant).containsEntry("fonctionRetenue", "COORDONNATEUR");
        assertThat(avant).containsEntry("montantApplique", 35000);
        assertThat(avant).containsEntry("inclusDansEtat", true);

        assertThat(apres).containsEntry("fonctionRetenue", "CHEF_DIVISION");
        assertThat(apres).containsEntry("montantApplique", 35000);
        assertThat(apres).containsEntry("inclusDansEtat", false);
    }

    @Test
    void ajuster_ajustementSansChangementReel_appliqueTrueSansMotifRejet() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2005L)
                .idProcessus(900L)
                .idBeneficiaire(505L)
                .montantApplique(30000)
                .inclusDansEtat(true)
                .fonctionRetenue("ATTACHE_COMMERCIAL")
                .build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(505L);
        ajustement.setInclusDansEtat(true);
        ajustement.setFonctionRetenue("ATTACHE_COMMERCIAL");

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 505L)).thenReturn(Optional.of(ligne));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getApplique()).isTrue();
        assertThat(resultat.getMotifRejet()).isNull();

        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
    }

    @Test
    void ajuster_fonctionSansGrilleValide_appliqueFalseAvecMotifRejet() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2006L)
                .idProcessus(900L)
                .idBeneficiaire(506L)
                .montantApplique(35000)
                .inclusDansEtat(true)
                .fonctionRetenue("CHEF_PRODUIT")
                .build();

        FonctionEligible agentRecouvrement = FonctionEligible.builder().id(20L).code("JURISTE")
                .libelle("Agent de Recouvrement").actif(true).build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(506L);
        ajustement.setFonctionRetenue("JURISTE");

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 506L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("JURISTE")).thenReturn(Optional.of(agentRecouvrement));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(20L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.empty());

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getIdBeneficiaire()).isEqualTo(506L);
        assertThat(resultat.getApplique()).isFalse();
        assertThat(resultat.getMotifRejet()).isEqualTo("Aucune grille tarifaire ACTIVE pour cette fonction");

        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
        // Retour anticipe sur la grille : le grade n'est jamais interroge.
        verify(beneficiaireRepository, never()).findById(any());
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
    }

    // ------------------------------------------------------------------
    // RG-02 sur le chemin d'ajustement (PATCH /processus/{id}).
    // Faille relevee en recette du Sprint 6F.6 : changer la fonction retenue
    // vers un corps de controle etait accepte sans aucune verification du
    // grade, contrairement a PATCH /beneficiaires/{id}.
    // ------------------------------------------------------------------

    private ProcessusMensuel processusEnCoursArh() {
        return ProcessusMensuel.builder().id(900L).statut(StatutEnum.EN_COURS_ARH).build();
    }

    private PatchProcessusRequestDto requeteAvec(AjustementLigneEtatDto... ajustements) {
        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustements));
        return requete;
    }

    private AjustementLigneEtatDto ajustementFonction(Long idBeneficiaire, String fonctionRetenue) {
        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(idBeneficiaire);
        ajustement.setFonctionRetenue(fonctionRetenue);
        return ajustement;
    }

    private AjustementLigneEtatDto ajustementInclusion(Long idBeneficiaire, boolean inclusDansEtat) {
        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(idBeneficiaire);
        ajustement.setInclusDansEtat(inclusDansEtat);
        return ajustement;
    }

    @Test
    void ajuster_fonctionCorpsControleAvecGradeNonGrade_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2010L).idProcessus(900L).idBeneficiaire(507L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_DIVISION").build();

        Beneficiaire belinga = Beneficiaire.builder().id(507L).matricule("7508")
                .nomPrenoms("BELINGA Christelle").fonction("CHEF_DIVISION").grade("NON GRADE").actif(true).build();

        FonctionEligible inspecteurGeneral = FonctionEligible.builder().id(3L).code("CORPS_CONTROLE_IG")
                .libelle("Inspecteur Général").actif(true).build();
        GrilleTarifaire grilleIg = GrilleTarifaire.builder().id(4L).idFonctionEligible(3L)
                .montantFcfa(70000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 507L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("CORPS_CONTROLE_IG")).thenReturn(Optional.of(inspecteurGeneral));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(3L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleIg));
        when(beneficiaireRepository.findById(507L)).thenReturn(Optional.of(belinga));
        when(eligibiliteService.verifierEligibilite("CORPS_CONTROLE_IG", "NON GRADE")).thenReturn(false);

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(507L, "CORPS_CONTROLE_IG")));

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getApplique()).isFalse();
        assertThat(resultat.getMotifRejet()).isEqualTo("Grade non éligible pour une fonction de corps de contrôle");

        // La ligne ne doit avoir subi aucune mutation, meme partielle.
        assertThat(ligne.getFonctionRetenue()).isEqualTo("CHEF_DIVISION");
        assertThat(ligne.getMontantApplique()).isEqualTo(35000);
        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
    }

    @Test
    void ajuster_fonctionCorpsControleAvecGradeReel_appliqueLajustement() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2011L).idProcessus(900L).idBeneficiaire(508L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();

        Beneficiaire onana = Beneficiaire.builder().id(508L).matricule("9720")
                .nomPrenoms("ONANA Patrice").fonction("DA").grade("Grade 6").actif(true).build();

        FonctionEligible inspecteurGeneralAdjoint = FonctionEligible.builder().id(4L).code("CORPS_CONTROLE_IGA")
                .libelle("Inspecteur Général Adjoint").actif(true).build();
        GrilleTarifaire grilleIga = GrilleTarifaire.builder().id(5L).idFonctionEligible(4L)
                .montantFcfa(65000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 508L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("CORPS_CONTROLE_IGA")).thenReturn(Optional.of(inspecteurGeneralAdjoint));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(4L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleIga));
        when(beneficiaireRepository.findById(508L)).thenReturn(Optional.of(onana));
        when(eligibiliteService.verifierEligibilite("CORPS_CONTROLE_IGA", "Grade 6")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(508L, "CORPS_CONTROLE_IGA")));

        assertThat(reponse.getResultats().get(0).getApplique()).isTrue();
        assertThat(ligne.getFonctionRetenue()).isEqualTo("CORPS_CONTROLE_IGA");
        assertThat(ligne.getMontantApplique()).isEqualTo(65000);
    }

    // Beneficiaire issu de l'import Excel : la colonne GRADE n'existe pas, donc
    // grade null en base. Doit rester fail-closed sur un corps de controle.
    @Test
    void ajuster_fonctionCorpsControleGradeNull_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2012L).idProcessus(900L).idBeneficiaire(509L)
                .montantApplique(30000).inclusDansEtat(true).fonctionRetenue("ATTACHE_COMMERCIAL").build();

        Beneficiaire nkolo = Beneficiaire.builder().id(509L).matricule("3164")
                .nomPrenoms("NKOLO Emmanuel").fonction("ATTACHE_COMMERCIAL").grade(null).actif(true).build();

        FonctionEligible comptable = FonctionEligible.builder().id(23L).code("COMPTABLE")
                .libelle("Comptable").actif(true).build();
        GrilleTarifaire grilleComptable = GrilleTarifaire.builder().id(12L).idFonctionEligible(23L)
                .montantFcfa(35000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 509L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptable));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(23L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleComptable));
        when(beneficiaireRepository.findById(509L)).thenReturn(Optional.of(nkolo));
        when(eligibiliteService.verifierEligibilite("COMPTABLE", null)).thenReturn(false);

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(509L, "COMPTABLE")));

        assertThat(reponse.getResultats().get(0).getApplique()).isFalse();
        assertThat(reponse.getResultats().get(0).getMotifRejet())
                .isEqualTo("Grade non éligible pour une fonction de corps de contrôle");
        verify(ligneEtatMensuelRepository, never()).save(any());
    }

    @Test
    void ajuster_fonctionCorpsControleGradeVide_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2013L).idProcessus(900L).idBeneficiaire(510L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_PRODUIT").build();

        Beneficiaire eyenga = Beneficiaire.builder().id(510L).matricule("4821")
                .nomPrenoms("EYENGA Brigitte").fonction("CHEF_PRODUIT").grade("   ").actif(true).build();

        FonctionEligible controleurGestion = FonctionEligible.builder().id(21L).code("CONTROLEUR_GESTION")
                .libelle("Contrôleur de Gestion").actif(true).build();
        GrilleTarifaire grilleControleur = GrilleTarifaire.builder().id(10L).idFonctionEligible(21L)
                .montantFcfa(35000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 510L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("CONTROLEUR_GESTION")).thenReturn(Optional.of(controleurGestion));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(21L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleControleur));
        when(beneficiaireRepository.findById(510L)).thenReturn(Optional.of(eyenga));
        when(eligibiliteService.verifierEligibilite("CONTROLEUR_GESTION", "   ")).thenReturn(false);

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(510L, "CONTROLEUR_GESTION")));

        assertThat(reponse.getResultats().get(0).getApplique()).isFalse();
        verify(ligneEtatMensuelRepository, never()).save(any());
    }

    // Hors corps de controle, RG-02 ne s'applique pas : un grade absent ne doit
    // pas bloquer l'ajustement.
    @Test
    void ajuster_fonctionHorsCorpsControleGradeNull_appliqueLajustement() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2014L).idProcessus(900L).idBeneficiaire(511L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("GFC").build();

        Beneficiaire tchinda = Beneficiaire.builder().id(511L).matricule("6180")
                .nomPrenoms("TCHINDA Paul").fonction("GFC").grade(null).actif(true).build();

        FonctionEligible directeur = FonctionEligible.builder().id(6L).code("DIRECTEUR")
                .libelle("Directeur Central/Succursale/Régional").actif(true).build();
        GrilleTarifaire grilleDirecteur = GrilleTarifaire.builder().id(3L).idFonctionEligible(6L)
                .montantFcfa(60000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 511L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("DIRECTEUR")).thenReturn(Optional.of(directeur));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(6L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleDirecteur));
        when(beneficiaireRepository.findById(511L)).thenReturn(Optional.of(tchinda));
        when(eligibiliteService.verifierEligibilite("DIRECTEUR", null)).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(511L, "DIRECTEUR")));

        assertThat(reponse.getResultats().get(0).getApplique()).isTrue();
        assertThat(ligne.getMontantApplique()).isEqualTo(60000);
    }

    // Tests d'ordonnancement : RG-01 doit court-circuiter AVANT le controle du
    // grade. C'est ce qui garantit qu'un retour false d'EligibiliteService ne
    // peut venir que du grade, et donc que le motif de rejet est exact.
    @Test
    void ajuster_fonctionInconnue_neVerifiePasLeGrade() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2015L).idProcessus(900L).idBeneficiaire(512L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_DIVISION").build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 512L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("FONCTION_INEXISTANTE")).thenReturn(Optional.empty());

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(512L, "FONCTION_INEXISTANTE")));

        assertThat(reponse.getResultats().get(0).getMotifRejet())
                .isEqualTo("Fonction inconnue du référentiel fonction_eligible");
        verify(beneficiaireRepository, never()).findById(any());
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
    }

    @Test
    void ajuster_fonctionDesactivee_neVerifiePasLeGrade() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2016L).idProcessus(900L).idBeneficiaire(513L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_DIVISION").build();

        FonctionEligible comptableDesactive = FonctionEligible.builder().id(23L).code("COMPTABLE")
                .libelle("Comptable").actif(false).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 513L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptableDesactive));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(513L, "COMPTABLE")));

        assertThat(reponse.getResultats().get(0).getMotifRejet()).isEqualTo("Fonction désactivée");
        verify(beneficiaireRepository, never()).findById(any());
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
    }

    // Cas limite : la ligne existe mais le beneficiaire est introuvable. On
    // reste fail-closed (grade null) sans lever d'exception, sinon les autres
    // lignes valides du meme lot echoueraient.
    @Test
    void ajuster_beneficiaireIntrouvable_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2017L).idProcessus(900L).idBeneficiaire(514L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_DIVISION").build();

        FonctionEligible controleurComptable = FonctionEligible.builder().id(22L).code("CONTROLEUR_COMPTABLE")
                .libelle("Contrôleur Comptable").actif(true).build();
        GrilleTarifaire grilleControleur = GrilleTarifaire.builder().id(11L).idFonctionEligible(22L)
                .montantFcfa(35000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 514L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("CONTROLEUR_COMPTABLE")).thenReturn(Optional.of(controleurComptable));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(22L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleControleur));
        when(beneficiaireRepository.findById(514L)).thenReturn(Optional.empty());
        when(eligibiliteService.verifierEligibilite("CONTROLEUR_COMPTABLE", null)).thenReturn(false);

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(514L, "CONTROLEUR_COMPTABLE")));

        assertThat(reponse.getResultats().get(0).getApplique()).isFalse();
        verify(ligneEtatMensuelRepository, never()).save(any());
    }

    // Atomicite par ligne : un ajustement portant a la fois un changement de
    // fonction refuse et une reintegration ne doit rien appliquer du tout.
    @Test
    void ajuster_gradeNonEligible_nappliquePasLaReintegrationDeLaMemeLigne() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2018L).idProcessus(900L).idBeneficiaire(515L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("CHEF_DIVISION").build();

        Beneficiaire mbarga = Beneficiaire.builder().id(515L).matricule("1847")
                .nomPrenoms("MBARGA Jean Paul").fonction("CHEF_DIVISION").grade("NON GRADE").actif(true).build();

        FonctionEligible comptable = FonctionEligible.builder().id(23L).code("COMPTABLE")
                .libelle("Comptable").actif(true).build();
        GrilleTarifaire grilleComptable = GrilleTarifaire.builder().id(12L).idFonctionEligible(23L)
                .montantFcfa(35000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(515L);
        ajustement.setFonctionRetenue("COMPTABLE");
        ajustement.setInclusDansEtat(true);

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 515L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptable));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(23L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleComptable));
        when(beneficiaireRepository.findById(515L)).thenReturn(Optional.of(mbarga));
        when(eligibiliteService.verifierEligibilite("COMPTABLE", "NON GRADE")).thenReturn(false);

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requeteAvec(ajustement));

        assertThat(reponse.getResultats().get(0).getApplique()).isFalse();
        assertThat(ligne.getInclusDansEtat()).isFalse();
        assertThat(ligne.getFonctionRetenue()).isEqualTo("CHEF_DIVISION");
        verify(ligneEtatMensuelRepository, never()).save(any());
    }

    // Rapport partiel : une ligne rejetee sur le grade ne doit pas empecher les
    // autres lignes du meme lot d'etre appliquees.
    @Test
    void ajuster_uneLigneRejeteeNempechePasLesAutres_rapportPartiel() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligneEssama = LigneEtatMensuel.builder()
                .id(2019L).idProcessus(900L).idBeneficiaire(516L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("CHEF_DEPARTEMENT").build();
        LigneEtatMensuel ligneBelinga = LigneEtatMensuel.builder()
                .id(2020L).idProcessus(900L).idBeneficiaire(517L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_DIVISION").build();

        Beneficiaire essama = Beneficiaire.builder().id(516L).matricule("5522")
                .nomPrenoms("ESSAMA Solange").fonction("CHEF_DEPARTEMENT").grade("Grade 3").actif(true).build();
        Beneficiaire belinga = Beneficiaire.builder().id(517L).matricule("7508")
                .nomPrenoms("BELINGA Christelle").fonction("CHEF_DIVISION").grade("NON GRADE").actif(true).build();

        FonctionEligible directeur = FonctionEligible.builder().id(6L).code("DIRECTEUR")
                .libelle("Directeur Central/Succursale/Régional").actif(true).build();
        GrilleTarifaire grilleDirecteur = GrilleTarifaire.builder().id(3L).idFonctionEligible(6L)
                .montantFcfa(60000).statutValidation(StatutGrilleEnum.ACTIVE).build();
        FonctionEligible inspecteurGeneral = FonctionEligible.builder().id(3L).code("CORPS_CONTROLE_IG")
                .libelle("Inspecteur Général").actif(true).build();
        GrilleTarifaire grilleIg = GrilleTarifaire.builder().id(4L).idFonctionEligible(3L)
                .montantFcfa(70000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 516L)).thenReturn(Optional.of(ligneEssama));
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 517L)).thenReturn(Optional.of(ligneBelinga));
        when(fonctionEligibleRepository.findByCode("DIRECTEUR")).thenReturn(Optional.of(directeur));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(6L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleDirecteur));
        when(fonctionEligibleRepository.findByCode("CORPS_CONTROLE_IG")).thenReturn(Optional.of(inspecteurGeneral));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(3L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleIg));
        when(beneficiaireRepository.findById(516L)).thenReturn(Optional.of(essama));
        when(beneficiaireRepository.findById(517L)).thenReturn(Optional.of(belinga));
        when(eligibiliteService.verifierEligibilite("DIRECTEUR", "Grade 3")).thenReturn(true);
        when(eligibiliteService.verifierEligibilite("CORPS_CONTROLE_IG", "NON GRADE")).thenReturn(false);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requeteAvec(
                ajustementFonction(516L, "DIRECTEUR"),
                ajustementFonction(517L, "CORPS_CONTROLE_IG")));

        assertThat(reponse.getResultats()).hasSize(2);
        assertThat(reponse.getResultats()).extracting(
                ResultatAjustementDto::getIdBeneficiaire, ResultatAjustementDto::getApplique
        ).containsExactly(tuple(516L, true), tuple(517L, false));

        assertThat(ligneEssama.getMontantApplique()).isEqualTo(60000);
        assertThat(ligneBelinga.getFonctionRetenue()).isEqualTo("CHEF_DIVISION");
        verify(ligneEtatMensuelRepository, times(1)).save(any(LigneEtatMensuel.class));
    }

    // ------------------------------------------------------------------
    // Revalidation a la reintegration d'une ligne exclue. Avant correction,
    // cocher la case reintegrait un beneficiaire sans aucun controle.
    // ------------------------------------------------------------------

    @Test
    void ajuster_reintegrerLigneExclueFonctionDesactivee_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2021L).idProcessus(900L).idBeneficiaire(518L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("CHEF_PRODUIT").build();

        FonctionEligible chefProduitDesactive = FonctionEligible.builder().id(18L).code("CHEF_PRODUIT")
                .libelle("Chef de Produit").actif(false).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 518L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("CHEF_PRODUIT")).thenReturn(Optional.of(chefProduitDesactive));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementInclusion(518L, true)));

        assertThat(reponse.getResultats().get(0).getApplique()).isFalse();
        assertThat(reponse.getResultats().get(0).getMotifRejet()).isEqualTo("Fonction désactivée");
        assertThat(ligne.getInclusDansEtat()).isFalse();
        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
    }

    @Test
    void ajuster_reintegrerLigneExclueSansGrilleActive_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2022L).idProcessus(900L).idBeneficiaire(519L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("JURISTE").build();

        FonctionEligible agentRecouvrement = FonctionEligible.builder().id(20L).code("JURISTE")
                .libelle("Agent de Recouvrement").actif(true).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 519L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("JURISTE")).thenReturn(Optional.of(agentRecouvrement));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(20L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.empty());

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementInclusion(519L, true)));

        assertThat(reponse.getResultats().get(0).getMotifRejet())
                .isEqualTo("Aucune grille tarifaire ACTIVE pour cette fonction");
        assertThat(ligne.getInclusDansEtat()).isFalse();
        verify(ligneEtatMensuelRepository, never()).save(any());
    }

    @Test
    void ajuster_reintegrerLigneExclueCorpsControleNonGrade_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2023L).idProcessus(900L).idBeneficiaire(520L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("COMPTABLE").build();

        Beneficiaire ndongo = Beneficiaire.builder().id(520L).matricule("7508")
                .nomPrenoms("NDONGO Béatrice").fonction("COMPTABLE").grade("NON GRADE").actif(true).build();

        FonctionEligible comptable = FonctionEligible.builder().id(23L).code("COMPTABLE")
                .libelle("Comptable").actif(true).build();
        GrilleTarifaire grilleComptable = GrilleTarifaire.builder().id(12L).idFonctionEligible(23L)
                .montantFcfa(35000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 520L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("COMPTABLE")).thenReturn(Optional.of(comptable));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(23L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleComptable));
        when(beneficiaireRepository.findById(520L)).thenReturn(Optional.of(ndongo));
        when(eligibiliteService.verifierEligibilite("COMPTABLE", "NON GRADE")).thenReturn(false);

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementInclusion(520L, true)));

        assertThat(reponse.getResultats().get(0).getMotifRejet())
                .isEqualTo("Grade non éligible pour une fonction de corps de contrôle");
        assertThat(ligne.getInclusDansEtat()).isFalse();
        verify(ligneEtatMensuelRepository, never()).save(any());
    }

    // Miroir du trou : une ligne exclue au declenchement porte un montant 0.
    // La reintegrer doit resynchroniser le montant, sinon le beneficiaire entre
    // dans l'etat mensuel a 0 FCFA (sous-paiement silencieux).
    @Test
    void ajuster_reintegrerLigneRedevenueEligible_resynchroniseLeMontant() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2024L).idProcessus(900L).idBeneficiaire(521L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("GFC").build();

        Beneficiaire atangana = Beneficiaire.builder().id(521L).matricule("4275")
                .nomPrenoms("ATANGANA Sylvie").fonction("GFC").grade("Grade 2").actif(true).build();

        FonctionEligible gfc = FonctionEligible.builder().id(13L).code("GFC")
                .libelle("Gestionnaire de Fonds de Commerce").actif(true).build();
        GrilleTarifaire grilleGfc = GrilleTarifaire.builder().id(7L).idFonctionEligible(13L)
                .montantFcfa(40000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 521L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("GFC")).thenReturn(Optional.of(gfc));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(13L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleGfc));
        when(beneficiaireRepository.findById(521L)).thenReturn(Optional.of(atangana));
        when(eligibiliteService.verifierEligibilite("GFC", "Grade 2")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processusMensuelService.ajuster(900L, requeteAvec(ajustementInclusion(521L, true)));

        assertThat(ligne.getInclusDansEtat()).isTrue();
        assertThat(ligne.getMontantApplique()).isEqualTo(40000);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> avantCaptor = ArgumentCaptor.forClass(Map.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> apresCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditService).enregistrer(eq(10L), eq("AJUSTEMENT_LIGNE_ETAT_MENSUEL"), eq("ligne_etat_mensuel"),
                eq(2024L), avantCaptor.capture(), apresCaptor.capture());
        assertThat(avantCaptor.getValue()).containsEntry("montantApplique", 0);
        assertThat(apresCaptor.getValue()).containsEntry("montantApplique", 40000);
    }

    // Non-regression du cas legitime : une ligne exclue par l'ARH (et non au
    // declenchement) conserve son montant ; la reintegration reste possible.
    @Test
    void ajuster_reintegrerLigneExclueParLarh_conserveLeMontant() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2025L).idProcessus(900L).idBeneficiaire(522L)
                .montantApplique(50000).inclusDansEtat(false).fonctionRetenue("DA").build();

        Beneficiaire owono = Beneficiaire.builder().id(522L).matricule("6100")
                .nomPrenoms("OWONO Serge").fonction("DA").grade("Grade 5").actif(true).build();

        FonctionEligible directeurAgence = FonctionEligible.builder().id(5L).code("DA")
                .libelle("Directeur d'Agence").actif(true).build();
        GrilleTarifaire grilleDa = GrilleTarifaire.builder().id(1L).idFonctionEligible(5L)
                .montantFcfa(50000).statutValidation(StatutGrilleEnum.ACTIVE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 522L)).thenReturn(Optional.of(ligne));
        when(fonctionEligibleRepository.findByCode("DA")).thenReturn(Optional.of(directeurAgence));
        when(grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(5L, StatutGrilleEnum.ACTIVE))
                .thenReturn(Optional.of(grilleDa));
        when(beneficiaireRepository.findById(522L)).thenReturn(Optional.of(owono));
        when(eligibiliteService.verifierEligibilite("DA", "Grade 5")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementInclusion(522L, true)));

        assertThat(reponse.getResultats().get(0).getApplique()).isTrue();
        assertThat(ligne.getInclusDansEtat()).isTrue();
        assertThat(ligne.getMontantApplique()).isEqualTo(50000);
    }

    // Exclure reste une operation sure : aucune revalidation ne doit avoir lieu.
    @Test
    void ajuster_exclureBeneficiaire_neDeclencheAucuneRevalidation() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2026L).idProcessus(900L).idBeneficiaire(523L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 523L)).thenReturn(Optional.of(ligne));
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementInclusion(523L, false)));

        assertThat(reponse.getResultats().get(0).getApplique()).isTrue();
        assertThat(ligne.getInclusDansEtat()).isFalse();
        verify(fonctionEligibleRepository, never()).findByCode(any());
        verify(beneficiaireRepository, never()).findById(any());
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
    }

    @Test
    void valider_casNominal_genereLePdfEtChangeLeStatut() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").nom("MBARGA").prenom("Jean-Paul").build();

        LigneEtatMensuel ligneIncluse = LigneEtatMensuel.builder()
                .id(2001L).idProcessus(900L).idBeneficiaire(501L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();

        PieceJointe pieceJointeGeneree = PieceJointe.builder().id(700L).idProcessus(900L)
                .nomFichier("dotations-telephoniques-7-2026.pdf").nombreSignatures(1).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(900L)).thenReturn(List.of(ligneIncluse));
        when(documentService.genererInitiale(processus, List.of(ligneIncluse), arhConnecte)).thenReturn(pieceJointeGeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 22/07/2026 10:00:00");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)).thenReturn(List.of());

        ValiderProcessusResponseDto reponse = processusMensuelService.valider(900L, "État vérifié et conforme");

        assertThat(reponse.getId()).isEqualTo(900L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponse.getEtapeValidee()).isEqualTo("VALIDATION_ARH");
        assertThat(reponse.getIdPieceJointe()).isEqualTo(700L);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdProcessus()).isEqualTo(900L);
        assertThat(etape.getIdActeur()).isEqualTo(10L);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_ARH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.VALIDEE);
        assertThat(etape.getSignatureNumerique()).isNotBlank();

        verify(auditService).enregistrer(eq(10L), eq("VALIDATION_PROCESSUS_ARH"), eq("processus_mensuel"),
                eq(900L), anyMap(), anyMap());
    }

    @Test
    void valider_processusDejaValide_leve409() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.CLOTURE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));

        assertThatThrownBy(() -> processusMensuelService.valider(900L, "commentaire"))
                .isInstanceOf(ProcessusMensuelNonModifiableException.class);

        verify(documentService, never()).genererInitiale(any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(ligneEtatMensuelRepository, never()).findByIdProcessusAndInclusDansEtatTrue(any());
    }

    @Test
    void valider_processusIntrouvable_leve404() {
        when(processusMensuelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processusMensuelService.valider(999L, null))
                .isInstanceOf(ProcessusMensuelIntrouvableException.class);

        verify(documentService, never()).genererInitiale(any(), any(), any());
    }

    @Test
    void valider_appelleNotificationService_uneFoisParDestinataireCRH() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(910L).moisPaiement(8).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(11L).matricule("2202").nom("ESSAMA").prenom("Marie-Claire").build();
        Utilisateur crh1 = Utilisateur.builder().id(20L).matricule("3001").nom("ATANGANA").prenom("Paul")
                .role(RoleEnum.CRH).actif(true).build();
        Utilisateur crh2 = Utilisateur.builder().id(21L).matricule("3002").nom("NKOLO").prenom("Sylvie")
                .role(RoleEnum.CRH).actif(true).build();

        PieceJointe pieceJointeGeneree = PieceJointe.builder().id(701L).idProcessus(910L).build();

        when(processusMensuelRepository.findById(910L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(910L)).thenReturn(List.of());
        when(documentService.genererInitiale(eq(processus), any(), eq(arhConnecte))).thenReturn(pieceJointeGeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("signature");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)).thenReturn(List.of(crh1, crh2));

        processusMensuelService.valider(910L, null);

        verify(notificationService).notifier(eq(crh1), any(), any());
        verify(notificationService).notifier(eq(crh2), any(), any());
        verify(notificationService, times(2)).notifier(any(), any(), any());
    }

    @Test
    void valider_pieceJointeUniquePourLeProcessus_verifieContrainte() {
        // RG-06 : piece_jointe.id_processus est UNIQUE en base. Le controle de
        // statut doit empecher toute nouvelle generation en amont, sans jamais
        // laisser la contrainte SQL echouer en 500 sur une seconde tentative de
        // validation (ici sur un processus deja cloture, donc plus modifiable).
        ProcessusMensuel processusDejaValide = ProcessusMensuel.builder().id(920L).statut(StatutEnum.CLOTURE).build();

        when(processusMensuelRepository.findById(920L)).thenReturn(Optional.of(processusDejaValide));

        assertThatThrownBy(() -> processusMensuelService.valider(920L, "seconde tentative"))
                .isInstanceOf(ProcessusMensuelNonModifiableException.class);

        verify(documentService, never()).genererInitiale(any(), any(), any());
        verify(ligneEtatMensuelRepository, never()).findByIdProcessusAndInclusDansEtatTrue(any());
    }

    @Test
    void valider_brancheCRH_casNominal_ajouteSignatureEtChangeStatut() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(930L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).build();
        Utilisateur crhConnecte = Utilisateur.builder().id(21L).matricule("3002").nom("NKOLO").prenom("Sylvie")
                .role(RoleEnum.CRH).actif(true).build();

        PieceJointe pieceJointeExistante = PieceJointe.builder().id(705L).idProcessus(930L)
                .nomFichier("dotations-telephoniques-7-2026.pdf").nombreSignatures(1).build();

        when(processusMensuelRepository.findById(930L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte);
        when(pieceJointeRepository.findByIdProcessus(930L)).thenReturn(Optional.of(pieceJointeExistante));
        when(documentService.ajouterSignature(pieceJointeExistante, crhConnecte, NomEtapeEnum.VALIDATION_CRH))
                .thenReturn(pieceJointeExistante);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.DRH)).thenReturn(List.of());

        ValiderProcessusResponseDto reponse = processusMensuelService.valider(930L, "Etat verifie niveau CRH");

        assertThat(reponse.getId()).isEqualTo(930L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_DRH);
        assertThat(reponse.getEtapeValidee()).isEqualTo("VALIDATION_CRH");
        assertThat(reponse.getIdPieceJointe()).isEqualTo(705L);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_DRH);

        verify(separationTachesService).verifier(930L, 21L, NomEtapeEnum.VALIDATION_ARH);
        verify(documentService).ajouterSignature(pieceJointeExistante, crhConnecte, NomEtapeEnum.VALIDATION_CRH);

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdProcessus()).isEqualTo(930L);
        assertThat(etape.getIdActeur()).isEqualTo(21L);
        assertThat(etape.getOrdreEtape()).isEqualTo(2);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_CRH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.VALIDEE);

        verify(auditService).enregistrer(eq(21L), eq("VALIDATION_PROCESSUS_CRH"), eq("processus_mensuel"),
                eq(930L), anyMap(), anyMap());
    }

    // RG-05, cas exact releve en recette du Sprint 6F.6 : un processus RETOURNE
    // declenche la branche ARH sur le seul critere du statut. Avant correction,
    // la DRH ATANGANA validait ainsi l'etape ARH a la place de l'ARH (200 OK,
    // statut passe a EN_ATTENTE_CRH, PDF genere). Attendu desormais : 403 et
    // aucun effet de bord.
    @Test
    void valider_processusRetourne_acteurDrhSurBrancheArh_leve403() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(940L).moisPaiement(9).anneePaiement(2027)
                .statut(StatutEnum.RETOURNE).build();
        Utilisateur atanganaDrh = Utilisateur.builder().id(40L).matricule("1562").nom("ATANGANA").prenom("Paul")
                .role(RoleEnum.DRH).actif(true).build();

        when(processusMensuelRepository.findById(940L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(atanganaDrh);
        org.mockito.Mockito.doThrow(new RoleEtapeNonAutoriseException(
                        "L'étape VALIDATION_ARH doit être validée par un utilisateur de rôle ARH"))
                .when(separationTachesService).verifierRoleAttendu(NomEtapeEnum.VALIDATION_ARH, atanganaDrh);

        assertThatThrownBy(() -> processusMensuelService.valider(940L, "tentative DRH sur etape ARH"))
                .isInstanceOf(RoleEtapeNonAutoriseException.class);

        // La branche ARH ne doit avoir produit aucun effet : ni PDF, ni etape,
        // ni changement de statut, ni notification, ni trace d'audit.
        verify(documentService, never()).genererInitiale(any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
        verify(notificationService, never()).notifier(any(), any(), any());
        verify(auditService, never()).enregistrer(any(), any(), any(), any(), any(), any());
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);
    }

    // Pendant RG-05 pour la branche CRH : un ARH ne doit pas pouvoir valider
    // l'etape CRH d'un processus EN_ATTENTE_CRH.
    @Test
    void valider_processusEnAttenteCrh_acteurArh_leve403() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(941L).moisPaiement(9).anneePaiement(2027)
                .statut(StatutEnum.EN_ATTENTE_CRH).build();
        Utilisateur mbargaArh = Utilisateur.builder().id(41L).matricule("1847").nom("MBARGA").prenom("Jean Paul")
                .role(RoleEnum.ARH).actif(true).build();

        when(processusMensuelRepository.findById(941L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(mbargaArh);
        org.mockito.Mockito.doThrow(new RoleEtapeNonAutoriseException(
                        "L'étape VALIDATION_CRH doit être validée par un utilisateur de rôle CRH"))
                .when(separationTachesService).verifierRoleAttendu(NomEtapeEnum.VALIDATION_CRH, mbargaArh);

        assertThatThrownBy(() -> processusMensuelService.valider(941L, null))
                .isInstanceOf(RoleEtapeNonAutoriseException.class);

        // Le controle RG-05 precede RG-08 : la separation des taches ne doit
        // meme pas etre interrogee pour un role incompatible.
        verify(separationTachesService, never()).verifier(any(), any(), any());
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
    }

    @Test
    void valider_brancheCRH_memeActeurQueArh_leve403() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(931L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).build();
        Utilisateur acteurUnique = Utilisateur.builder().id(21L).matricule("3002").nom("NKOLO").prenom("Sylvie")
                .role(RoleEnum.CRH).actif(true).build();

        when(processusMensuelRepository.findById(931L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(acteurUnique);
        org.mockito.Mockito.doThrow(new SeparationTachesViolationException(
                        "L'acteur de l'étape VALIDATION_ARH ne peut pas valider l'étape suivante du même processus"))
                .when(separationTachesService).verifier(931L, 21L, NomEtapeEnum.VALIDATION_ARH);

        assertThatThrownBy(() -> processusMensuelService.valider(931L, "tentative meme acteur"))
                .isInstanceOf(SeparationTachesViolationException.class);

        verify(pieceJointeRepository, never()).findByIdProcessus(any());
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
    }

    @Test
    void valider_brancheCRH_appelleNotificationDRH() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(932L).moisPaiement(8).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).build();
        Utilisateur crhConnecte = Utilisateur.builder().id(22L).matricule("3003").nom("FOUDA").prenom("Bertrand")
                .role(RoleEnum.CRH).actif(true).build();
        Utilisateur drh1 = Utilisateur.builder().id(30L).matricule("4001").nom("BELINGA").prenom("Alice")
                .role(RoleEnum.DRH).actif(true).build();
        Utilisateur drh2 = Utilisateur.builder().id(31L).matricule("4002").nom("ONANA").prenom("Serge")
                .role(RoleEnum.DRH).actif(true).build();

        PieceJointe pieceJointeExistante = PieceJointe.builder().id(706L).idProcessus(932L).nombreSignatures(1).build();

        when(processusMensuelRepository.findById(932L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte);
        when(pieceJointeRepository.findByIdProcessus(932L)).thenReturn(Optional.of(pieceJointeExistante));
        when(documentService.ajouterSignature(pieceJointeExistante, crhConnecte, NomEtapeEnum.VALIDATION_CRH))
                .thenReturn(pieceJointeExistante);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.DRH)).thenReturn(List.of(drh1, drh2));

        processusMensuelService.valider(932L, null);

        verify(notificationService).notifier(eq(drh1), any(), any());
        verify(notificationService).notifier(eq(drh2), any(), any());
        verify(notificationService, times(2)).notifier(any(), any(), any());
    }

    @Test
    void valider_brancheARH_toujoursFonctionnelle() {
        // Non-regression : la restructuration de valider() en deux branches
        // (Sprint 5.2) ne doit rien changer au comportement ARH existant
        // (Sprint 3.4), et ne doit jamais invoquer la logique propre a la CRH.
        ProcessusMensuel processus = ProcessusMensuel.builder().id(940L).moisPaiement(9).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(12L).matricule("2203").nom("EYENGA").prenom("Christian")
                .role(RoleEnum.ARH).actif(true).build();

        LigneEtatMensuel ligneIncluse = LigneEtatMensuel.builder()
                .id(2010L).idProcessus(940L).idBeneficiaire(510L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("CHEF_ANTENNE").build();

        PieceJointe pieceJointeGeneree = PieceJointe.builder().id(710L).idProcessus(940L)
                .nomFichier("dotations-telephoniques-9-2026.pdf").nombreSignatures(1).build();

        when(processusMensuelRepository.findById(940L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(940L)).thenReturn(List.of(ligneIncluse));
        when(documentService.genererInitiale(processus, List.of(ligneIncluse), arhConnecte)).thenReturn(pieceJointeGeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("Christian EYENGA (matricule 2203) - 24/07/2026 09:00:00");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)).thenReturn(List.of());

        ValiderProcessusResponseDto reponse = processusMensuelService.valider(940L, "État vérifié niveau ARH");

        assertThat(reponse.getId()).isEqualTo(940L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponse.getEtapeValidee()).isEqualTo("VALIDATION_ARH");
        assertThat(reponse.getIdPieceJointe()).isEqualTo(710L);

        verify(documentService).genererInitiale(processus, List.of(ligneIncluse), arhConnecte);
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        verify(pieceJointeRepository, never()).findByIdProcessus(any());
        verify(separationTachesService, never()).verifier(any(), any(), any());

        verify(auditService).enregistrer(eq(12L), eq("VALIDATION_PROCESSUS_ARH"), eq("processus_mensuel"),
                eq(940L), anyMap(), anyMap());
    }

    @Test
    void valider_brancheDRH_casNominal_clotureEtPublieEvenement() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(950L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(40L).matricule("4001").nom("BELINGA").prenom("Alice")
                .role(RoleEnum.DRH).actif(true).build();

        PieceJointe pieceJointeExistante = PieceJointe.builder().id(707L).idProcessus(950L)
                .nomFichier("dotations-telephoniques-7-2026.pdf").nombreSignatures(2).build();

        when(processusMensuelRepository.findById(950L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(pieceJointeRepository.findByIdProcessus(950L)).thenReturn(Optional.of(pieceJointeExistante));
        when(documentService.ajouterSignature(pieceJointeExistante, drhConnecte, NomEtapeEnum.VALIDATION_DRH))
                .thenReturn(pieceJointeExistante);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(950L)).thenReturn(50000L);

        ValiderProcessusResponseDto reponse = processusMensuelService.valider(950L, "Valide, autorise au paiement");

        assertThat(reponse.getId()).isEqualTo(950L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.CLOTURE);
        assertThat(reponse.getEtapeValidee()).isEqualTo("VALIDATION_DRH");
        assertThat(reponse.getIdPieceJointe()).isEqualTo(707L);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.CLOTURE);
        assertThat(processus.getDateCloture()).isNotNull();

        verify(separationTachesService).verifier(950L, 40L, NomEtapeEnum.VALIDATION_CRH);
        verify(documentService).ajouterSignature(pieceJointeExistante, drhConnecte, NomEtapeEnum.VALIDATION_DRH);
        verify(evenementClotureService).publier(processus, 50000L);

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdProcessus()).isEqualTo(950L);
        assertThat(etape.getIdActeur()).isEqualTo(40L);
        assertThat(etape.getOrdreEtape()).isEqualTo(3);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_DRH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.VALIDEE);

        verify(auditService).enregistrer(eq(40L), eq("VALIDATION_PROCESSUS_DRH"), eq("processus_mensuel"),
                eq(950L), anyMap(), anyMap());
        verify(auditService).enregistrer(eq(40L), eq("CLOTURE_PROCESSUS"), eq("processus_mensuel"),
                eq(950L), org.mockito.ArgumentMatchers.isNull(), anyMap());
    }

    @Test
    void valider_brancheDRH_memeActeurQueCrh_leve403() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(951L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).build();
        Utilisateur acteurUnique = Utilisateur.builder().id(41L).matricule("4002").nom("ONANA").prenom("Serge")
                .role(RoleEnum.DRH).actif(true).build();

        when(processusMensuelRepository.findById(951L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(acteurUnique);
        org.mockito.Mockito.doThrow(new SeparationTachesViolationException(
                        "L'acteur de l'étape VALIDATION_CRH ne peut pas valider l'étape suivante du même processus"))
                .when(separationTachesService).verifier(951L, 41L, NomEtapeEnum.VALIDATION_CRH);

        assertThatThrownBy(() -> processusMensuelService.valider(951L, "tentative meme acteur"))
                .isInstanceOf(SeparationTachesViolationException.class);

        verify(pieceJointeRepository, never()).findByIdProcessus(any());
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
        verify(evenementClotureService, never()).publier(any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void valider_brancheDRH_troisSignatures_verifieNombreSignatures() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(952L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(42L).matricule("4003").nom("MBARGA").prenom("Estelle")
                .role(RoleEnum.DRH).actif(true).build();

        PieceJointe pieceJointeExistante = PieceJointe.builder().id(708L).idProcessus(952L)
                .nomFichier("dotations-telephoniques-7-2026.pdf").nombreSignatures(2).build();

        when(processusMensuelRepository.findById(952L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(pieceJointeRepository.findByIdProcessus(952L)).thenReturn(Optional.of(pieceJointeExistante));
        // Simule le comportement reel de DocumentService.ajouterSignature, qui
        // incremente nombre_signatures sur l'entite avant de la persister.
        when(documentService.ajouterSignature(pieceJointeExistante, drhConnecte, NomEtapeEnum.VALIDATION_DRH))
                .thenAnswer(invocation -> {
                    PieceJointe pieceJointe = invocation.getArgument(0);
                    pieceJointe.setNombreSignatures(pieceJointe.getNombreSignatures() + 1);
                    return pieceJointe;
                });
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(952L)).thenReturn(40000L);

        processusMensuelService.valider(952L, "Valide, autorise au paiement");

        assertThat(pieceJointeExistante.getNombreSignatures()).isEqualTo(3);
    }

    @Test
    void valider_brancheDRH_montantTotalCorrect() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(953L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(43L).matricule("4004").nom("NDONGO").prenom("Blaise")
                .role(RoleEnum.DRH).actif(true).build();

        PieceJointe pieceJointeExistante = PieceJointe.builder().id(709L).idProcessus(953L).nombreSignatures(2).build();

        when(processusMensuelRepository.findById(953L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(pieceJointeRepository.findByIdProcessus(953L)).thenReturn(Optional.of(pieceJointeExistante));
        when(documentService.ajouterSignature(pieceJointeExistante, drhConnecte, NomEtapeEnum.VALIDATION_DRH))
                .thenReturn(pieceJointeExistante);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(953L)).thenReturn(125000L);

        processusMensuelService.valider(953L, null);

        ArgumentCaptor<Long> montantCaptor = ArgumentCaptor.forClass(Long.class);
        verify(evenementClotureService).publier(eq(processus), montantCaptor.capture());
        assertThat(montantCaptor.getValue()).isEqualTo(125000L);
    }

    @Test
    void valider_branchesArhEtCrh_toujoursFonctionnelles() {
        // Non-regression : le cycle complet ARH -> CRH -> DRH fonctionne de
        // bout en bout apres l'ajout de la branche DRH (Sprint 5.3), avec
        // trois acteurs distincts (RG-08 verifiee a chaque transition).
        ProcessusMensuel processus = ProcessusMensuel.builder().id(960L).moisPaiement(10).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(50L).matricule("2210").nom("ATANGANA").prenom("Paul")
                .role(RoleEnum.ARH).actif(true).build();
        Utilisateur crhConnecte = Utilisateur.builder().id(51L).matricule("3010").nom("EYENGA").prenom("Christian")
                .role(RoleEnum.CRH).actif(true).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(52L).matricule("4010").nom("FOUDA").prenom("Bertrand")
                .role(RoleEnum.DRH).actif(true).build();

        LigneEtatMensuel ligneIncluse = LigneEtatMensuel.builder()
                .id(2020L).idProcessus(960L).idBeneficiaire(520L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();

        PieceJointe pieceJointe = PieceJointe.builder().id(720L).idProcessus(960L)
                .nomFichier("dotations-telephoniques-10-2026.pdf").nombreSignatures(1).build();

        when(processusMensuelRepository.findById(960L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte, crhConnecte, drhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(960L)).thenReturn(List.of(ligneIncluse));
        when(documentService.genererInitiale(processus, List.of(ligneIncluse), arhConnecte)).thenReturn(pieceJointe);
        when(signatureService.signer(arhConnecte)).thenReturn("Paul ATANGANA (matricule 2210) - 24/07/2026 08:00:00");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)).thenReturn(List.of());
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.DRH)).thenReturn(List.of());
        when(pieceJointeRepository.findByIdProcessus(960L)).thenReturn(Optional.of(pieceJointe));
        when(documentService.ajouterSignature(eq(pieceJointe), any(Utilisateur.class), any(NomEtapeEnum.class)))
                .thenReturn(pieceJointe);
        when(ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(960L)).thenReturn(50000L);

        ValiderProcessusResponseDto reponseArh = processusMensuelService.valider(960L, "Etat verifie niveau ARH");
        assertThat(reponseArh.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponseArh.getEtapeValidee()).isEqualTo("VALIDATION_ARH");

        ValiderProcessusResponseDto reponseCrh = processusMensuelService.valider(960L, "Etat verifie niveau CRH");
        assertThat(reponseCrh.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_DRH);
        assertThat(reponseCrh.getEtapeValidee()).isEqualTo("VALIDATION_CRH");

        ValiderProcessusResponseDto reponseDrh = processusMensuelService.valider(960L, "Valide, autorise au paiement");
        assertThat(reponseDrh.getStatut()).isEqualTo(StatutEnum.CLOTURE);
        assertThat(reponseDrh.getEtapeValidee()).isEqualTo("VALIDATION_DRH");

        assertThat(processus.getStatut()).isEqualTo(StatutEnum.CLOTURE);
        assertThat(processus.getDateCloture()).isNotNull();

        verify(separationTachesService).verifier(960L, 51L, NomEtapeEnum.VALIDATION_ARH);
        verify(separationTachesService).verifier(960L, 52L, NomEtapeEnum.VALIDATION_CRH);
        verify(evenementClotureService).publier(processus, 50000L);
    }

    @Test
    void retourner_depuisEnAttenteCrh_casNominal_passeAuStatutRetourne() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(970L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();
        Utilisateur crhConnecte = Utilisateur.builder().id(21L).matricule("3002").nom("NKOLO").prenom("Sylvie")
                .role(RoleEnum.CRH).actif(true).build();
        Utilisateur arhCreateur = Utilisateur.builder().id(10L).matricule("2201").nom("MBARGA").prenom("Jean-Paul")
                .role(RoleEnum.ARH).actif(true).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Montant incorrect pour le matricule 1562");

        when(processusMensuelRepository.findById(970L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(arhCreateur));

        RetournerProcessusResponseDto reponse = processusMensuelService.retourner(970L, requete);

        assertThat(reponse.getId()).isEqualTo(970L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.RETOURNE);
        assertThat(reponse.getMotif()).isEqualTo("Montant incorrect pour le matricule 1562");
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdProcessus()).isEqualTo(970L);
        assertThat(etape.getIdActeur()).isEqualTo(21L);
        assertThat(etape.getOrdreEtape()).isEqualTo(2);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_CRH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.RETOURNEE);
        assertThat(etape.getMotifRetour()).isEqualTo("Montant incorrect pour le matricule 1562");

        verify(auditService).enregistrer(eq(21L), eq("RETOUR_PROCESSUS"), eq("processus_mensuel"),
                eq(970L), anyMap(), anyMap());
    }

    @Test
    void retourner_depuisEnAttenteDrh_casNominal_passeAuStatutRetourne() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(971L).moisPaiement(8).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).idCreateur(11L).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(40L).matricule("4001").nom("BELINGA").prenom("Alice")
                .role(RoleEnum.DRH).actif(true).build();
        Utilisateur arhCreateur = Utilisateur.builder().id(11L).matricule("2202").nom("ESSAMA").prenom("Marie-Claire")
                .role(RoleEnum.ARH).actif(true).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Beneficiaire ONANA Serge exclu a tort");

        when(processusMensuelRepository.findById(971L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findById(11L)).thenReturn(Optional.of(arhCreateur));

        RetournerProcessusResponseDto reponse = processusMensuelService.retourner(971L, requete);

        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.RETOURNE);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdActeur()).isEqualTo(40L);
        assertThat(etape.getOrdreEtape()).isEqualTo(3);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_DRH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.RETOURNEE);

        verify(auditService).enregistrer(eq(40L), eq("RETOUR_PROCESSUS"), eq("processus_mensuel"),
                eq(971L), anyMap(), anyMap());
    }

    @Test
    void retourner_motifVide_leveMotifRejetObligatoire() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(972L).statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("   ");

        when(processusMensuelRepository.findById(972L)).thenReturn(Optional.of(processus));

        assertThatThrownBy(() -> processusMensuelService.retourner(972L, requete))
                .isInstanceOf(MotifRejetObligatoireException.class);

        verify(etapeWorkflowRepository, never()).save(any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
        verify(notificationService, never()).notifier(any(), any(), any());
    }

    @Test
    void retourner_statutIncompatible_leve409() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(973L).statut(StatutEnum.CLOTURE).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Tentative de retour tardive");

        when(processusMensuelRepository.findById(973L)).thenReturn(Optional.of(processus));

        assertThatThrownBy(() -> processusMensuelService.retourner(973L, requete))
                .isInstanceOf(ProcessusMensuelNonModifiableException.class);

        verify(etapeWorkflowRepository, never()).save(any());
        verify(notificationService, never()).notifier(any(), any(), any());
    }

    @Test
    void retourner_notifieArhCreateurAvecLeMotif() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(974L).moisPaiement(9).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).idCreateur(12L).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(41L).matricule("4002").nom("ONANA").prenom("Serge")
                .role(RoleEnum.DRH).actif(true).build();
        Utilisateur arhCreateur = Utilisateur.builder().id(12L).matricule("2203").nom("EYENGA").prenom("Christian")
                .role(RoleEnum.ARH).actif(true).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Beneficiaire TCHINDA Paul exclu a tort");

        when(processusMensuelRepository.findById(974L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findById(12L)).thenReturn(Optional.of(arhCreateur));

        processusMensuelService.retourner(974L, requete);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifier(eq(arhCreateur), any(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("Beneficiaire TCHINDA Paul exclu a tort");
    }

    @Test
    void cycleRetourCorrectionRevalidationArh_reutiliseUnePieceJointeUnique() {
        // Decision Sprint 5.4 : RETOURNE n'est pas terminal -- l'ARH corrige via
        // PATCH (deja accepte pour RETOURNE) puis revalide via POST valider()
        // (branche ARH, elle aussi etendue a RETOURNE). RG-06 : le PDF issu de
        // la revalidation reutilise le meme enregistrement PieceJointe, jamais
        // un second (verifie plus en detail au niveau de DocumentServiceTest).
        ProcessusMensuel processus = ProcessusMensuel.builder().id(980L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();
        Utilisateur crhConnecte = Utilisateur.builder().id(21L).matricule("3002").nom("NKOLO").prenom("Sylvie")
                .role(RoleEnum.CRH).actif(true).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").nom("MBARGA").prenom("Jean-Paul")
                .role(RoleEnum.ARH).actif(true).build();

        RetournerProcessusRequestDto requeteRetour = new RetournerProcessusRequestDto();
        requeteRetour.setMotif("Montant incorrect pour le matricule 1562");

        when(processusMensuelRepository.findById(980L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte, arhConnecte, arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(arhConnecte));

        // 1. Retour CRH -> ARH
        processusMensuelService.retourner(980L, requeteRetour);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);

        // 2. Correction ARH via PATCH, deja accepte pour le statut RETOURNE
        LigneEtatMensuel ligneExclue = LigneEtatMensuel.builder().id(3001L).idProcessus(980L).idBeneficiaire(601L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();
        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(601L);
        ajustement.setInclusDansEtat(false);
        PatchProcessusRequestDto requetePatch = new PatchProcessusRequestDto();
        requetePatch.setAjustements(List.of(ajustement));

        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(980L, 601L)).thenReturn(Optional.of(ligneExclue));
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processusMensuelService.ajuster(980L, requetePatch);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);

        // 3. Revalidation ARH : reutilise le meme PieceJointe (RG-06), signatures reinitialisees a 1
        LigneEtatMensuel ligneIncluse = LigneEtatMensuel.builder().id(3002L).idProcessus(980L).idBeneficiaire(602L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("CHEF_DEPARTEMENT").build();
        PieceJointe pieceJointeRegeneree = PieceJointe.builder().id(60L).idProcessus(980L)
                .nomFichier("dotations-telephoniques-7-2026.pdf").nombreSignatures(1).build();

        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(980L)).thenReturn(List.of(ligneIncluse));
        when(documentService.genererInitiale(processus, List.of(ligneIncluse), arhConnecte)).thenReturn(pieceJointeRegeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 24/07/2026 12:00:00");
        when(utilisateurRepository.findByRoleAndActifTrue(RoleEnum.CRH)).thenReturn(List.of());

        ValiderProcessusResponseDto reponseRevalidation = processusMensuelService.valider(980L, "Corrige et revalide");

        assertThat(reponseRevalidation.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponseRevalidation.getIdPieceJointe()).isEqualTo(60L);
        assertThat(pieceJointeRegeneree.getNombreSignatures()).isEqualTo(1);

        verify(documentService, times(1)).genererInitiale(any(), any(), any());
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        verify(pieceJointeRepository, never()).findByIdProcessus(any());
    }

    @Test
    void consulterDetail_casNominal_retourneLesLignesInclusesEtExclues() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(700L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();

        LigneEtatMensuel ligneIncluse = LigneEtatMensuel.builder().id(1L).idProcessus(700L).idBeneficiaire(501L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("GFC").build();
        LigneEtatMensuel ligneExclue = LigneEtatMensuel.builder().id(2L).idProcessus(700L).idBeneficiaire(502L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("NON_ELIGIBLE").build();

        Beneficiaire nkolo = Beneficiaire.builder().id(501L).matricule("1001").nomPrenoms("NKOLO Emmanuel").build();
        Beneficiaire essama = Beneficiaire.builder().id(502L).matricule("1002").nomPrenoms("ESSAMA Marie Claire").build();

        when(processusMensuelRepository.findById(700L)).thenReturn(Optional.of(processus));
        when(ligneEtatMensuelRepository.findByIdProcessus(700L)).thenReturn(List.of(ligneIncluse, ligneExclue));
        when(beneficiaireRepository.findAllById(List.of(501L, 502L))).thenReturn(List.of(nkolo, essama));

        ProcessusDetailResponseDto reponse = processusMensuelService.consulterDetail(700L);

        assertThat(reponse.getId()).isEqualTo(700L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponse.getLignesEtatMensuel()).hasSize(2);
        assertThat(reponse.getLignesEtatMensuel())
                .extracting("matricule", "inclusDansEtat")
                .containsExactlyInAnyOrder(
                        tuple("1001", true),
                        tuple("1002", false));
    }

    @Test
    void consulterDetail_processusRetourne_exposeLeMotifEtLOrigineDuDernierRetour() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(700L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.RETOURNE).idCreateur(10L).build();

        EtapeWorkflow retourCrh = EtapeWorkflow.builder().id(1L).idProcessus(700L).idActeur(2L)
                .ordreEtape(2).nomEtape(NomEtapeEnum.VALIDATION_CRH).statutEtape(StatutEtapeEnum.RETOURNEE)
                .dateAction(java.time.LocalDateTime.of(2026, 7, 20, 9, 0))
                .motifRetour("Fonction retenue incorrecte pour NKOLO Emmanuel.").build();

        when(processusMensuelRepository.findById(700L)).thenReturn(Optional.of(processus));
        when(ligneEtatMensuelRepository.findByIdProcessus(700L)).thenReturn(List.of());
        when(beneficiaireRepository.findAllById(List.of())).thenReturn(List.of());
        when(etapeWorkflowRepository.findFirstByIdProcessusAndStatutEtapeOrderByDateActionDesc(700L, StatutEtapeEnum.RETOURNEE))
                .thenReturn(Optional.of(retourCrh));

        ProcessusDetailResponseDto reponse = processusMensuelService.consulterDetail(700L);

        assertThat(reponse.getMotifRetour()).isEqualTo("Fonction retenue incorrecte pour NKOLO Emmanuel.");
        assertThat(reponse.getOrigineRetour()).isEqualTo("CRH");
    }

    // Un processus peut etre retourne plusieurs fois au fil de ses resoumissions
    // (RETOURNE n'est pas terminal). Le service doit exposer le motif du DERNIER
    // retour (celui retourne par le repository trie par dateAction desc), jamais
    // un motif d'un cycle anterieur, meme si celui-ci vient d'une autre etape.
    @Test
    void consulterDetail_processusRetourneDeuxFois_exposeUniquementLeDernierMotif() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(701L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.RETOURNE).idCreateur(10L).build();

        EtapeWorkflow retourDrhPlusRecent = EtapeWorkflow.builder().id(5L).idProcessus(701L).idActeur(3L)
                .ordreEtape(3).nomEtape(NomEtapeEnum.VALIDATION_DRH).statutEtape(StatutEtapeEnum.RETOURNEE)
                .dateAction(java.time.LocalDateTime.of(2026, 7, 25, 14, 0))
                .motifRetour("Montant applique non conforme a la grille en vigueur.").build();

        when(processusMensuelRepository.findById(701L)).thenReturn(Optional.of(processus));
        when(ligneEtatMensuelRepository.findByIdProcessus(701L)).thenReturn(List.of());
        when(beneficiaireRepository.findAllById(List.of())).thenReturn(List.of());
        // Le repository (findFirst...OrderByDateActionDesc) ne renvoie que la
        // plus recente : le premier retour CRH n'est pas remonte ici, simulant
        // le tri reel qui l'exclurait.
        when(etapeWorkflowRepository.findFirstByIdProcessusAndStatutEtapeOrderByDateActionDesc(701L, StatutEtapeEnum.RETOURNEE))
                .thenReturn(Optional.of(retourDrhPlusRecent));

        ProcessusDetailResponseDto reponse = processusMensuelService.consulterDetail(701L);

        assertThat(reponse.getMotifRetour()).isEqualTo("Montant applique non conforme a la grille en vigueur.");
        assertThat(reponse.getOrigineRetour()).isEqualTo("DRH");
    }

    @Test
    void consulterDetail_processusJamaisRetourne_neRemonteAucunMotif() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(702L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();

        when(processusMensuelRepository.findById(702L)).thenReturn(Optional.of(processus));
        when(ligneEtatMensuelRepository.findByIdProcessus(702L)).thenReturn(List.of());
        when(beneficiaireRepository.findAllById(List.of())).thenReturn(List.of());
        when(etapeWorkflowRepository.findFirstByIdProcessusAndStatutEtapeOrderByDateActionDesc(702L, StatutEtapeEnum.RETOURNEE))
                .thenReturn(Optional.empty());

        ProcessusDetailResponseDto reponse = processusMensuelService.consulterDetail(702L);

        assertThat(reponse.getMotifRetour()).isNull();
        assertThat(reponse.getOrigineRetour()).isNull();
    }

    @Test
    void consulterDetail_processusIntrouvable_leve404() {
        when(processusMensuelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processusMensuelService.consulterDetail(999L))
                .isInstanceOf(ProcessusMensuelIntrouvableException.class);
    }

    @Test
    void metadonneesPieceJointe_casNominal_retourneLesInfos() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(700L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();
        PieceJointe pieceJointe = PieceJointe.builder().id(60L).idProcessus(700L)
                .nomFichier("dotations-telephoniques-7-2026.pdf")
                .nombreSignatures(1).build();

        when(processusMensuelRepository.findById(700L)).thenReturn(Optional.of(processus));
        when(pieceJointeRepository.findByIdProcessus(700L)).thenReturn(Optional.of(pieceJointe));

        PieceJointeMetadonneesResponseDto reponse = processusMensuelService.obtenirMetadonneesPieceJointe(700L);

        assertThat(reponse.getId()).isEqualTo(60L);
        assertThat(reponse.getNomFichier()).isEqualTo("dotations-telephoniques-7-2026.pdf");
        assertThat(reponse.getNombreSignatures()).isEqualTo(1);
    }

    @Test
    void metadonneesPieceJointe_aucunePieceJointe_leve404() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(700L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).idCreateur(10L).build();

        when(processusMensuelRepository.findById(700L)).thenReturn(Optional.of(processus));
        when(pieceJointeRepository.findByIdProcessus(700L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processusMensuelService.obtenirMetadonneesPieceJointe(700L))
                .isInstanceOf(PieceJointeIntrouvableException.class);
    }

    @Test
    void telechargerPieceJointe_fichierAbsentDuDisque_leve404() {
        PieceJointe pieceJointe = PieceJointe.builder().id(60L).idProcessus(700L)
                .nomFichier("dotations-telephoniques-7-2026.pdf")
                .cheminStockage(Path.of(System.getProperty("java.io.tmpdir"), "fichier-inexistant-sprint61.pdf").toString())
                .nombreSignatures(1).build();

        when(pieceJointeRepository.findById(60L)).thenReturn(Optional.of(pieceJointe));

        assertThatThrownBy(() -> processusMensuelService.obtenirPieceJointePourTelechargement(60L))
                .isInstanceOf(PieceJointeIntrouvableException.class);
    }
}