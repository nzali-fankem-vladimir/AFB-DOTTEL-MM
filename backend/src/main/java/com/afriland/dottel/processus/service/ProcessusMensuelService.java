package com.afriland.dottel.processus.service;
import com.afriland.dottel.beneficiaires.api.BeneficiaireApi;
import com.afriland.dottel.beneficiaires.api.BeneficiaireDocumentDto;
import com.afriland.dottel.beneficiaires.api.BeneficiaireDotationDto;
import com.afriland.dottel.beneficiaires.api.BeneficiaireIdentiteDto;
import com.afriland.dottel.referentiel.api.GrilleTarifaireApi;
import com.afriland.dottel.referentiel.api.ResolutionGrilleDto;
import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.api.UtilisateurApi;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.processus.exception.MotifRejetObligatoireException;
import com.afriland.dottel.processus.exception.PieceJointeIntrouvableException;
import com.afriland.dottel.processus.exception.ProcessusMensuelExisteDejaException;
import com.afriland.dottel.processus.exception.ProcessusMensuelIntrouvableException;
import com.afriland.dottel.processus.exception.ProcessusMensuelNonModifiableException;
import com.afriland.dottel.processus.model.dto.processus.AjustementLigneEtatDto;
import com.afriland.dottel.processus.model.dto.processus.BeneficiaireExcluDto;
import com.afriland.dottel.processus.model.dto.processus.DeclencherProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.LigneDocumentDto;
import com.afriland.dottel.processus.model.dto.processus.LigneEtatMensuelDetailDto;
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
import com.afriland.dottel.referentiel.service.EligibiliteService;
import com.afriland.dottel.referentiel.service.FonctionEligibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessusMensuelService {

    // Les trois autres motifs d'exclusion appartiennent au module referentiel
    // depuis MM.2 (voir ResolutionGrilleDto). Celui-ci reste ici : il depend du
    // grade du beneficiaire, pas du referentiel, et n'intervient qu'a
    // l'ajustement -- jamais au declenchement.
    private static final String MOTIF_GRADE_NON_ELIGIBLE = "Grade non éligible pour une fonction de corps de contrôle";

    private final ProcessusMensuelRepository processusMensuelRepository;
    private final BeneficiaireApi beneficiaireApi;
    private final GrilleTarifaireApi grilleTarifaireApi;
    private final LigneEtatMensuelRepository ligneEtatMensuelRepository;
    private final EtapeWorkflowRepository etapeWorkflowRepository;
    private final PieceJointeRepository pieceJointeRepository;
    private final UtilisateurApi utilisateurApi;
    private final AuditService auditService;
    private final EligibiliteService eligibiliteService;
    private final FonctionEligibleService fonctionEligibleService;
    private final AuthenticatedUserService authenticatedUserService;
    private final DocumentService documentService;
    private final SignatureService signatureService;
    private final NotificationService notificationService;
    private final SeparationTachesService separationTachesService;
    private final EvenementClotureService evenementClotureService;

    @Transactional
    public ProcessusMensuelResponseDto declencher(DeclencherProcessusRequestDto requeteDeclenchement) {
        Integer moisPaiement = requeteDeclenchement.getMoisPaiement();
        Integer anneePaiement = requeteDeclenchement.getAnneePaiement();

        if (processusMensuelRepository.existsByMoisPaiementAndAnneePaiement(moisPaiement, anneePaiement)) {
            throw new ProcessusMensuelExisteDejaException(
                    "Un processus mensuel existe déjà pour " + moisPaiement + "/" + anneePaiement);
        }

        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();

        ProcessusMensuel processus = ProcessusMensuel.builder()
                .moisPaiement(moisPaiement)
                .anneePaiement(anneePaiement)
                .statut(StatutEnum.EN_COURS_ARH)
                .dateCreation(LocalDateTime.now())
                .idCreateur(utilisateurCourant.getId())
                .build();
        processus = processusMensuelRepository.save(processus);

        List<BeneficiaireDotationDto> beneficiairesActifs = beneficiaireApi.listerActifsPourDotation();
        List<BeneficiaireExcluDto> beneficiairesExclus = new ArrayList<>();
        int nombreBeneficiairesInclus = 0;

        for (BeneficiaireDotationDto beneficiaire : beneficiairesActifs) {
            ResolutionGrilleDto resolution = grilleTarifaireApi.resoudrePourFonction(beneficiaire.fonction());

            String motifExclusion = resolution.motifExclusion();
            boolean inclusDansEtat = motifExclusion == null;
            int montantApplique = resolution.montantFcfa() != null ? resolution.montantFcfa() : 0;

            LigneEtatMensuel ligne = LigneEtatMensuel.builder()
                    .idProcessus(processus.getId())
                    .idBeneficiaire(beneficiaire.id())
                    .montantApplique(montantApplique)
                    .inclusDansEtat(inclusDansEtat)
                    .fonctionRetenue(beneficiaire.fonction())
                    .build();
            ligneEtatMensuelRepository.save(ligne);

            if (inclusDansEtat) {
                nombreBeneficiairesInclus++;
            } else {
                beneficiairesExclus.add(BeneficiaireExcluDto.builder()
                        .matricule(beneficiaire.matricule())
                        .nomPrenoms(beneficiaire.nomPrenoms())
                        .fonction(beneficiaire.fonction())
                        .motif(motifExclusion)
                        .build());
            }
        }

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("moisPaiement", processus.getMoisPaiement());
        apres.put("anneePaiement", processus.getAnneePaiement());
        apres.put("statut", processus.getStatut().name());
        apres.put("nombreBeneficiairesInclus", nombreBeneficiairesInclus);
        apres.put("nombreBeneficiairesExclus", beneficiairesExclus.size());

        auditService.enregistrer(utilisateurCourant.getId(), "DECLENCHEMENT_PROCESSUS", "processus_mensuel",
                processus.getId(), null, apres);

        return ProcessusMensuelResponseDto.builder()
                .id(processus.getId())
                .moisPaiement(processus.getMoisPaiement())
                .anneePaiement(processus.getAnneePaiement())
                .statut(processus.getStatut())
                .dateCreation(processus.getDateCreation())
                .nombreBeneficiaires(nombreBeneficiairesInclus)
                .beneficiairesExclus(beneficiairesExclus)
                .build();
    }

    // GET /processus : vue liste (mois/annee, statut, date de creation), sans
    // les lignes d'etat -- consulterDetail() reste le seul point d'entree pour
    // le detail complet d'un processus.
    public List<ProcessusListItemDto> lister(StatutEnum statut, Integer anneePaiement) {
        List<ProcessusMensuel> processus;
        if (statut != null && anneePaiement != null) {
            processus = processusMensuelRepository
                    .findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(statut, anneePaiement);
        } else if (statut != null) {
            processus = processusMensuelRepository.findByStatutOrderByAnneePaiementDescMoisPaiementDesc(statut);
        } else if (anneePaiement != null) {
            processus = processusMensuelRepository
                    .findByAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(anneePaiement);
        } else {
            processus = processusMensuelRepository.findAllByOrderByAnneePaiementDescMoisPaiementDesc();
        }

        return processus.stream()
                .map(p -> ProcessusListItemDto.builder()
                        .id(p.getId())
                        .moisPaiement(p.getMoisPaiement())
                        .anneePaiement(p.getAnneePaiement())
                        .statut(p.getStatut())
                        .dateCreation(p.getDateCreation())
                        .build())
                .toList();
    }

    // Vue complete (lignes incluses ET exclues) contrairement a declencher()
    // qui ne compte que les lignes incluses dans nombreBeneficiaires -- ici
    // l'utilisateur doit comprendre pourquoi un beneficiaire a ete exclu.
    public ProcessusDetailResponseDto consulterDetail(Long idProcessus) {
        ProcessusMensuel processus = processusMensuelRepository.findById(idProcessus)
                .orElseThrow(() -> new ProcessusMensuelIntrouvableException("Aucun processus mensuel avec l'id " + idProcessus));

        List<LigneEtatMensuel> lignes = ligneEtatMensuelRepository.findByIdProcessus(idProcessus);

        List<Long> idsBeneficiaires = lignes.stream().map(LigneEtatMensuel::getIdBeneficiaire).toList();
        Map<Long, BeneficiaireIdentiteDto> beneficiairesParId = beneficiaireApi.identitesParId(idsBeneficiaires);

        List<LigneEtatMensuelDetailDto> lignesDetail = lignes.stream()
                .map(ligne -> {
                    BeneficiaireIdentiteDto beneficiaire = beneficiairesParId.get(ligne.getIdBeneficiaire());
                    return LigneEtatMensuelDetailDto.builder()
                            .idBeneficiaire(ligne.getIdBeneficiaire())
                            .matricule(beneficiaire != null ? beneficiaire.matricule() : null)
                            .nomPrenoms(beneficiaire != null ? beneficiaire.nomPrenoms() : null)
                            .fonctionRetenue(ligne.getFonctionRetenue())
                            .montantApplique(ligne.getMontantApplique())
                            .inclusDansEtat(ligne.getInclusDansEtat())
                            .build();
                })
                .toList();

        // RETOURNE n'est pas terminal (un processus peut etre retourne plusieurs
        // fois au fil de ses resoumissions) : on prend la DERNIERE etape RETOURNEE
        // par dateAction, jamais la premiere trouvee, pour ne pas afficher un
        // motif obsolete d'un cycle de retour anterieur.
        EtapeWorkflow derniereEtapeRetournee = etapeWorkflowRepository
                .findFirstByIdProcessusAndStatutEtapeOrderByDateActionDesc(idProcessus, StatutEtapeEnum.RETOURNEE)
                .orElse(null);

        return ProcessusDetailResponseDto.builder()
                .id(processus.getId())
                .moisPaiement(processus.getMoisPaiement())
                .anneePaiement(processus.getAnneePaiement())
                .statut(processus.getStatut())
                .lignesEtatMensuel(lignesDetail)
                .motifRetour(derniereEtapeRetournee != null ? derniereEtapeRetournee.getMotifRetour() : null)
                .origineRetour(derniereEtapeRetournee != null
                        ? (derniereEtapeRetournee.getNomEtape() == NomEtapeEnum.VALIDATION_CRH ? "CRH" : "DRH")
                        : null)
                .build();
    }

    public PieceJointeMetadonneesResponseDto obtenirMetadonneesPieceJointe(Long idProcessus) {
        processusMensuelRepository.findById(idProcessus)
                .orElseThrow(() -> new ProcessusMensuelIntrouvableException("Aucun processus mensuel avec l'id " + idProcessus));

        PieceJointe pieceJointe = pieceJointeRepository.findByIdProcessus(idProcessus)
                .orElseThrow(() -> new PieceJointeIntrouvableException("Aucune piece jointe pour le processus " + idProcessus));

        return PieceJointeMetadonneesResponseDto.builder()
                .id(pieceJointe.getId())
                .nomFichier(pieceJointe.getNomFichier())
                .dateGenerationInitiale(pieceJointe.getDateGenerationInitiale())
                .dateDerniereMiseAJour(pieceJointe.getDateDerniereMiseAJour())
                .nombreSignatures(pieceJointe.getNombreSignatures())
                .build();
    }

    // Le fichier physique peut manquer meme si l'enregistrement PieceJointe
    // existe (ex. volume de stockage non monte, fichier deplace manuellement) --
    // ce cas limite doit renvoyer un 404 explicite plutot qu'une IOException
    // non geree qui remonterait en 500.
    public PieceJointe obtenirPieceJointePourTelechargement(Long idPieceJointe) {
        PieceJointe pieceJointe = pieceJointeRepository.findById(idPieceJointe)
                .orElseThrow(() -> new PieceJointeIntrouvableException("Aucune piece jointe avec l'id " + idPieceJointe));

        if (!Files.exists(Path.of(pieceJointe.getCheminStockage()))) {
            throw new PieceJointeIntrouvableException(
                    "Le fichier de la piece jointe " + idPieceJointe + " est introuvable au chemin enregistre");
        }

        return pieceJointe;
    }

    @Transactional
    public PatchProcessusResponseDto ajuster(Long idProcessus, PatchProcessusRequestDto requeteAjustement) {
        ProcessusMensuel processus = processusMensuelRepository.findById(idProcessus)
                .orElseThrow(() -> new ProcessusMensuelIntrouvableException("Aucun processus mensuel avec l'id " + idProcessus));
                

        // RETOURNE accepte au meme titre que EN_COURS_ARH (Sprint 5.4) : un
        // processus retourne par le CRH ou la DRH doit rester ajustable par
        // l'ARH avant une nouvelle soumission, exactement comme lors de sa
        // premiere constitution.
        if (processus.getStatut() != StatutEnum.EN_COURS_ARH && processus.getStatut() != StatutEnum.RETOURNE) {
            throw new ProcessusMensuelNonModifiableException(
                    "Le processus n'est plus modifiable (statut actuel : " + processus.getStatut() + ")");
        }

        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();
        List<ResultatAjustementDto> resultats = new ArrayList<>();

        for (AjustementLigneEtatDto ajustement : requeteAjustement.getAjustements()) {
            resultats.add(appliquerAjustement(idProcessus, ajustement, utilisateurCourant));
        }

        return PatchProcessusResponseDto.builder()
                .id(idProcessus)
                .resultats(resultats)
                .build();
    }

    // Un seul endpoint (POST /processus/{id}/valider) pour les trois roles,
    // differencie par le statut courant du processus. Sprint 3.4 : branche ARH.
    // Sprint 5.2 : branche CRH. Sprint 5.3 : branche DRH (cloture).
    @Transactional
    public ValiderProcessusResponseDto valider(Long idProcessus, String commentaire) {
        ProcessusMensuel processus = processusMensuelRepository.findById(idProcessus)
                .orElseThrow(() -> new ProcessusMensuelIntrouvableException("Aucun processus mensuel avec l'id " + idProcessus));

        // RETOURNE accepte au meme titre que EN_COURS_ARH (Sprint 5.4) : apres
        // correction, l'ARH revalide par le meme circuit qu'une premiere
        // validation. validerBrancheArh() rappelle documentService.genererInitiale(),
        // qui remplace desormais la PieceJointe existante en place (RG-06) plutot
        // que d'en creer une seconde -- voir DocumentService.
        if (processus.getStatut() == StatutEnum.EN_COURS_ARH || processus.getStatut() == StatutEnum.RETOURNE) {
            return validerBrancheArh(processus, commentaire);
        }
        if (processus.getStatut() == StatutEnum.EN_ATTENTE_CRH) {
            return validerBrancheCrh(processus, commentaire);
        }
        if (processus.getStatut() == StatutEnum.EN_ATTENTE_DRH) {
            return validerBrancheDrh(processus, commentaire);
        }

        throw new ProcessusMensuelNonModifiableException(
                "Le processus n'est plus modifiable (statut actuel : " + processus.getStatut() + ")");
    }

    // RG-05 : seul un ARH peut declencher cette branche. Le controle est
    // indispensable ici car la branche est choisie sur le seul statut du
    // processus, tandis que le @PreAuthorize du controleur laisse passer
    // ARH, CRH et DRH -- sans lui, un CRH ou un DRH validerait l'etape ARH.
    // RG-08/verifier() n'est en revanche pas applicable : il compare l'acteur
    // courant au validateur de l'etape PRECEDENTE, or il n'y a pas d'etape
    // precedente pour une premiere validation ARH.
    private ValiderProcessusResponseDto validerBrancheArh(ProcessusMensuel processus, String commentaire) {
        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();

        separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_ARH, utilisateurCourant);

        List<LigneEtatMensuel> lignesIncluses = ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(processus.getId());
        Map<Long, LigneDocumentDto> donneesDocumentParBeneficiaire = assemblerDonneesDocument(lignesIncluses);

        PieceJointe pieceJointe = documentService.genererInitiale(processus, lignesIncluses, donneesDocumentParBeneficiaire, utilisateurCourant);

        EtapeWorkflow etape = EtapeWorkflow.builder()
                .idProcessus(processus.getId())
                .idActeur(utilisateurCourant.getId())
                .ordreEtape(1)
                .nomEtape(NomEtapeEnum.VALIDATION_ARH)
                .statutEtape(StatutEtapeEnum.VALIDEE)
                .dateAction(LocalDateTime.now())
                .signatureNumerique(signatureService.signer(utilisateurCourant))
                .build();
        etapeWorkflowRepository.save(etape);

        StatutEnum statutAvant = processus.getStatut();
        processus.setStatut(StatutEnum.EN_ATTENTE_CRH);
        processusMensuelRepository.save(processus);

        for (DestinataireNotificationDto destinataireCrh : utilisateurApi.destinatairesParRole(RoleEnum.CRH)) {
            notificationService.notifier(destinataireCrh, "Etat mensuel a valider",
                    "L'etat mensuel " + processus.getMoisPaiement() + "/" + processus.getAnneePaiement()
                            + " a ete valide par l'ARH et attend votre validation.");
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("statut", statutAvant.name());

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("statut", processus.getStatut().name());
        apres.put("etapeValidee", NomEtapeEnum.VALIDATION_ARH.name());
        apres.put("idPieceJointe", pieceJointe.getId());
        apres.put("commentaire", commentaire);

        auditService.enregistrer(utilisateurCourant.getId(), "VALIDATION_PROCESSUS_ARH", "processus_mensuel",
                processus.getId(), avant, apres);

        return ValiderProcessusResponseDto.builder()
                .id(processus.getId())
                .statut(processus.getStatut())
                .etapeValidee(NomEtapeEnum.VALIDATION_ARH.name())
                .idPieceJointe(pieceJointe.getId())
                .build();
    }

    // Couplage C4 (Sprint MM.3) : DocumentService ne va plus chercher Beneficiaire
    // ni FonctionEligible lui-meme -- c'est l'appelant (ici) qui assemble le DTO
    // via les API des modules deja creees en MM.2/MM.3. libelleFonction se replie
    // sur le code brut si le referentiel ne connait plus fonctionRetenue, exactement
    // le comportement precedent de DocumentService.ajouterTableau().
    private Map<Long, LigneDocumentDto> assemblerDonneesDocument(List<LigneEtatMensuel> lignes) {
        List<Long> idsBeneficiaires = lignes.stream().map(LigneEtatMensuel::getIdBeneficiaire).toList();
        Map<Long, BeneficiaireDocumentDto> beneficiairesParId = beneficiaireApi.donneesDocumentParId(idsBeneficiaires);

        Map<Long, LigneDocumentDto> donneesParBeneficiaire = new LinkedHashMap<>();
        for (LigneEtatMensuel ligne : lignes) {
            BeneficiaireDocumentDto beneficiaire = beneficiairesParId.get(ligne.getIdBeneficiaire());
            String libelleFonction = fonctionEligibleService.libelle(ligne.getFonctionRetenue())
                    .orElse(ligne.getFonctionRetenue());

            donneesParBeneficiaire.put(ligne.getIdBeneficiaire(), LigneDocumentDto.builder()
                    .nomPrenoms(beneficiaire != null ? beneficiaire.nomPrenoms() : null)
                    .codeUnite(beneficiaire != null ? beneficiaire.codeUnite() : null)
                    .numCompteCourant(beneficiaire != null ? beneficiaire.numCompteCourant() : null)
                    .chapitre(beneficiaire != null ? beneficiaire.chapitre() : null)
                    .libelleFonction(libelleFonction)
                    .build());
        }
        return donneesParBeneficiaire;
    }

    // RG-05 : seul un CRH peut declencher cette branche (voir validerBrancheArh).
    // RG-08 : l'acteur CRH ne peut pas etre celui qui a valide l'etape ARH
    // precedente -> SeparationTachesViolationException (403).
    // RG-06 : le PDF existant est enrichi (DocumentService.ajouterSignature),
    // jamais recree. Le champ signatureNumerique de l'etape reste null : la
    // signature reelle est deja embarquee dans le PDF par ajouterSignature(),
    // qui appelle SignatureService en interne (voir DocumentService).
    private ValiderProcessusResponseDto validerBrancheCrh(ProcessusMensuel processus, String commentaire) {
        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();

        separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_CRH, utilisateurCourant);
        separationTachesService.verifier(processus.getId(), utilisateurCourant.getId(), NomEtapeEnum.VALIDATION_ARH);

        PieceJointe pieceJointe = pieceJointeRepository.findByIdProcessus(processus.getId())
                .orElseThrow(() -> new PieceJointeIntrouvableException(
                        "Aucune piece jointe trouvee pour le processus " + processus.getId()));

        documentService.ajouterSignature(pieceJointe, utilisateurCourant, NomEtapeEnum.VALIDATION_CRH);

        EtapeWorkflow etape = EtapeWorkflow.builder()
                .idProcessus(processus.getId())
                .idActeur(utilisateurCourant.getId())
                .ordreEtape(2)
                .nomEtape(NomEtapeEnum.VALIDATION_CRH)
                .statutEtape(StatutEtapeEnum.VALIDEE)
                .dateAction(LocalDateTime.now())
                .build();
        etapeWorkflowRepository.save(etape);

        StatutEnum statutAvant = processus.getStatut();
        processus.setStatut(StatutEnum.EN_ATTENTE_DRH);
        processusMensuelRepository.save(processus);

        for (DestinataireNotificationDto destinataireDrh : utilisateurApi.destinatairesParRole(RoleEnum.DRH)) {
            notificationService.notifier(destinataireDrh, "Etat mensuel a valider",
                    "L'etat mensuel " + processus.getMoisPaiement() + "/" + processus.getAnneePaiement()
                            + " a ete valide par le CRH et attend votre validation.");
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("statut", statutAvant.name());

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("statut", processus.getStatut().name());
        apres.put("etapeValidee", NomEtapeEnum.VALIDATION_CRH.name());
        apres.put("idPieceJointe", pieceJointe.getId());
        apres.put("commentaire", commentaire);

        auditService.enregistrer(utilisateurCourant.getId(), "VALIDATION_PROCESSUS_CRH", "processus_mensuel",
                processus.getId(), avant, apres);

        return ValiderProcessusResponseDto.builder()
                .id(processus.getId())
                .statut(processus.getStatut())
                .etapeValidee(NomEtapeEnum.VALIDATION_CRH.name())
                .idPieceJointe(pieceJointe.getId())
                .build();
    }

    // RG-05 : seul un DRH peut declencher cette branche (voir validerBrancheArh).
    // RG-08 : l'acteur DRH ne peut pas etre celui qui a valide l'etape CRH
    // precedente -> SeparationTachesViolationException (403).
    // RG-06 : le PDF existant est enrichi une derniere fois (nombre_signatures
    // passe a 3), jamais recree.
    // Deux entrees d'audit distinctes plutot qu'une seule : la validation de
    // l'etape DRH (meme forme que VALIDATION_PROCESSUS_ARH/CRH, coherence avec
    // les deux autres branches) et la cloture du processus (evenement terminal
    // a part entiere, qui declenche en plus la publication Kafka -- utile pour
    // tracer separement "le DRH a valide" de "le processus est desormais clos
    // et l'evenement comptable a ete emis").
    // Pas de NotificationService ici : contrairement aux branches ARH/CRH qui
    // notifient le role suivant dans le circuit DOTTEL, il n'y a personne a
    // notifier en interne apres la cloture -- l'evenement Kafka tient ce role
    // vis-a-vis du module comptable.
    private ValiderProcessusResponseDto validerBrancheDrh(ProcessusMensuel processus, String commentaire) {
        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();

        separationTachesService.verifierRoleAttendu(NomEtapeEnum.VALIDATION_DRH, utilisateurCourant);
        separationTachesService.verifier(processus.getId(), utilisateurCourant.getId(), NomEtapeEnum.VALIDATION_CRH);

        PieceJointe pieceJointe = pieceJointeRepository.findByIdProcessus(processus.getId())
                .orElseThrow(() -> new PieceJointeIntrouvableException(
                        "Aucune piece jointe trouvee pour le processus " + processus.getId()));

        documentService.ajouterSignature(pieceJointe, utilisateurCourant, NomEtapeEnum.VALIDATION_DRH);

        EtapeWorkflow etape = EtapeWorkflow.builder()
                .idProcessus(processus.getId())
                .idActeur(utilisateurCourant.getId())
                .ordreEtape(3)
                .nomEtape(NomEtapeEnum.VALIDATION_DRH)
                .statutEtape(StatutEtapeEnum.VALIDEE)
                .dateAction(LocalDateTime.now())
                .build();
        etapeWorkflowRepository.save(etape);

        StatutEnum statutAvant = processus.getStatut();
        processus.setStatut(StatutEnum.CLOTURE);
        processus.setDateCloture(LocalDateTime.now());
        processusMensuelRepository.save(processus);

        long montantTotal = ligneEtatMensuelRepository.sumMontantAppliqueByIdProcessus(processus.getId());
        evenementClotureService.publier(processus, montantTotal);

        Map<String, Object> avantValidation = new LinkedHashMap<>();
        avantValidation.put("statut", statutAvant.name());

        Map<String, Object> apresValidation = new LinkedHashMap<>();
        apresValidation.put("statut", processus.getStatut().name());
        apresValidation.put("etapeValidee", NomEtapeEnum.VALIDATION_DRH.name());
        apresValidation.put("idPieceJointe", pieceJointe.getId());
        apresValidation.put("commentaire", commentaire);

        auditService.enregistrer(utilisateurCourant.getId(), "VALIDATION_PROCESSUS_DRH", "processus_mensuel",
                processus.getId(), avantValidation, apresValidation);

        Map<String, Object> apresCloture = new LinkedHashMap<>();
        apresCloture.put("statut", StatutEnum.CLOTURE.name());
        apresCloture.put("dateCloture", processus.getDateCloture().toString());
        apresCloture.put("montantTotal", montantTotal);

        auditService.enregistrer(utilisateurCourant.getId(), "CLOTURE_PROCESSUS", "processus_mensuel",
                processus.getId(), null, apresCloture);

        return ValiderProcessusResponseDto.builder()
                .id(processus.getId())
                .statut(processus.getStatut())
                .etapeValidee(NomEtapeEnum.VALIDATION_DRH.name())
                .idPieceJointe(pieceJointe.getId())
                .build();
    }

    // US-14 (Sprint 5.4) : le CRH ou la DRH retourne le processus a l'ARH avec
    // un motif obligatoire (RG-07, meme principe que l'exclusion de
    // beneficiaire au Sprint 3.2). Pas de SeparationTachesService ici : cette
    // verification compare l'acteur courant au validateur de l'etape
    // PRECEDENTE lors d'une VALIDATION, ce qui ne s'applique pas a un retour.
    // Decision produit (Sprint 5.4) : RETOURNE n'est pas terminal. L'ARH
    // reprend la main via PATCH /processus/{id} (deja etendu ci-dessus) puis
    // revalide via POST /processus/{id}/valider (branche ARH, elle aussi
    // etendue ci-dessus).
    @Transactional
    public RetournerProcessusResponseDto retourner(Long idProcessus, RetournerProcessusRequestDto requeteRetour) {
        ProcessusMensuel processus = processusMensuelRepository.findById(idProcessus)
                .orElseThrow(() -> new ProcessusMensuelIntrouvableException("Aucun processus mensuel avec l'id " + idProcessus));

        if (processus.getStatut() != StatutEnum.EN_ATTENTE_CRH && processus.getStatut() != StatutEnum.EN_ATTENTE_DRH) {
            throw new ProcessusMensuelNonModifiableException(
                    "Un retour n'est possible que depuis EN_ATTENTE_CRH ou EN_ATTENTE_DRH (statut actuel : "
                            + processus.getStatut() + ")");
        }

        String motif = requeteRetour.getMotif();
        if (motif == null || motif.isBlank()) {
            throw new MotifRejetObligatoireException("Le motif de retour est obligatoire");
        }

        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();

        boolean etapeCrh = processus.getStatut() == StatutEnum.EN_ATTENTE_CRH;
        NomEtapeEnum nomEtapeRetournee = etapeCrh ? NomEtapeEnum.VALIDATION_CRH : NomEtapeEnum.VALIDATION_DRH;
        int ordreEtape = etapeCrh ? 2 : 3;

        EtapeWorkflow etape = EtapeWorkflow.builder()
                .idProcessus(processus.getId())
                .idActeur(utilisateurCourant.getId())
                .ordreEtape(ordreEtape)
                .nomEtape(nomEtapeRetournee)
                .statutEtape(StatutEtapeEnum.RETOURNEE)
                .dateAction(LocalDateTime.now())
                .motifRetour(motif)
                .build();
        etapeWorkflowRepository.save(etape);

        StatutEnum statutAvant = processus.getStatut();
        processus.setStatut(StatutEnum.RETOURNE);
        processusMensuelRepository.save(processus);

        // UtilisateurApi.destinataireParId() leve UtilisateurIntrouvableException
        // si l'ARH createur n'existe plus -- meme exception, meme 404 qu'avant.
        DestinataireNotificationDto arhCreateur = utilisateurApi.destinataireParId(processus.getIdCreateur());
        notificationService.notifier(arhCreateur, "Etat mensuel retourne pour correction",
                "L'etat mensuel " + processus.getMoisPaiement() + "/" + processus.getAnneePaiement()
                        + " vous a ete retourne. Motif : " + motif);

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("statut", statutAvant.name());

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("statut", processus.getStatut().name());
        apres.put("motif", motif);

        auditService.enregistrer(utilisateurCourant.getId(), "RETOUR_PROCESSUS", "processus_mensuel",
                processus.getId(), avant, apres);

        return RetournerProcessusResponseDto.builder()
                .id(processus.getId())
                .statut(processus.getStatut())
                .motif(motif)
                .build();
    }

    private ResultatAjustementDto appliquerAjustement(Long idProcessus, AjustementLigneEtatDto ajustement,
                                                        Utilisateur utilisateurCourant) {
        Long idBeneficiaire = ajustement.getIdBeneficiaire();
        Optional<LigneEtatMensuel> ligneExistante = ligneEtatMensuelRepository
                .findByIdProcessusAndIdBeneficiaire(idProcessus, idBeneficiaire);

        if (ligneExistante.isEmpty()) {
            return rejeter(idBeneficiaire, "Aucune ligne d'état mensuel pour ce bénéficiaire dans ce processus");
        }

        LigneEtatMensuel ligne = ligneExistante.get();
        Map<String, Object> avant = new LinkedHashMap<>();
        Map<String, Object> apres = new LinkedHashMap<>();

        // Deux operations exigent une revalidation complete de l'eligibilite :
        // changer la fonction retenue, et reintegrer une ligne exclue. Exclure
        // (true -> false) n'est jamais revalide : retirer quelqu'un de l'etat
        // est toujours sur.
        boolean fonctionModifiee = ajustement.getFonctionRetenue() != null
                && !ajustement.getFonctionRetenue().equals(ligne.getFonctionRetenue());
        boolean reintegration = Boolean.TRUE.equals(ajustement.getInclusDansEtat())
                && !Boolean.TRUE.equals(ligne.getInclusDansEtat());
        String fonctionCible = fonctionModifiee ? ajustement.getFonctionRetenue() : ligne.getFonctionRetenue();

        if (fonctionModifiee || reintegration) {
            ResolutionGrilleDto resolution = grilleTarifaireApi.resoudrePourFonction(fonctionCible);
            if (resolution.motifExclusion() != null) {
                return rejeter(idBeneficiaire, resolution.motifExclusion());
            }

            // RG-02. Le grade vient exclusivement de la base (comme
            // BeneficiaireService.modifier()) : AjustementLigneEtatDto n'en porte
            // pas, donc aucun contournement possible par la requete. Un
            // beneficiaire introuvable donne grade = null, traite comme NON GRADE
            // par EligibiliteService -- fail-closed sur les corps de controle,
            // neutre ailleurs. On ne leve pas d'exception ici : cela ferait
            // echouer les autres lignes valides du meme lot (rapport partiel).
            // RG-01 etant deja acquise ci-dessus (fonction connue et active), un
            // retour false ne peut plus venir que du grade -- d'ou un motif
            // precis sans dupliquer la liste des corps de controle, qui reste
            // portee par le seul EligibiliteService.
            String grade = beneficiaireApi.gradeDe(idBeneficiaire).orElse(null);
            if (!eligibiliteService.verifierEligibilite(fonctionCible, grade)) {
                return rejeter(idBeneficiaire, MOTIF_GRADE_NON_ELIGIBLE);
            }

            int montantCible = resolution.montantFcfa();

            if (fonctionModifiee) {
                avant.put("fonctionRetenue", ligne.getFonctionRetenue());
                avant.put("montantApplique", ligne.getMontantApplique());
                ligne.setFonctionRetenue(fonctionCible);
                ligne.setMontantApplique(montantCible);
                apres.put("fonctionRetenue", ligne.getFonctionRetenue());
                apres.put("montantApplique", ligne.getMontantApplique());
            } else if (!Objects.equals(ligne.getMontantApplique(), montantCible)) {
                // Reintegration seule : une ligne exclue au declenchement porte un
                // montant 0. La reintegrer sans resynchroniser la ferait entrer
                // dans l'etat mensuel a 0 FCFA (sous-paiement silencieux).
                avant.put("montantApplique", ligne.getMontantApplique());
                ligne.setMontantApplique(montantCible);
                apres.put("montantApplique", ligne.getMontantApplique());
            }
        }

        if (ajustement.getInclusDansEtat() != null && !ajustement.getInclusDansEtat().equals(ligne.getInclusDansEtat())) {
            avant.put("inclusDansEtat", ligne.getInclusDansEtat());
            ligne.setInclusDansEtat(ajustement.getInclusDansEtat());
            apres.put("inclusDansEtat", ligne.getInclusDansEtat());
        }

        if (!apres.isEmpty()) {
            ligneEtatMensuelRepository.save(ligne);
            auditService.enregistrer(utilisateurCourant.getId(), "AJUSTEMENT_LIGNE_ETAT_MENSUEL",
                    "ligne_etat_mensuel", ligne.getId(), avant, apres);
        }

        return ResultatAjustementDto.builder()
                .idBeneficiaire(idBeneficiaire)
                .applique(true)
                .build();
    }

    private ResultatAjustementDto rejeter(Long idBeneficiaire, String motif) {
        return ResultatAjustementDto.builder()
                .idBeneficiaire(idBeneficiaire)
                .applique(false)
                .motifRejet(motif)
                .build();
    }

}