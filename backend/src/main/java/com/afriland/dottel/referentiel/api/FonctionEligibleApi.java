package com.afriland.dottel.referentiel.api;

import java.util.Optional;

/**
 * API publique du module referentiel pour les lectures ponctuelles sur
 * fonction_eligible, destinees aux autres modules (Sprint MM.5).
 *
 * Les deux methodes existaient deja dans FonctionEligibleService depuis le
 * Sprint MM.3 (couplage C2), specifiquement pour EnrolementService et
 * BeneficiaireImportService (beneficiaires) et ProcessusMensuelService
 * (processus) -- seul le passage par une interface dediee etait manquant.
 */
public interface FonctionEligibleApi {

    /**
     * Libelle d'affichage de la fonction, ou vide si le code est inconnu.
     */
    Optional<String> libelle(String codeFonction);

    /**
     * true/false si le code de fonction est connu (actif ou non), vide si
     * inconnu -- distinction necessaire pour RG-11 (rapport d'import Excel).
     */
    Optional<Boolean> estActive(String codeFonction);
}
