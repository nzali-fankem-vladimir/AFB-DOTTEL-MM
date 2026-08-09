package com.afriland.dottel.security;

import com.afriland.dottel.utilisateurs.exception.ActionAdminNonAutoriseeException;
import com.afriland.dottel.beneficiaires.exception.BeneficiaireIntrouvableException;
import com.afriland.dottel.referentiel.exception.DecisionGrilleInvalideException;
import com.afriland.dottel.utilisateurs.exception.EmailUtilisateurDejaUtiliseException;
import com.afriland.dottel.beneficiaires.exception.FichierImportInvalideException;
import com.afriland.dottel.referentiel.exception.DateDebutGrilleAnterieureException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleBeneficiairesActifsException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleCodeDejaUtiliseException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleEnAttenteDrhExistanteException;
import com.afriland.dottel.referentiel.exception.GrilleIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleNonActiveException;
import com.afriland.dottel.referentiel.exception.GrilleNonEnAttenteDrhException;
import com.afriland.dottel.referentiel.exception.GrilleNonModifiableException;
import com.afriland.dottel.referentiel.exception.GrilleTarifaireIntrouvableException;
import com.afriland.dottel.referentiel.exception.GrilleTarifaireMotifRejetObligatoireException;
import com.afriland.dottel.referentiel.exception.RoleDecisionGrilleNonAutoriseException;
import com.afriland.dottel.referentiel.exception.SeparationTachesGrilleViolationException;
import com.afriland.dottel.beneficiaires.exception.MatriculeDejaEnroleException;
import com.afriland.dottel.beneficiaires.exception.MatriculeInconnuException;
import com.afriland.dottel.utilisateurs.exception.MatriculeUtilisateurDejaUtiliseException;
import com.afriland.dottel.processus.exception.MotifRejetObligatoireException;
import com.afriland.dottel.processus.exception.PeriodeProcessusFutureException;
import com.afriland.dottel.processus.exception.ProcessusOriginalIntrouvableException;
import com.afriland.dottel.beneficiaires.exception.NonEligibleException;
import com.afriland.dottel.beneficiaires.exception.UniteInconnueException;
import com.afriland.dottel.processus.exception.ProcessusMensuelExisteDejaException;
import com.afriland.dottel.processus.exception.ProcessusMensuelIntrouvableException;
import com.afriland.dottel.processus.exception.ProcessusMensuelNonModifiableException;
import com.afriland.dottel.processus.exception.EtapeWorkflowIntrouvableException;
import com.afriland.dottel.processus.exception.PieceJointeIntrouvableException;
import com.afriland.dottel.processus.exception.ResynchronisationNonConfirmeeException;
import com.afriland.dottel.processus.exception.RoleEtapeNonAutoriseException;
import com.afriland.dottel.processus.exception.ValidationBloqueeGrilleEnAttenteException;
import com.afriland.dottel.utilisateurs.exception.RoleInvalideException;
import com.afriland.dottel.processus.exception.SeparationTachesViolationException;
import com.afriland.dottel.utilisateurs.exception.UtilisateurIntrouvableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> gererValidationInvalide(MethodArgumentNotValidException exception) {
        Map<String, String> erreursParChamp = new LinkedHashMap<>();
        for (FieldError erreur : exception.getBindingResult().getFieldErrors()) {
            erreursParChamp.put(erreur.getField(), erreur.getDefaultMessage());
        }

        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", "Données invalides");
        corps.put("champs", erreursParChamp);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(MatriculeDejaEnroleException.class)
    public ResponseEntity<Map<String, Object>> gererMatriculeDejaEnrole(MatriculeDejaEnroleException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(MatriculeInconnuException.class)
    public ResponseEntity<Map<String, Object>> gererMatriculeInconnu(MatriculeInconnuException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(NonEligibleException.class)
    public ResponseEntity<Map<String, Object>> gererNonEligible(NonEligibleException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.FORBIDDEN.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corps);
    }

    @ExceptionHandler(UniteInconnueException.class)
    public ResponseEntity<Map<String, Object>> gererUniteInconnue(UniteInconnueException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(BeneficiaireIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererBeneficiaireIntrouvable(BeneficiaireIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(GrilleTarifaireIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleTarifaireIntrouvable(GrilleTarifaireIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(FonctionEligibleIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererFonctionEligibleIntrouvable(FonctionEligibleIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(FonctionEligibleCodeDejaUtiliseException.class)
    public ResponseEntity<Map<String, Object>> gererFonctionEligibleCodeDejaUtilise(
            FonctionEligibleCodeDejaUtiliseException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(DateDebutGrilleAnterieureException.class)
    public ResponseEntity<Map<String, Object>> gererDateDebutGrilleAnterieure(
            DateDebutGrilleAnterieureException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(FonctionEligibleBeneficiairesActifsException.class)
    public ResponseEntity<Map<String, Object>> gererFonctionEligibleBeneficiairesActifs(
            FonctionEligibleBeneficiairesActifsException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(GrilleEnAttenteDrhExistanteException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleEnAttenteDrhExistante(GrilleEnAttenteDrhExistanteException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(GrilleIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleIntrouvable(GrilleIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(GrilleNonModifiableException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleNonModifiable(GrilleNonModifiableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(GrilleNonEnAttenteDrhException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleNonEnAttenteDrh(GrilleNonEnAttenteDrhException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(GrilleNonActiveException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleNonActive(GrilleNonActiveException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(DecisionGrilleInvalideException.class)
    public ResponseEntity<Map<String, Object>> gererDecisionGrilleInvalide(DecisionGrilleInvalideException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(MotifRejetObligatoireException.class)
    public ResponseEntity<Map<String, Object>> gererMotifRejetObligatoire(MotifRejetObligatoireException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    // Sprint MM.5 : exception propre a referentiel, distincte de
    // MotifRejetObligatoireException (processus) -- reutiliser cette derniere
    // depuis GrilleTarifaireService.validerOuRejeter() creait un cycle de
    // modules (processus <-> referentiel) detecte par ModularityTests. Meme
    // contrat HTTP (400, meme forme de corps) : aucun changement pour l'appelant.
    @ExceptionHandler(GrilleTarifaireMotifRejetObligatoireException.class)
    public ResponseEntity<Map<String, Object>> gererGrilleTarifaireMotifRejetObligatoire(
            GrilleTarifaireMotifRejetObligatoireException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(ProcessusMensuelExisteDejaException.class)
    public ResponseEntity<Map<String, Object>> gererProcessusMensuelExisteDeja(ProcessusMensuelExisteDejaException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    // Sprint MM.11 : periode future demandee au declenchement. 400 et non 409
    // -- le 409 est reserve aux conflits d'unicite (RG-03, RG-12) dans ce
    // projet ; une periode future est une donnee invalide, pas un conflit.
    @ExceptionHandler(PeriodeProcessusFutureException.class)
    public ResponseEntity<Map<String, Object>> gererPeriodeProcessusFuture(PeriodeProcessusFutureException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    // Sprint MM.11 : rattrapage demande sur une periode sans processus normal
    // CLOTURE. 400 -- meme raisonnement que PeriodeProcessusFutureException :
    // ce n'est pas un conflit d'unicite (409), c'est une donnee de requete
    // invalide au regard de l'etat actuel du systeme.
    @ExceptionHandler(ProcessusOriginalIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererProcessusOriginalIntrouvable(ProcessusOriginalIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(ProcessusMensuelIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererProcessusMensuelIntrouvable(ProcessusMensuelIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(ProcessusMensuelNonModifiableException.class)
    public ResponseEntity<Map<String, Object>> gererProcessusMensuelNonModifiable(ProcessusMensuelNonModifiableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(MatriculeUtilisateurDejaUtiliseException.class)
    public ResponseEntity<Map<String, Object>> gererMatriculeUtilisateurDejaUtilise(MatriculeUtilisateurDejaUtiliseException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(EmailUtilisateurDejaUtiliseException.class)
    public ResponseEntity<Map<String, Object>> gererEmailUtilisateurDejaUtilise(EmailUtilisateurDejaUtiliseException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    @ExceptionHandler(FichierImportInvalideException.class)
    public ResponseEntity<Map<String, Object>> gererFichierImportInvalide(FichierImportInvalideException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", "Fichier illisible ou format invalide");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(UtilisateurIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererUtilisateurIntrouvable(UtilisateurIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(RoleInvalideException.class)
    public ResponseEntity<Map<String, Object>> gererRoleInvalide(RoleInvalideException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.BAD_REQUEST.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corps);
    }

    @ExceptionHandler(ActionAdminNonAutoriseeException.class)
    public ResponseEntity<Map<String, Object>> gererActionAdminNonAutorisee(ActionAdminNonAutoriseeException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    // RG-05 : role de l'acteur incompatible avec l'etape declenchee par le
    // statut du processus. 403 au meme titre que la violation RG-08 ci-dessous,
    // mais motif distinct pour rester exploitable en audit et cote client.
    @ExceptionHandler(RoleEtapeNonAutoriseException.class)
    public ResponseEntity<Map<String, Object>> gererRoleEtapeNonAutorise(RoleEtapeNonAutoriseException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.FORBIDDEN.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corps);
    }

    // Sprint MM.12 (option P-2) : une fonction de l'etat mensuel n'a plus de
    // grille en vigueur parce qu'une grille attend encore une signature. 409 :
    // l'etat courant du referentiel est en conflit avec la validation demandee.
    @ExceptionHandler(ValidationBloqueeGrilleEnAttenteException.class)
    public ResponseEntity<Map<String, Object>> gererValidationBloqueeGrilleEnAttente(ValidationBloqueeGrilleEnAttenteException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    // Sprint MM.12 : validation ARH demandee alors que des montants sont
    // obsoletes et que l'ARH ne les a pas confirmes (variante B2-RESYNC en deux
    // temps). 409 : l'etat courant du processus est en conflit avec la requete.
    @ExceptionHandler(ResynchronisationNonConfirmeeException.class)
    public ResponseEntity<Map<String, Object>> gererResynchronisationNonConfirmee(ResynchronisationNonConfirmeeException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.CONFLICT.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
    }

    // Pendants MM.12 des deux handlers ci-dessus pour le workflow des grilles.
    // Exceptions distinctes et non reutilisees : celles du processus vivent dans
    // processus.exception, les lever depuis referentiel creerait un second cycle
    // de modules (option W-3 ecartee, MM.12 section 1bis). Meme statut HTTP,
    // meme forme de corps.
    @ExceptionHandler(RoleDecisionGrilleNonAutoriseException.class)
    public ResponseEntity<Map<String, Object>> gererRoleDecisionGrilleNonAutorise(RoleDecisionGrilleNonAutoriseException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.FORBIDDEN.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corps);
    }

    @ExceptionHandler(SeparationTachesGrilleViolationException.class)
    public ResponseEntity<Map<String, Object>> gererSeparationTachesGrilleViolation(SeparationTachesGrilleViolationException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.FORBIDDEN.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corps);
    }

    @ExceptionHandler(SeparationTachesViolationException.class)
    public ResponseEntity<Map<String, Object>> gererSeparationTachesViolation(SeparationTachesViolationException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.FORBIDDEN.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corps);
    }

    @ExceptionHandler(EtapeWorkflowIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererEtapeWorkflowIntrouvable(EtapeWorkflowIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(PieceJointeIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> gererPieceJointeIntrouvable(PieceJointeIntrouvableException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.NOT_FOUND.value());
        corps.put("erreur", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corps);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> gererAccesRefuse(AccessDeniedException exception) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.FORBIDDEN.value());
        corps.put("erreur", "Rôle non autorisé pour cette opération");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corps);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> gererErreurGenerique(Exception exception) {
        LOGGER.error("Erreur interne non gérée", exception);

        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("horodatage", LocalDateTime.now());
        corps.put("statut", HttpStatus.INTERNAL_SERVER_ERROR.value());
        corps.put("erreur", "Une erreur interne est survenue");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corps);
    }
}