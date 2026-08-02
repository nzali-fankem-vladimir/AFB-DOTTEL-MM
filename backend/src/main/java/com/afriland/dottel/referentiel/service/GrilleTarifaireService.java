package com.afriland.dottel.referentiel.service;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.referentiel.exception.DateDebutGrilleAnterieureException;
import com.afriland.dottel.referentiel.exception.DecisionGrilleInvalideException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleEnAttenteDrhExistanteException;
import com.afriland.dottel.referentiel.exception.GrilleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleNonActiveException;
import com.afriland.dottel.referentiel.exception.GrilleNonEnAttenteDrhException;
import com.afriland.dottel.referentiel.exception.GrilleNonModifiableException;
import com.afriland.dottel.processus.exception.MotifRejetObligatoireException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrilleTarifaireService {

    private final GrilleTarifaireRepository grilleTarifaireRepository;
    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final AuditService auditService;

    // Decision actee avec le metier (Sprint 4bis.1) : le contrat API
    // (docs/reference/contrats_api_dotations_v3.md) se contredit sur le statut
    // de creation (BROUILLON vs EN_ATTENTE_DRH) et sur la condition du 409
    // (grille ACTIVE vs EN_ATTENTE_DRH). Les user stories V3 (US-20) sont
    // retenues comme source : creation directe en EN_ATTENTE_DRH, 409 si une
    // grille est deja EN_ATTENTE_DRH pour la fonction (pas si une grille
    // ACTIVE existe, cas normal de mise a jour tarifaire).
    @Transactional
    public GrilleTarifaireResponseDto creer(CreerGrilleTarifaireRequestDto requete, Long idCreateur) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(requete.getCodeFonction())
                .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                        "Aucune fonction éligible avec le code " + requete.getCodeFonction()));

        grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                        fonctionEligible.getId(), StatutGrilleEnum.EN_ATTENTE_DRH)
                .ifPresent(grille -> {
                    throw new GrilleEnAttenteDrhExistanteException(
                            "Une grille est déjà en attente de validation DRH pour la fonction "
                                    + requete.getCodeFonction());
                });

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
                .statutValidation(StatutGrilleEnum.EN_ATTENTE_DRH)
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

        auditService.enregistrer(idCreateur, "CREATION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), null, apres);

        return versDto(grille, requete.getCodeFonction());
    }

    @Transactional
    public GrilleTarifaireResponseDto modifier(Long id, ModifierGrilleTarifaireRequestDto requete, Long idModificateur) {
        GrilleTarifaire grille = grilleTarifaireRepository.findById(id)
                .orElseThrow(() -> new GrilleIntrouvableException("Aucune grille tarifaire avec l'id " + id));

        if (grille.getStatutValidation() != StatutGrilleEnum.EN_ATTENTE_DRH) {
            throw new GrilleNonModifiableException(
                    "Grille non en statut EN_ATTENTE_DRH, modification impossible");
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("montantFcfa", grille.getMontantFcfa());

        grille.setMontantFcfa(requete.getMontantFcfa());
        grilleTarifaireRepository.save(grille);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("montantFcfa", grille.getMontantFcfa());

        auditService.enregistrer(idModificateur, "MODIFICATION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), avant, apres);

        String codeFonction = fonctionEligibleRepository.findById(grille.getIdFonctionEligible())
                .map(FonctionEligible::getCode)
                .orElse(null);

        return versDto(grille, codeFonction);
    }

    // Decision actee avec le metier le 23/07/2026 : date_fin de l'ancienne grille
    // ACTIVE = dateDebut de la nouvelle grille moins un jour, pour garantir des
    // periodes strictement disjointes (aucune ambiguite sur la recherche RG-04,
    // que les bornes soient traitees comme inclusives ou exclusives).
    @Transactional
    public GrilleTarifaireResponseDto validerOuRejeter(Long id, DecisionGrilleTarifaireRequestDto requete, Long idValidateur) {
        GrilleTarifaire grille = grilleTarifaireRepository.findById(id)
                .orElseThrow(() -> new GrilleIntrouvableException("Aucune grille tarifaire avec l'id " + id));

        if (grille.getStatutValidation() != StatutGrilleEnum.EN_ATTENTE_DRH) {
            throw new GrilleNonEnAttenteDrhException(
                    "Grille non en statut EN_ATTENTE_DRH, décision impossible");
        }

        String decision = requete.getDecision();
        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("statutValidation", grille.getStatutValidation().name());

        if ("REJETER".equalsIgnoreCase(decision)) {
            if (requete.getMotifRejet() == null || requete.getMotifRejet().isBlank()) {
                throw new MotifRejetObligatoireException("Le motif de rejet est obligatoire");
            }

            grille.setStatutValidation(StatutGrilleEnum.REJETEE);
            grille.setMotifRejet(requete.getMotifRejet());
            grille.setIdValidateur(idValidateur);
            grille.setDateValidation(LocalDateTime.now());
            grilleTarifaireRepository.save(grille);
        } else if ("VALIDER".equalsIgnoreCase(decision)) {
            // saveAndFlush + flush explicite : l'index partiel unique (ACTIVE + date_fin
            // NULL) est verifie a chaque instruction SQL, pas seulement au commit. Sans
            // flush immediat de la fermeture, Hibernate peut ordonner les updates selon
            // l'ordre de chargement en memoire et activer la nouvelle grille avant que
            // l'ancienne soit fermee, violant la contrainte.
            grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                            grille.getIdFonctionEligible(), StatutGrilleEnum.ACTIVE)
                    .ifPresent(ancienneGrille -> {
                        ancienneGrille.setDateFin(grille.getDateDebut().minusDays(1));
                        grilleTarifaireRepository.saveAndFlush(ancienneGrille);
                    });

            grille.setStatutValidation(StatutGrilleEnum.ACTIVE);
            grille.setIdValidateur(idValidateur);
            grille.setDateValidation(LocalDateTime.now());
            grilleTarifaireRepository.save(grille);
        } else {
            throw new DecisionGrilleInvalideException(
                    "Décision invalide, valeurs attendues : VALIDER ou REJETER");
        }

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("statutValidation", grille.getStatutValidation().name());
        apres.put("motifRejet", grille.getMotifRejet());

        auditService.enregistrer(idValidateur, "DECISION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), avant, apres);

        String codeFonction = fonctionEligibleRepository.findById(grille.getIdFonctionEligible())
                .map(FonctionEligible::getCode)
                .orElse(null);

        return versDto(grille, codeFonction);
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

        auditService.enregistrer(idActeur, "DESACTIVATION_GRILLE_TARIFAIRE", "grille_tarifaire",
                grille.getId(), avant, apres);

        String codeFonction = fonctionEligibleRepository.findById(grille.getIdFonctionEligible())
                .map(FonctionEligible::getCode)
                .orElse(null);

        return versDto(grille, codeFonction);
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
}