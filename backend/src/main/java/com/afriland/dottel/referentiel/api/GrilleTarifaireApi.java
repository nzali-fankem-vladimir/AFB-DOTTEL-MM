package com.afriland.dottel.referentiel.api;

import java.util.Optional;

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

    /**
     * Grille engagee dans le circuit de validation pour cette fonction, s'il y
     * en a une (Sprint MM.12).
     *
     * <p>Volontairement SEPAREE de {@link #resoudrePourFonction} plutot
     * qu'ajoutee a {@link ResolutionGrilleDto} : ce dernier porte "SOIT un
     * montant, SOIT un motif d'exclusion, jamais les deux", invariant que tout
     * le projet exploite depuis MM.2, et ses libelles de motif sont figes par
     * le contrat API. Cette methode n'est appelee que sur le chemin d'exclusion,
     * cas rare.</p>
     *
     * <p>Au plus une grille peut etre en attente par fonction : la creation
     * refuse une seconde grille tant qu'une autre attend le CRH ou la DRH.</p>
     *
     * @return la grille en attente, ou vide si la fonction n'en a aucune
     */
    Optional<GrilleEnAttenteDto> grilleEnAttentePourFonction(String codeFonction);
}
