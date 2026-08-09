package com.afriland.dottel.referentiel.service;
import com.afriland.dottel.audit.api.EvenementAudit;
import com.afriland.dottel.notifications.api.EvenementNotification;
import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.api.UtilisateurApi;

import com.afriland.dottel.referentiel.exception.DateDebutGrilleAnterieureException;
import com.afriland.dottel.referentiel.exception.DecisionGrilleInvalideException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleEnAttenteDrhExistanteException;
import com.afriland.dottel.referentiel.exception.GrilleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleNonActiveException;
import com.afriland.dottel.referentiel.exception.GrilleNonEnAttenteDrhException;
import com.afriland.dottel.referentiel.exception.GrilleNonModifiableException;
import com.afriland.dottel.referentiel.exception.GrilleTarifaireMotifRejetObligatoireException;
import com.afriland.dottel.referentiel.model.dto.grille.CreerGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.DecisionGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleHistoriqueLigneDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireListeLigneDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.HistoriqueGrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.ModifierGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrilleTarifaireService {

    private final GrilleTarifaireRepository grilleTarifaireRepository;
    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final SeparationTachesGrilleService separationTachesGrilleService;
    private final UtilisateurApi utilisateurApi;
    private final ApplicationEventPublisher eventPublisher;

    // Sprint MM.12 : statuts d'attente du workflow a trois acteurs. Une seule
    // grille "en vol" par fonction, quel que soit l'etage ou elle se trouve.
    private static final Set<StatutGrilleEnum> STATUTS_EN_ATTENTE =
            EnumSet.of(StatutGrilleEnum.EN_ATTENTE_CRH, StatutGrilleEnum.EN_ATTENTE_DRH);

    // Decision actee avec le metier (Sprint 4bis.1) : le contrat API
    // (docs/reference/contrats_api_dotations_v3.md) se contredit sur le statut
    // de creation (BROUILLON vs EN_ATTENTE_DRH) et sur la condition du 409
    // (grille ACTIVE vs EN_ATTENTE_DRH). Les user stories V3 (US-20) sont
    // retenues comme source : creation directe au premier statut d'attente,
    // 409 si une grille est deja en attente pour la fonction (pas si une
    // grille ACTIVE existe, cas normal de mise a jour tarifaire).
    //
    // Sprint MM.12 : ce premier statut devient EN_ATTENTE_CRH (le CRH est
    // insere avant la DRH), et le garde-fou 409 couvre desormais les DEUX
    // statuts d'attente -- sinon on pourrait creer une grille EN_ATTENTE_CRH
    // pour une fonction dont une autre grille attend deja la DRH, et se
    // retrouver avec deux grilles concurrentes pour la meme fonction.
    @Transactional
    public GrilleTarifaireResponseDto creer(CreerGrilleTarifaireRequestDto requete, Long idCreateur) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(requete.getCodeFonction())
                .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                        "Aucune fonction éligible avec le code " + requete.getCodeFonction()));

        if (grilleTarifaireRepository.existsByIdFonctionEligibleAndStatutValidationIn(
                fonctionEligible.getId(), STATUTS_EN_ATTENTE)) {
            throw new GrilleEnAttenteDrhExistanteException(
                    "Une grille est déjà en attente de validation pour la fonction "
                            + requete.getCodeFonction());
        }

        // Ecart trouve manuellement Sprint 6F.7bis : avant l'ajout de la
        // desactivation manuelle, il existait toujours une grille ACTIVE +
        // dateFin IS NULL par fonction, et validerOuRejeter() fermait cette
        // grille automatiquement a la validation, donnant l'illusion d'un
        // ordre chronologique impose. Une fois cette grille desactivee (donc
        // plus de grille ACTIVE+dateFin IS NULL), plus rien n'empechait de
        // creer une nouvelle grille avec une dateDebut anterieure a
        // l'historique existant. Controle explicite ici : la nouvelle
        // dateDebut doit etre strictement posterieure a celle de la grille
        // non rejetee la plus recente pour la fonction.
        grilleTarifaireRepository.findByIdFonctionEligibleOrderByDateDebutDesc(fonctionEligible.getId()).stream()
                .filter(g -> g.getStatutValidation() != StatutGrilleEnum.REJETEE)
                .findFirst()
                .ifPresent(derniereGrille -> {
                    // Borne de comparaison = dateFin si la grille est deja
                    // close (evite tout chevauchement avec sa propre periode
                    // passee), sinon dateDebut (grille encore ouverte, cas
                    // normal de mise a jour tarifaire deja gere par
                    // validerOuRejeter qui la fermera a newDateDebut - 1).
                    var borne = derniereGrille.getDateFin() != null
                            ? derniereGrille.getDateFin() : derniereGrille.getDateDebut();
                    if (!requete.getDateDebut().isAfter(borne)) {
                        throw new DateDebutGrilleAnterieureException(
                                "La date de début doit être postérieure au " + borne
                                        + " pour la fonction " + requete.getCodeFonction());
                    }
                });

        GrilleTarifaire grille = GrilleTarifaire.builder()
                .idFonctionEligible(fonctionEligible.getId())
                .montantFcfa(requete.getMontantFcfa())
                .dateDebut(requete.getDateDebut())
                .statutValidation(StatutGrilleEnum.EN_ATTENTE_CRH)
                .idCreateur(idCreateur)
                .idValidateur(null)
                .dateCreation(LocalDateTime.now())
                .build();

        grilleTarifaireRepository.save(grille);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("codeFonction", requete.getCodeFonction());
        apres.put("montantFcfa", grille.getMontantFcfa());
        apres.put("dateDebut", grille.getDateDebut());
        apres.put("statutValidation", grille.getStatutValidation().name());

        eventPublisher.publishEvent(new EvenementAudit(idCreateur, "CREATION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), null, apres));

        return versDto(grille, requete.getCodeFonction());
    }

    @Transactional
    public GrilleTarifaireResponseDto modifier(Long id, ModifierGrilleTarifaireRequestDto requete, Long idModificateur) {
        GrilleTarifaire grille = grilleTarifaireRepository.findById(id)
                .orElseThrow(() -> new GrilleIntrouvableException("Aucune grille tarifaire avec l'id " + id));

        // Sprint MM.12, arbitrage du 2026-08-09 : la correction du montant par
        // l'ARH s'arrete des que le CRH a statue. Autoriser EN_ATTENTE_DRH
        // permettrait a l'ARH de changer le montant APRES la validation CRH :
        // la DRH validerait alors un chiffre que le CRH n'a jamais vu, ce qui
        // viderait l'etape CRH de son sens.
        if (grille.getStatutValidation() != StatutGrilleEnum.EN_ATTENTE_CRH) {
            throw new GrilleNonModifiableException(
                    "Grille non en statut EN_ATTENTE_CRH, modification impossible");
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("montantFcfa", grille.getMontantFcfa());

        grille.setMontantFcfa(requete.getMontantFcfa());
        grilleTarifaireRepository.save(grille);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("montantFcfa", grille.getMontantFcfa());

        eventPublisher.publishEvent(new EvenementAudit(idModificateur, "MODIFICATION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), avant, apres));

        return versDto(grille, codeFonctionDe(grille));
    }

    // Un seul point d'entree (POST /grilles-tarifaires/{id}/valider) pour le CRH
    // et la DRH, differencie par le statut courant de la grille -- exactement le
    // modele de ProcessusMensuelService.valider(), qui sert deja trois roles
    // (arbitrage du 2026-08-09, Sprint MM.12).
    //
    // Le @PreAuthorize du controleur laisse passer CRH ET DRH : c'est
    // verifierRoleAttendu() en tete de chaque branche qui garantit qu'un DRH ne
    // statue pas sur une grille EN_ATTENTE_CRH, sautant purement et simplement
    // l'etape CRH (meme discipline de garde que les branches du processus).
    @Transactional
    public GrilleTarifaireResponseDto validerOuRejeter(Long id, DecisionGrilleTarifaireRequestDto requete,
                                                         Utilisateur acteur) {
        GrilleTarifaire grille = grilleTarifaireRepository.findById(id)
                .orElseThrow(() -> new GrilleIntrouvableException("Aucune grille tarifaire avec l'id " + id));

        if (grille.getStatutValidation() == StatutGrilleEnum.EN_ATTENTE_CRH) {
            return deciderBrancheCrh(grille, requete, acteur);
        }
        if (grille.getStatutValidation() == StatutGrilleEnum.EN_ATTENTE_DRH) {
            return deciderBrancheDrh(grille, requete, acteur);
        }

        throw new GrilleNonEnAttenteDrhException(
                "Grille non en attente de décision (statut actuel : " + grille.getStatutValidation() + ")");
    }

    // Premiere etape du workflow a trois acteurs (Sprint MM.12).
    // RG-05 : seul un CRH peut declencher cette branche.
    // RG-08 : le CRH ne peut pas etre l'ARH qui a cree la grille (scenario reel
    // protege : creation en ARH puis promotion du compte en CRH via
    // PATCH /admin/utilisateurs/{id}/role).
    // Une validation CRH ne touche NI le statut ACTIVE NI la date_fin de
    // l'ancienne grille : RG-10 ne s'applique qu'a la validation DRH, seule
    // etape qui met une grille en vigueur.
    private GrilleTarifaireResponseDto deciderBrancheCrh(GrilleTarifaire grille,
                                                           DecisionGrilleTarifaireRequestDto requete,
                                                           Utilisateur acteur) {
        separationTachesGrilleService.verifierRoleAttendu(StatutGrilleEnum.EN_ATTENTE_CRH, acteur);
        separationTachesGrilleService.verifier(grille.getIdCreateur(), acteur.getId(), "de création ARH");

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("statutValidation", grille.getStatutValidation().name());

        String decision = requete.getDecision();
        if ("REJETER".equalsIgnoreCase(decision)) {
            exigerMotifRejet(requete);
            grille.setStatutValidation(StatutGrilleEnum.REJETEE);
            grille.setMotifRejet(requete.getMotifRejet());
        } else if ("VALIDER".equalsIgnoreCase(decision)) {
            grille.setStatutValidation(StatutGrilleEnum.EN_ATTENTE_DRH);
        } else {
            throw new DecisionGrilleInvalideException(
                    "Décision invalide, valeurs attendues : VALIDER ou REJETER");
        }

        // Renseignes dans LES DEUX cas, validation comme rejet -- c'est
        // l'invariant sur lequel repose la derivation de origineRejet (voir
        // origineRejet() plus bas et la migration V7).
        grille.setIdDecideurCrh(acteur.getId());
        grille.setDateDecisionCrh(LocalDateTime.now());
        grilleTarifaireRepository.save(grille);

        publierDecisionAudit(grille, acteur, "DECISION_GRILLE_TARIFAIRE_CRH", avant);
        notifierRejetAlArhCreateur(grille, "CRH");

        return versDto(grille, codeFonctionDe(grille));
    }

    // Seconde etape. RG-05 : seul un DRH peut declencher cette branche.
    // RG-08 : le DRH ne peut pas etre le CRH qui a statue juste avant.
    // Decision actee avec le metier le 23/07/2026 : date_fin de l'ancienne grille
    // ACTIVE = dateDebut de la nouvelle grille moins un jour, pour garantir des
    // periodes strictement disjointes (aucune ambiguite sur la recherche RG-04,
    // que les bornes soient traitees comme inclusives ou exclusives).
    private GrilleTarifaireResponseDto deciderBrancheDrh(GrilleTarifaire grille,
                                                           DecisionGrilleTarifaireRequestDto requete,
                                                           Utilisateur acteur) {
        separationTachesGrilleService.verifierRoleAttendu(StatutGrilleEnum.EN_ATTENTE_DRH, acteur);
        separationTachesGrilleService.verifier(grille.getIdDecideurCrh(), acteur.getId(), "de validation CRH");

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("statutValidation", grille.getStatutValidation().name());

        String decision = requete.getDecision();
        if ("REJETER".equalsIgnoreCase(decision)) {
            exigerMotifRejet(requete);
            grille.setStatutValidation(StatutGrilleEnum.REJETEE);
            grille.setMotifRejet(requete.getMotifRejet());
        } else if ("VALIDER".equalsIgnoreCase(decision)) {
            // RG-10 : saveAndFlush + flush explicite : l'index partiel unique
            // (ACTIVE + date_fin NULL) est verifie a chaque instruction SQL, pas
            // seulement au commit. Sans flush immediat de la fermeture, Hibernate
            // peut ordonner les updates selon l'ordre de chargement en memoire et
            // activer la nouvelle grille avant que l'ancienne soit fermee,
            // violant la contrainte.
            grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                            grille.getIdFonctionEligible(), StatutGrilleEnum.ACTIVE)
                    .ifPresent(ancienneGrille -> {
                        ancienneGrille.setDateFin(grille.getDateDebut().minusDays(1));
                        grilleTarifaireRepository.saveAndFlush(ancienneGrille);
                    });

            grille.setStatutValidation(StatutGrilleEnum.ACTIVE);
        } else {
            throw new DecisionGrilleInvalideException(
                    "Décision invalide, valeurs attendues : VALIDER ou REJETER");
        }

        grille.setIdValidateur(acteur.getId());
        grille.setDateValidation(LocalDateTime.now());
        grilleTarifaireRepository.save(grille);

        publierDecisionAudit(grille, acteur, "DECISION_GRILLE_TARIFAIRE_DRH", avant);
        notifierRejetAlArhCreateur(grille, "DRH");

        return versDto(grille, codeFonctionDe(grille));
    }

    /**
     * Notifie l'ARH createur qu'une grille qu'il a soumise vient d'etre
     * rejetee (Sprint MM.13, perimetre arbitre avec l'utilisateur).
     *
     * <p>Seul le REJET est notifie : c'est le seul vrai trou metier. REJETEE
     * est TERMINAL pour une grille -- sans notification, la revision tarifaire
     * reste bloquee sans que personne ne le sache. La soumission ARH et la
     * validation CRH ne comblent aucun trou : le CRH et la DRH disposent deja
     * d'un ecran dedie qu'ils consultent.</p>
     *
     * <p>id_createur est NULLABLE (CLAUDE.md section 4) : les grilles initiales
     * inserees par Flyway V2 n'ont pas de createur reel. Personne a notifier
     * dans ce cas -- et aucune raison de faire echouer le rejet pour autant.</p>
     */
    private void notifierRejetAlArhCreateur(GrilleTarifaire grille, String origineRejet) {
        if (grille.getStatutValidation() != StatutGrilleEnum.REJETEE || grille.getIdCreateur() == null) {
            return;
        }

        FonctionEligible fonctionEligible = fonctionEligibleRepository.findById(grille.getIdFonctionEligible())
                .orElse(null);
        String codeFonction = fonctionEligible != null ? fonctionEligible.getCode() : "inconnue";
        String libelleFonction = fonctionEligible != null ? fonctionEligible.getLibelle() : "inconnue";

        DestinataireNotificationDto arhCreateur = utilisateurApi.destinataireParId(grille.getIdCreateur());
        eventPublisher.publishEvent(new EvenementNotification(arhCreateur, "Grille tarifaire rejetee",
                "La grille tarifaire que vous avez soumise pour la fonction " + codeFonction
                        + " (" + libelleFonction + "), d'un montant de " + grille.getMontantFcfa()
                        + " FCFA, a ete rejetee par le " + origineRejet
                        + ". Motif : " + grille.getMotifRejet()));
    }

    // RG-07 : tout rejet exige un motif textuel non vide, quel que soit l'acteur.
    private void exigerMotifRejet(DecisionGrilleTarifaireRequestDto requete) {
        if (requete.getMotifRejet() == null || requete.getMotifRejet().isBlank()) {
            throw new GrilleTarifaireMotifRejetObligatoireException("Le motif de rejet est obligatoire");
        }
    }

    // RG-09 : delta avant/apres a chaque transition. Actions distinctes par
    // etage (CRH/DRH) sur le modele de VALIDATION_PROCESSUS_ARH/CRH/DRH -- le
    // filtre du journal d'audit derive sa liste d'actions de la base depuis
    // MM.11, ces deux nouvelles valeurs y apparaissent donc sans configuration.
    private void publierDecisionAudit(GrilleTarifaire grille, Utilisateur acteur, String action,
                                        Map<String, Object> avant) {
        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("statutValidation", grille.getStatutValidation().name());
        apres.put("motifRejet", grille.getMotifRejet());

        eventPublisher.publishEvent(new EvenementAudit(acteur.getId(), action, "grille_tarifaire",
                grille.getId(), avant, apres));
    }

    // Ecart cahier des charges II.1.7 comble au Sprint 6F.7bis : jusqu'ici le
    // cycle de vie ne couvrait que le remplacement d'une grille ACTIVE par une
    // autre (validerOuRejeter). Cette methode couvre le retrait volontaire
    // d'une grille ACTIVE sans remplacement -- aucune nouvelle grille creee,
    // uniquement une fermeture (RG-10).
    @Transactional
    public GrilleTarifaireResponseDto desactiver(Long id, Long idActeur) {
        GrilleTarifaire grille = grilleTarifaireRepository.findById(id)
                .orElseThrow(() -> new GrilleIntrouvableException("Aucune grille tarifaire avec l'id " + id));

        if (grille.getStatutValidation() != StatutGrilleEnum.ACTIVE || grille.getDateFin() != null) {
            throw new GrilleNonActiveException(
                    "Grille non en statut ACTIVE, désactivation impossible");
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("dateFin", grille.getDateFin());

        grille.setDateFin(LocalDateTime.now().toLocalDate());
        grilleTarifaireRepository.save(grille);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("dateFin", grille.getDateFin());

        eventPublisher.publishEvent(new EvenementAudit(idActeur, "DESACTIVATION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), avant, apres));

        return versDto(grille, codeFonctionDe(grille));
    }

    // Pas de pagination : volume interne limite (25 fonctions eligibles,
    // quelques grilles par fonction au fil du temps), meme decision que
    // pour GET /admin/utilisateurs.
    @Transactional(readOnly = true)
    public List<GrilleTarifaireListeLigneDto> rechercher(String codeFonction, StatutGrilleEnum statut) {
        Long idFonctionEligible = null;
        if (codeFonction != null) {
            idFonctionEligible = fonctionEligibleRepository.findByCode(codeFonction)
                    .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                            "Aucune fonction éligible avec le code " + codeFonction))
                    .getId();
        }

        Map<Long, FonctionEligible> fonctionsParId = fonctionEligibleRepository.findAll().stream()
                .collect(Collectors.toMap(FonctionEligible::getId, Function.identity()));

        Long finalIdFonctionEligible = idFonctionEligible;
        return grilleTarifaireRepository.findAll().stream()
                .filter(grille -> finalIdFonctionEligible == null
                        || grille.getIdFonctionEligible().equals(finalIdFonctionEligible))
                .filter(grille -> statut == null || grille.getStatutValidation() == statut)
                .map(grille -> {
                    FonctionEligible fonctionEligible = fonctionsParId.get(grille.getIdFonctionEligible());
                    return GrilleTarifaireListeLigneDto.builder()
                            .id(grille.getId())
                            .codeFonction(fonctionEligible != null ? fonctionEligible.getCode() : null)
                            .libelleFonction(fonctionEligible != null ? fonctionEligible.getLibelle() : null)
                            .montantFcfa(grille.getMontantFcfa())
                            .dateDebut(grille.getDateDebut())
                            .dateFin(grille.getDateFin())
                            .statutValidation(grille.getStatutValidation())
                            .motifRejet(grille.getMotifRejet())
                            .origineRejet(origineRejet(grille))
                            .build();
                })
                .toList();
    }

    public HistoriqueGrilleTarifaireResponseDto historique(String codeFonction) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(codeFonction)
                .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                        "Aucune fonction éligible avec le code " + codeFonction));

        List<GrilleTarifaire> grilles = grilleTarifaireRepository
                .findByIdFonctionEligibleOrderByDateDebutDesc(fonctionEligible.getId());

        List<GrilleHistoriqueLigneDto> lignes = grilles.stream()
                .map(grille -> GrilleHistoriqueLigneDto.builder()
                        .id(grille.getId())
                        .montantFcfa(grille.getMontantFcfa())
                        .dateDebut(grille.getDateDebut())
                        .dateFin(grille.getDateFin())
                        .statutValidation(grille.getStatutValidation())
                        .motifRejet(grille.getMotifRejet())
                        .origineRejet(origineRejet(grille))
                        .build())
                .toList();

        return HistoriqueGrilleTarifaireResponseDto.builder()
                .codeFonction(fonctionEligible.getCode())
                .libelleFonction(fonctionEligible.getLibelle())
                .grilles(lignes)
                .build();
    }

    private GrilleTarifaireResponseDto versDto(GrilleTarifaire grille, String codeFonction) {
        return GrilleTarifaireResponseDto.builder()
                .id(grille.getId())
                .codeFonction(codeFonction)
                .montantFcfa(grille.getMontantFcfa())
                .dateDebut(grille.getDateDebut())
                .statutValidation(grille.getStatutValidation())
                .build();
    }

    private String codeFonctionDe(GrilleTarifaire grille) {
        return fonctionEligibleRepository.findById(grille.getIdFonctionEligible())
                .map(FonctionEligible::getCode)
                .orElse(null);
    }

    /**
     * Origine d'un rejet -- "CRH" ou "DRH" -- equivalent d'origineRetour pour le
     * processus mensuel (Sprint MM.12).
     *
     * <p>Derivee, non stockee, exactement comme origineRetour l'est a partir du
     * nomEtape de la derniere EtapeWorkflow RETOURNEE. Aucune colonne
     * supplementaire : les deux couples de decision suffisent, puisque chacun
     * est renseigne a la validation COMME au rejet.</p>
     *
     * <p>La DRH est testee EN PREMIER : apres un rejet DRH les deux couples sont
     * renseignes (le CRH avait valide avant), l'ordre inverse attribuerait donc
     * le rejet au CRH.</p>
     *
     * <p>Le "dernier motif saisi" ne demande aucun mecanisme particulier ici,
     * contrairement au processus mensuel : REJETEE est TERMINAL pour une grille
     * (l'ARH en cree une nouvelle plutot que de resoumettre celle-ci), il n'y a
     * donc jamais plus d'un rejet par ligne et motif_rejet EST mecaniquement le
     * dernier.</p>
     */
    private String origineRejet(GrilleTarifaire grille) {
        if (grille.getStatutValidation() != StatutGrilleEnum.REJETEE) {
            return null;
        }
        if (grille.getIdValidateur() != null) {
            return "DRH";
        }
        if (grille.getIdDecideurCrh() != null) {
            return "CRH";
        }
        // Grilles rejetees avant MM.12 : le couple DRH etait deja renseigne au
        // rejet, ce cas ne devrait donc pas se presenter. Repli neutre plutot
        // qu'une origine inventee.
        return null;
    }
}