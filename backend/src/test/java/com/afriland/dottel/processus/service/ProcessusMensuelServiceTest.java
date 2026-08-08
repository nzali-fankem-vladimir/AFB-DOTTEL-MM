package com.afriland.dottel.processus.service;
import com.afriland.dottel.beneficiaires.api.BeneficiaireApi;
import com.afriland.dottel.beneficiaires.api.BeneficiaireDotationDto;
import com.afriland.dottel.beneficiaires.api.BeneficiaireIdentiteDto;
import com.afriland.dottel.referentiel.api.GrilleTarifaireApi;
import com.afriland.dottel.referentiel.api.ResolutionGrilleDto;
import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.api.UtilisateurApi;
import com.afriland.dottel.utilisateurs.api.AuthenticatedUserService;
import com.afriland.dottel.audit.api.EvenementAudit;

import com.afriland.dottel.processus.exception.MotifRejetObligatoireException;
import com.afriland.dottel.processus.exception.PieceJointeIntrouvableException;
import com.afriland.dottel.processus.exception.ProcessusMensuelExisteDejaException;
import com.afriland.dottel.processus.exception.ProcessusMensuelIntrouvableException;
import com.afriland.dottel.processus.exception.ProcessusMensuelNonModifiableException;
import com.afriland.dottel.processus.exception.RoleEtapeNonAutoriseException;
import com.afriland.dottel.processus.exception.SeparationTachesViolationException;
import com.afriland.dottel.processus.model.dto.processus.AjustementLigneEtatDto;
import com.afriland.dottel.processus.model.dto.processus.DeclencherProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.PatchProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.PatchProcessusResponseDto;
import com.afriland.dottel.processus.model.dto.processus.PieceJointeMetadonneesResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ProcessusDetailResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ProcessusListItemDto;
import com.afriland.dottel.processus.model.dto.processus.ProcessusMensuelResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ResultatAjustementDto;
import com.afriland.dottel.processus.model.dto.processus.RetournerProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.RetournerProcessusResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ValiderProcessusResponseDto;
import com.afriland.dottel.processus.model.entity.EtapeWorkflow;
import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.PieceJointe;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.processus.model.enums.NomEtapeEnum;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import com.afriland.dottel.processus.model.enums.StatutEtapeEnum;
import com.afriland.dottel.processus.repository.EtapeWorkflowRepository;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import com.afriland.dottel.processus.repository.PieceJointeRepository;
import com.afriland.dottel.processus.repository.ProcessusMensuelRepository;
import com.afriland.dottel.referentiel.api.EligibiliteService;
import com.afriland.dottel.referentiel.api.FonctionEligibleApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private BeneficiaireApi beneficiaireApi;

    @Mock
    private GrilleTarifaireApi grilleTarifaireApi;

    @Mock
    private LigneEtatMensuelRepository ligneEtatMensuelRepository;

    @Mock
    private EtapeWorkflowRepository etapeWorkflowRepository;

    @Mock
    private PieceJointeRepository pieceJointeRepository;

    @Mock
    private UtilisateurApi utilisateurApi;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    // Attention : un mock Mockito renvoie false par defaut sur un boolean. Tout
    // test d'ajustement qui traverse la revalidation RG-02 doit donc stubber
    // explicitement verifierEligibilite(...) -> true, sinon la ligne est rejetee.
    @Mock
    private EligibiliteService eligibiliteService;

    // Couplage C4 (Sprint MM.3) : assemblerDonneesDocument() resout le libelle
    // de fonction pour le PDF. Non stubbe explicitement dans la plupart des
    // tests : Mockito renvoie Optional.empty() par defaut, ce qui declenche le
    // meme repli sur le code brut qu'avant (voir DocumentServiceTest).
    @Mock
    private FonctionEligibleApi fonctionEligibleService;

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

        BeneficiaireDotationDto nkolo =
                new BeneficiaireDotationDto(501L, "3164", "NKOLO Emmanuel", "DA");
        BeneficiaireDotationDto essama =
                new BeneficiaireDotationDto(502L, "5522", "ESSAMA Solange", "CHEF_DEPARTEMENT");



        when(processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(7, 2026)).thenReturn(false);
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> {
            ProcessusMensuel processus = invocation.getArgument(0);
            processus.setId(900L);
            return processus;
        });
        when(beneficiaireApi.listerActifsPourDotation()).thenReturn(List.of(nkolo, essama));
        when(grilleTarifaireApi.resoudrePourFonction("DA"))
                .thenReturn(ResolutionGrilleDto.resolue(50000));
        when(grilleTarifaireApi.resoudrePourFonction("CHEF_DEPARTEMENT"))
                .thenReturn(ResolutionGrilleDto.resolue(40000));
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

        ArgumentCaptor<EvenementAudit> evenementCaptor900 = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor900.capture());
        EvenementAudit evenement900 = evenementCaptor900.getValue();
        assertThat(evenement900.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement900.action()).isEqualTo("DECLENCHEMENT_PROCESSUS");
        assertThat(evenement900.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement900.idEntite()).isEqualTo(900L);
        assertThat(evenement900.avant()).isNull();
        assertThat(evenement900.apres()).isNotNull();
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
        verify(beneficiaireApi, never()).listerActifsPourDotation();
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
        when(beneficiaireApi.listerActifsPourDotation()).thenReturn(List.of());

        ProcessusMensuelResponseDto reponse = processusMensuelService.declencher(requete);

        assertThat(reponse.getId()).isEqualTo(901L);
        assertThat(reponse.getNombreBeneficiaires()).isZero();
        assertThat(reponse.getBeneficiairesExclus()).isEmpty();

        verify(ligneEtatMensuelRepository, never()).save(any());
        ArgumentCaptor<EvenementAudit> evenementCaptor901 = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor901.capture());
        EvenementAudit evenement901 = evenementCaptor901.getValue();
        assertThat(evenement901.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement901.action()).isEqualTo("DECLENCHEMENT_PROCESSUS");
        assertThat(evenement901.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement901.idEntite()).isEqualTo(901L);
        assertThat(evenement901.avant()).isNull();
        assertThat(evenement901.apres()).isNotNull();
    }

    @Test
    void declencher_beneficiaireSansGrilleActive_appliqueLaDecisionEtape3() {
        DeclencherProcessusRequestDto requete = new DeclencherProcessusRequestDto();
        requete.setMoisPaiement(9);
        requete.setAnneePaiement(2026);

        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        BeneficiaireDotationDto tchinda =
                new BeneficiaireDotationDto(503L, "6633", "TCHINDA Paul", "JURISTE");


        when(processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(9, 2026)).thenReturn(false);
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> {
            ProcessusMensuel processus = invocation.getArgument(0);
            processus.setId(902L);
            return processus;
        });
        when(beneficiaireApi.listerActifsPourDotation()).thenReturn(List.of(tchinda));
        when(grilleTarifaireApi.resoudrePourFonction("JURISTE"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE));
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

        ArgumentCaptor<EvenementAudit> evenementCaptor2001 = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor2001.capture());
        EvenementAudit evenement2001 = evenementCaptor2001.getValue();
        assertThat(evenement2001.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement2001.action()).isEqualTo("AJUSTEMENT_LIGNE_ETAT_MENSUEL");
        assertThat(evenement2001.entiteCible()).isEqualTo("ligne_etat_mensuel");
        assertThat(evenement2001.idEntite()).isEqualTo(2001L);
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

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 502L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("CHEF_DEPARTEMENT"))
                .thenReturn(ResolutionGrilleDto.resolue(40000));
        when(beneficiaireApi.gradeDe(502L)).thenReturn(Optional.of("Grade 3"));
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

        ArgumentCaptor<EvenementAudit> evenementCaptor2002 = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor2002.capture());
        EvenementAudit evenement2002 = evenementCaptor2002.getValue();
        assertThat(evenement2002.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement2002.action()).isEqualTo("AJUSTEMENT_LIGNE_ETAT_MENSUEL");
        assertThat(evenement2002.entiteCible()).isEqualTo("ligne_etat_mensuel");
        assertThat(evenement2002.idEntite()).isEqualTo(2002L);
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


        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(503L);
        ajustement.setFonctionRetenue("DIRECTEUR");

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 503L)).thenReturn(Optional.of(ligne));

        when(grilleTarifaireApi.resoudrePourFonction("DIRECTEUR"))
                .thenReturn(ResolutionGrilleDto.resolue(60000));
        when(beneficiaireApi.gradeDe(503L)).thenReturn(Optional.of("Grade 5"));
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
        verify(eventPublisher, never()).publishEvent(any());
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


        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(504L);
        ajustement.setFonctionRetenue("CHEF_DIVISION");
        ajustement.setInclusDansEtat(false);

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 504L)).thenReturn(Optional.of(ligne));

        when(grilleTarifaireApi.resoudrePourFonction("CHEF_DIVISION"))
                .thenReturn(ResolutionGrilleDto.resolue(35000));
        when(beneficiaireApi.gradeDe(504L)).thenReturn(Optional.of("Grade 4"));
        when(eligibiliteService.verifierEligibilite("CHEF_DIVISION", "Grade 4")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processusMensuelService.ajuster(900L, requete);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement.action()).isEqualTo("AJUSTEMENT_LIGNE_ETAT_MENSUEL");
        assertThat(evenement.entiteCible()).isEqualTo("ligne_etat_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(2004L);

        Map<String, Object> avant = evenement.avant();
        Map<String, Object> apres = evenement.apres();

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
        verify(eventPublisher, never()).publishEvent(any());
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


        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(506L);
        ajustement.setFonctionRetenue("JURISTE");

        PatchProcessusRequestDto requete = new PatchProcessusRequestDto();
        requete.setAjustements(List.of(ajustement));

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 506L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("JURISTE"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(900L, requete);

        ResultatAjustementDto resultat = reponse.getResultats().get(0);
        assertThat(resultat.getIdBeneficiaire()).isEqualTo(506L);
        assertThat(resultat.getApplique()).isFalse();
        assertThat(resultat.getMotifRejet()).isEqualTo("Aucune grille tarifaire ACTIVE pour cette fonction");

        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        // Retour anticipe sur la grille : le grade n'est jamais interroge.
        verify(beneficiaireApi, never()).gradeDe(any());
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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 507L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("CORPS_CONTROLE_IG"))
                .thenReturn(ResolutionGrilleDto.resolue(70000));
        when(beneficiaireApi.gradeDe(507L)).thenReturn(Optional.of("NON GRADE"));
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
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void ajuster_fonctionCorpsControleAvecGradeReel_appliqueLajustement() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2011L).idProcessus(900L).idBeneficiaire(508L)
                .montantApplique(50000).inclusDansEtat(true).fonctionRetenue("DA").build();



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 508L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("CORPS_CONTROLE_IGA"))
                .thenReturn(ResolutionGrilleDto.resolue(65000));
        when(beneficiaireApi.gradeDe(508L)).thenReturn(Optional.of("Grade 6"));
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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 509L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("COMPTABLE"))
                .thenReturn(ResolutionGrilleDto.resolue(35000));
        when(beneficiaireApi.gradeDe(509L)).thenReturn(Optional.empty());
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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 510L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("CONTROLEUR_GESTION"))
                .thenReturn(ResolutionGrilleDto.resolue(35000));
        when(beneficiaireApi.gradeDe(510L)).thenReturn(Optional.of("   "));
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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 511L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("DIRECTEUR"))
                .thenReturn(ResolutionGrilleDto.resolue(60000));
        when(beneficiaireApi.gradeDe(511L)).thenReturn(Optional.empty());
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
        when(grilleTarifaireApi.resoudrePourFonction("FONCTION_INEXISTANTE"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_FONCTION_INCONNUE));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(512L, "FONCTION_INEXISTANTE")));

        assertThat(reponse.getResultats().get(0).getMotifRejet())
                .isEqualTo("Fonction inconnue du référentiel fonction_eligible");
        verify(beneficiaireApi, never()).gradeDe(any());
        verify(eligibiliteService, never()).verifierEligibilite(any(), any());
    }

    @Test
    void ajuster_fonctionDesactivee_neVerifiePasLeGrade() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2016L).idProcessus(900L).idBeneficiaire(513L)
                .montantApplique(35000).inclusDansEtat(true).fonctionRetenue("CHEF_DIVISION").build();


        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 513L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("COMPTABLE"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_FONCTION_DESACTIVEE));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementFonction(513L, "COMPTABLE")));

        assertThat(reponse.getResultats().get(0).getMotifRejet()).isEqualTo("Fonction désactivée");
        verify(beneficiaireApi, never()).gradeDe(any());
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


        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 514L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("CONTROLEUR_COMPTABLE"))
                .thenReturn(ResolutionGrilleDto.resolue(35000));
        when(beneficiaireApi.gradeDe(514L)).thenReturn(Optional.empty());
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



        AjustementLigneEtatDto ajustement = new AjustementLigneEtatDto();
        ajustement.setIdBeneficiaire(515L);
        ajustement.setFonctionRetenue("COMPTABLE");
        ajustement.setInclusDansEtat(true);

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 515L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("COMPTABLE"))
                .thenReturn(ResolutionGrilleDto.resolue(35000));
        when(beneficiaireApi.gradeDe(515L)).thenReturn(Optional.of("NON GRADE"));
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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 516L)).thenReturn(Optional.of(ligneEssama));
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 517L)).thenReturn(Optional.of(ligneBelinga));
        when(grilleTarifaireApi.resoudrePourFonction("DIRECTEUR"))
                .thenReturn(ResolutionGrilleDto.resolue(60000));
        when(grilleTarifaireApi.resoudrePourFonction("CORPS_CONTROLE_IG"))
                .thenReturn(ResolutionGrilleDto.resolue(70000));
        when(beneficiaireApi.gradeDe(516L)).thenReturn(Optional.of("Grade 3"));
        when(beneficiaireApi.gradeDe(517L)).thenReturn(Optional.of("NON GRADE"));
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


        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 518L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("CHEF_PRODUIT"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_FONCTION_DESACTIVEE));

        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(
                900L, requeteAvec(ajustementInclusion(518L, true)));

        assertThat(reponse.getResultats().get(0).getApplique()).isFalse();
        assertThat(reponse.getResultats().get(0).getMotifRejet()).isEqualTo("Fonction désactivée");
        assertThat(ligne.getInclusDansEtat()).isFalse();
        verify(ligneEtatMensuelRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void ajuster_reintegrerLigneExclueSansGrilleActive_appliqueFalseAvecMotifRejet() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2022L).idProcessus(900L).idBeneficiaire(519L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("JURISTE").build();


        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 519L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("JURISTE"))
                .thenReturn(ResolutionGrilleDto.exclue(ResolutionGrilleDto.MOTIF_GRILLE_INTROUVABLE));

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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 520L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("COMPTABLE"))
                .thenReturn(ResolutionGrilleDto.resolue(35000));
        when(beneficiaireApi.gradeDe(520L)).thenReturn(Optional.of("NON GRADE"));
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



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 521L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("GFC"))
                .thenReturn(ResolutionGrilleDto.resolue(40000));
        when(beneficiaireApi.gradeDe(521L)).thenReturn(Optional.of("Grade 2"));
        when(eligibiliteService.verifierEligibilite("GFC", "Grade 2")).thenReturn(true);
        when(ligneEtatMensuelRepository.save(any(LigneEtatMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processusMensuelService.ajuster(900L, requeteAvec(ajustementInclusion(521L, true)));

        assertThat(ligne.getInclusDansEtat()).isTrue();
        assertThat(ligne.getMontantApplique()).isEqualTo(40000);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement.action()).isEqualTo("AJUSTEMENT_LIGNE_ETAT_MENSUEL");
        assertThat(evenement.entiteCible()).isEqualTo("ligne_etat_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(2024L);
        assertThat(evenement.avant()).containsEntry("montantApplique", 0);
        assertThat(evenement.apres()).containsEntry("montantApplique", 40000);
    }

    // Non-regression du cas legitime : une ligne exclue par l'ARH (et non au
    // declenchement) conserve son montant ; la reintegration reste possible.
    @Test
    void ajuster_reintegrerLigneExclueParLarh_conserveLeMontant() {
        Utilisateur arhConnecte = Utilisateur.builder().id(10L).matricule("2201").build();

        LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                .id(2025L).idProcessus(900L).idBeneficiaire(522L)
                .montantApplique(50000).inclusDansEtat(false).fonctionRetenue("DA").build();



        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processusEnCoursArh()));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndIdBeneficiaire(900L, 522L)).thenReturn(Optional.of(ligne));
        when(grilleTarifaireApi.resoudrePourFonction("DA"))
                .thenReturn(ResolutionGrilleDto.resolue(50000));
        when(beneficiaireApi.gradeDe(522L)).thenReturn(Optional.of("Grade 5"));
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
        verify(grilleTarifaireApi, never()).resoudrePourFonction(any());
        verify(beneficiaireApi, never()).gradeDe(any());
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
        when(documentService.genererInitiale(eq(processus), eq(List.of(ligneIncluse)), any(), eq(arhConnecte)))
                .thenReturn(pieceJointeGeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 22/07/2026 10:00:00");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinatairesParRole(RoleEnum.CRH)).thenReturn(List.of());

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

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(10L);
        assertThat(evenement.action()).isEqualTo("VALIDATION_PROCESSUS_ARH");
        assertThat(evenement.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(900L);
    }

    @Test
    void valider_processusDejaValide_leve409() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(900L).statut(StatutEnum.CLOTURE).build();

        when(processusMensuelRepository.findById(900L)).thenReturn(Optional.of(processus));

        assertThatThrownBy(() -> processusMensuelService.valider(900L, "commentaire"))
                .isInstanceOf(ProcessusMensuelNonModifiableException.class);

        verify(documentService, never()).genererInitiale(any(), any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(ligneEtatMensuelRepository, never()).findByIdProcessusAndInclusDansEtatTrue(any());
    }

    @Test
    void valider_processusIntrouvable_leve404() {
        when(processusMensuelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processusMensuelService.valider(999L, null))
                .isInstanceOf(ProcessusMensuelIntrouvableException.class);

        verify(documentService, never()).genererInitiale(any(), any(), any(), any());
    }

    @Test
    void valider_appelleNotificationService_uneFoisParDestinataireCRH() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(910L).moisPaiement(8).anneePaiement(2026)
                .statut(StatutEnum.EN_COURS_ARH).build();
        Utilisateur arhConnecte = Utilisateur.builder().id(11L).matricule("2202").nom("ESSAMA").prenom("Marie-Claire").build();
        DestinataireNotificationDto crh1 =
                new DestinataireNotificationDto("p.atangana@afrilandfirstbank.cm", RoleEnum.CRH);
        DestinataireNotificationDto crh2 =
                new DestinataireNotificationDto("s.nkolo@afrilandfirstbank.cm", RoleEnum.CRH);

        PieceJointe pieceJointeGeneree = PieceJointe.builder().id(701L).idProcessus(910L).build();

        when(processusMensuelRepository.findById(910L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(arhConnecte);
        when(ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(910L)).thenReturn(List.of());
        when(documentService.genererInitiale(eq(processus), any(), any(), eq(arhConnecte))).thenReturn(pieceJointeGeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("signature");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinatairesParRole(RoleEnum.CRH)).thenReturn(List.of(crh1, crh2));

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

        verify(documentService, never()).genererInitiale(any(), any(), any(), any());
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
        when(utilisateurApi.destinatairesParRole(RoleEnum.DRH)).thenReturn(List.of());

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

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(21L);
        assertThat(evenement.action()).isEqualTo("VALIDATION_PROCESSUS_CRH");
        assertThat(evenement.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(930L);
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
        verify(documentService, never()).genererInitiale(any(), any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
        verify(notificationService, never()).notifier(any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
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
        DestinataireNotificationDto drh1 =
                new DestinataireNotificationDto("a.belinga@afrilandfirstbank.cm", RoleEnum.DRH);
        DestinataireNotificationDto drh2 =
                new DestinataireNotificationDto("s.onana@afrilandfirstbank.cm", RoleEnum.DRH);

        PieceJointe pieceJointeExistante = PieceJointe.builder().id(706L).idProcessus(932L).nombreSignatures(1).build();

        when(processusMensuelRepository.findById(932L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte);
        when(pieceJointeRepository.findByIdProcessus(932L)).thenReturn(Optional.of(pieceJointeExistante));
        when(documentService.ajouterSignature(pieceJointeExistante, crhConnecte, NomEtapeEnum.VALIDATION_CRH))
                .thenReturn(pieceJointeExistante);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinatairesParRole(RoleEnum.DRH)).thenReturn(List.of(drh1, drh2));

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
        when(documentService.genererInitiale(eq(processus), eq(List.of(ligneIncluse)), any(), eq(arhConnecte)))
                .thenReturn(pieceJointeGeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("Christian EYENGA (matricule 2203) - 24/07/2026 09:00:00");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinatairesParRole(RoleEnum.CRH)).thenReturn(List.of());

        ValiderProcessusResponseDto reponse = processusMensuelService.valider(940L, "État vérifié niveau ARH");

        assertThat(reponse.getId()).isEqualTo(940L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponse.getEtapeValidee()).isEqualTo("VALIDATION_ARH");
        assertThat(reponse.getIdPieceJointe()).isEqualTo(710L);

        verify(documentService).genererInitiale(eq(processus), eq(List.of(ligneIncluse)), any(), eq(arhConnecte));
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        verify(pieceJointeRepository, never()).findByIdProcessus(any());
        verify(separationTachesService, never()).verifier(any(), any(), any());

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(12L);
        assertThat(evenement.action()).isEqualTo("VALIDATION_PROCESSUS_ARH");
        assertThat(evenement.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(940L);
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

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher, times(2)).publishEvent(evenementCaptor.capture());
        List<EvenementAudit> evenements = evenementCaptor.getAllValues();

        EvenementAudit evenementValidation = evenements.stream()
                .filter(e -> "VALIDATION_PROCESSUS_DRH".equals(e.action())).findFirst().orElseThrow();
        assertThat(evenementValidation.idUtilisateur()).isEqualTo(40L);
        assertThat(evenementValidation.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenementValidation.idEntite()).isEqualTo(950L);

        EvenementAudit evenementCloture = evenements.stream()
                .filter(e -> "CLOTURE_PROCESSUS".equals(e.action())).findFirst().orElseThrow();
        assertThat(evenementCloture.idUtilisateur()).isEqualTo(40L);
        assertThat(evenementCloture.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenementCloture.idEntite()).isEqualTo(950L);
        assertThat(evenementCloture.avant()).isNull();
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
        when(documentService.genererInitiale(eq(processus), eq(List.of(ligneIncluse)), any(), eq(arhConnecte)))
                .thenReturn(pieceJointe);
        when(signatureService.signer(arhConnecte)).thenReturn("Paul ATANGANA (matricule 2210) - 24/07/2026 08:00:00");
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinatairesParRole(RoleEnum.CRH)).thenReturn(List.of());
        when(utilisateurApi.destinatairesParRole(RoleEnum.DRH)).thenReturn(List.of());
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
        DestinataireNotificationDto arhCreateur =
                new DestinataireNotificationDto("jp.mbarga@afrilandfirstbank.cm", RoleEnum.ARH);
        PieceJointe pieceJointe = PieceJointe.builder().id(80L).idProcessus(970L).nombreSignatures(1).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Montant incorrect pour le matricule 1562");

        when(processusMensuelRepository.findById(970L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinataireParId(10L)).thenReturn(arhCreateur);
        when(pieceJointeRepository.findByIdProcessus(970L)).thenReturn(Optional.of(pieceJointe));

        RetournerProcessusResponseDto reponse = processusMensuelService.retourner(970L, requete);

        assertThat(reponse.getId()).isEqualTo(970L);
        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.RETOURNE);
        assertThat(reponse.getMotif()).isEqualTo("Montant incorrect pour le matricule 1562");
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);

        // Retour CRH : aucune signature CRH n'existe encore -> aucune page
        // annexe n'a de sens, invaliderSignatureCrh() n'est jamais appelee.
        // Seul le compteur est remis a 0, decision S-1 (2026-08-03).
        verify(documentService, never()).invaliderSignatureCrh(any(), any(), any());
        assertThat(pieceJointe.getNombreSignatures()).isEqualTo(0);
        verify(pieceJointeRepository).save(pieceJointe);

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdProcessus()).isEqualTo(970L);
        assertThat(etape.getIdActeur()).isEqualTo(21L);
        assertThat(etape.getOrdreEtape()).isEqualTo(2);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_CRH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.RETOURNEE);
        assertThat(etape.getMotifRetour()).isEqualTo("Montant incorrect pour le matricule 1562");

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(21L);
        assertThat(evenement.action()).isEqualTo("RETOUR_PROCESSUS");
        assertThat(evenement.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(970L);
    }

    @Test
    void retourner_depuisEnAttenteDrh_casNominal_passeAuStatutRetourne() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(971L).moisPaiement(8).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).idCreateur(11L).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(40L).matricule("4001").nom("BELINGA").prenom("Alice")
                .role(RoleEnum.DRH).actif(true).build();
        DestinataireNotificationDto arhCreateur =
                new DestinataireNotificationDto("mc.essama@afrilandfirstbank.cm", RoleEnum.ARH);
        PieceJointe pieceJointe = PieceJointe.builder().id(81L).idProcessus(971L).nombreSignatures(2).build();
        PieceJointe pieceJointeInvalidee = PieceJointe.builder().id(81L).idProcessus(971L).nombreSignatures(0).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Beneficiaire ONANA Serge exclu a tort");

        when(processusMensuelRepository.findById(971L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinataireParId(11L)).thenReturn(arhCreateur);
        when(pieceJointeRepository.findByIdProcessus(971L)).thenReturn(Optional.of(pieceJointe));
        when(documentService.invaliderSignatureCrh(pieceJointe, drhConnecte, "Beneficiaire ONANA Serge exclu a tort"))
                .thenReturn(pieceJointeInvalidee);

        RetournerProcessusResponseDto reponse = processusMensuelService.retourner(971L, requete);

        assertThat(reponse.getStatut()).isEqualTo(StatutEnum.RETOURNE);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);

        // Retour DRH : la signature CRH deja apposee est invalidee (page
        // annexe + compteur a 0, RG-09) via DocumentService, pas directement.
        verify(documentService).invaliderSignatureCrh(pieceJointe, drhConnecte, "Beneficiaire ONANA Serge exclu a tort");

        ArgumentCaptor<EtapeWorkflow> etapeCaptor = ArgumentCaptor.forClass(EtapeWorkflow.class);
        verify(etapeWorkflowRepository).save(etapeCaptor.capture());
        EtapeWorkflow etape = etapeCaptor.getValue();
        assertThat(etape.getIdActeur()).isEqualTo(40L);
        assertThat(etape.getOrdreEtape()).isEqualTo(3);
        assertThat(etape.getNomEtape()).isEqualTo(NomEtapeEnum.VALIDATION_DRH);
        assertThat(etape.getStatutEtape()).isEqualTo(StatutEtapeEnum.RETOURNEE);

        ArgumentCaptor<EvenementAudit> evenementCaptor = ArgumentCaptor.forClass(EvenementAudit.class);
        verify(eventPublisher).publishEvent(evenementCaptor.capture());
        EvenementAudit evenement = evenementCaptor.getValue();
        assertThat(evenement.idUtilisateur()).isEqualTo(40L);
        assertThat(evenement.action()).isEqualTo("RETOUR_PROCESSUS");
        assertThat(evenement.entiteCible()).isEqualTo("processus_mensuel");
        assertThat(evenement.idEntite()).isEqualTo(971L);
    }

    @Test
    void retourner_depuisEnAttenteDrh_pieceJointeIntrouvable_leveExceptionSansAucuneMutation() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(975L).moisPaiement(10).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_DRH).idCreateur(13L).build();
        Utilisateur drhConnecte = Utilisateur.builder().id(42L).matricule("4003").nom("EYENGA").prenom("Christelle")
                .role(RoleEnum.DRH).actif(true).build();

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Test d'anomalie piece jointe manquante");

        when(processusMensuelRepository.findById(975L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(pieceJointeRepository.findByIdProcessus(975L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processusMensuelService.retourner(975L, requete))
                .isInstanceOf(PieceJointeIntrouvableException.class);

        verify(documentService, never()).invaliderSignatureCrh(any(), any(), any());
        verify(etapeWorkflowRepository, never()).save(any());
        verify(processusMensuelRepository, never()).save(any(ProcessusMensuel.class));
        verify(notificationService, never()).notifier(any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
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
        DestinataireNotificationDto arhCreateur =
                new DestinataireNotificationDto("c.eyenga@afrilandfirstbank.cm", RoleEnum.ARH);

        RetournerProcessusRequestDto requete = new RetournerProcessusRequestDto();
        requete.setMotif("Beneficiaire TCHINDA Paul exclu a tort");

        PieceJointe pieceJointe = PieceJointe.builder().id(82L).idProcessus(974L).nombreSignatures(2).build();

        when(processusMensuelRepository.findById(974L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(drhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinataireParId(12L)).thenReturn(arhCreateur);
        when(pieceJointeRepository.findByIdProcessus(974L)).thenReturn(Optional.of(pieceJointe));

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

        PieceJointe pieceJointeAvantRetour = PieceJointe.builder().id(60L).idProcessus(980L).nombreSignatures(1).build();

        when(processusMensuelRepository.findById(980L)).thenReturn(Optional.of(processus));
        when(authenticatedUserService.utilisateurCourant()).thenReturn(crhConnecte, arhConnecte, arhConnecte);
        when(processusMensuelRepository.save(any(ProcessusMensuel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurApi.destinataireParId(10L))
                .thenReturn(new DestinataireNotificationDto("jp.mbarga@afrilandfirstbank.cm", RoleEnum.ARH));
        when(pieceJointeRepository.findByIdProcessus(980L)).thenReturn(Optional.of(pieceJointeAvantRetour));

        // 1. Retour CRH -> ARH
        processusMensuelService.retourner(980L, requeteRetour);
        assertThat(processus.getStatut()).isEqualTo(StatutEnum.RETOURNE);
        assertThat(pieceJointeAvantRetour.getNombreSignatures()).isEqualTo(0);

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
        when(documentService.genererInitiale(eq(processus), eq(List.of(ligneIncluse)), any(), eq(arhConnecte)))
                .thenReturn(pieceJointeRegeneree);
        when(signatureService.signer(arhConnecte)).thenReturn("Jean-Paul MBARGA (matricule 2201) - 24/07/2026 12:00:00");
        when(utilisateurApi.destinatairesParRole(RoleEnum.CRH)).thenReturn(List.of());

        ValiderProcessusResponseDto reponseRevalidation = processusMensuelService.valider(980L, "Corrige et revalide");

        assertThat(reponseRevalidation.getStatut()).isEqualTo(StatutEnum.EN_ATTENTE_CRH);
        assertThat(reponseRevalidation.getIdPieceJointe()).isEqualTo(60L);
        assertThat(pieceJointeRegeneree.getNombreSignatures()).isEqualTo(1);

        verify(documentService, times(1)).genererInitiale(any(), any(), any(), any());
        verify(documentService, never()).ajouterSignature(any(), any(), any());
        // Le seul appel a findByIdProcessus() de tout ce scenario vient du
        // retour CRH (etape 1, remise a 0 du compteur -- anomalie MM.8) : la
        // revalidation ARH elle-meme ne fait pas de lookup separe, genererInitiale()
        // gere la reutilisation de la PieceJointe existante en interne (RG-06).
        verify(pieceJointeRepository, times(1)).findByIdProcessus(980L);
    }

    @Test
    void consulterDetail_casNominal_retourneLesLignesInclusesEtExclues() {
        ProcessusMensuel processus = ProcessusMensuel.builder().id(700L).moisPaiement(7).anneePaiement(2026)
                .statut(StatutEnum.EN_ATTENTE_CRH).idCreateur(10L).build();

        LigneEtatMensuel ligneIncluse = LigneEtatMensuel.builder().id(1L).idProcessus(700L).idBeneficiaire(501L)
                .montantApplique(40000).inclusDansEtat(true).fonctionRetenue("GFC").build();
        LigneEtatMensuel ligneExclue = LigneEtatMensuel.builder().id(2L).idProcessus(700L).idBeneficiaire(502L)
                .montantApplique(0).inclusDansEtat(false).fonctionRetenue("NON_ELIGIBLE").build();

        BeneficiaireIdentiteDto nkolo = new BeneficiaireIdentiteDto(501L, "1001", "NKOLO Emmanuel");
        BeneficiaireIdentiteDto essama = new BeneficiaireIdentiteDto(502L, "1002", "ESSAMA Marie Claire");

        when(processusMensuelRepository.findById(700L)).thenReturn(Optional.of(processus));
        when(ligneEtatMensuelRepository.findByIdProcessus(700L)).thenReturn(List.of(ligneIncluse, ligneExclue));
        when(beneficiaireApi.identitesParId(List.of(501L, 502L)))
                .thenReturn(Map.of(501L, nkolo, 502L, essama));

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
        when(beneficiaireApi.identitesParId(List.of())).thenReturn(Map.of());
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
        when(beneficiaireApi.identitesParId(List.of())).thenReturn(Map.of());
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
        when(beneficiaireApi.identitesParId(List.of())).thenReturn(Map.of());
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