package com.afriland.dottel.referentiel.api;

/**
 * Resultat de la resolution du montant applicable a un code fonction.
 *
 * Porte SOIT un montant, SOIT un motif d'exclusion -- jamais les deux, jamais
 * aucun des deux. Ne transporte aucune entite JPA : ni FonctionEligible, ni
 * GrilleTarifaire (CLAUDE.md section 18 point 4).
 */
public record ResolutionGrilleDto(Integer montantFcfa, String motifExclusion) {

    // Libelles repris a l'identique de ProcessusMensuelService avant MM.2 : ils
    // remontent tels quels dans les reponses POST /processus/declencher
    // (BeneficiaireExcluDto.motif) et PATCH /processus/{id}
    // (ResultatAjustementDto.motifRejet). Le contrat API est fige, ne pas
    // reformuler ces chaines.
    public static final String MOTIF_FONCTION_INCONNUE =
            "Fonction inconnue du référentiel fonction_eligible";
    public static final String MOTIF_FONCTION_DESACTIVEE =
            "Fonction désactivée";
    public static final String MOTIF_GRILLE_INTROUVABLE =
            "Aucune grille tarifaire ACTIVE pour cette fonction";

    public static ResolutionGrilleDto resolue(int montantFcfa) {
        return new ResolutionGrilleDto(montantFcfa, null);
    }

    public static ResolutionGrilleDto exclue(String motifExclusion) {
        return new ResolutionGrilleDto(null, motifExclusion);
    }

    public boolean estResolue() {
        return motifExclusion == null;
    }
}
