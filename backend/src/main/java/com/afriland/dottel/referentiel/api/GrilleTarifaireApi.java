package com.afriland.dottel.referentiel.api;

/**
 * API publique du module referentiel pour la resolution du montant de dotation.
 *
 * UNIQUE point de resolution de grille du projet (RG-01 + RG-04). Introduite au
 * Sprint MM.2 pour que ProcessusMensuelService cesse d'injecter
 * FonctionEligibleRepository et GrilleTarifaireRepository ; MM.3 y branchera
 * les deux autres implementations existantes (BeneficiaireService et
 * EnrolementService, couplage C2 du plan maitre).
 *
 * Le grade n'est volontairement PAS un parametre : RG-02 depend du beneficiaire
 * et reste orchestree par l'appelant via EligibiliteService (decision actee au
 * Sprint MM.2, etape 3).
 */
public interface GrilleTarifaireApi {

    /**
     * Resout le montant applicable au code fonction, ou le motif qui l'exclut.
     *
     * Ordre de verification impose, inchange depuis l'origine :
     * fonction inconnue -> fonction desactivee -> grille ACTIVE introuvable.
     */
    ResolutionGrilleDto resoudrePourFonction(String codeFonction);
}
