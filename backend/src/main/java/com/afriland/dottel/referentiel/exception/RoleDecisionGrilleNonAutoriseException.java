package com.afriland.dottel.referentiel.exception;

/**
 * Equivalent RG-05 pour le workflow des grilles (Sprint MM.12) : le role de
 * l'acteur ne correspond pas a l'etape declenchee par le statut de la grille.
 *
 * Indispensable car POST /grilles-tarifaires/{id}/valider est ouvert a la fois
 * au CRH et a la DRH (hasAnyRole), la branche etant choisie sur le seul statut
 * de la grille -- sans ce controle, une DRH executerait l'etape CRH d'une
 * grille EN_ATTENTE_CRH et l'ordre ARH puis CRH puis DRH ne serait plus
 * garanti.
 *
 * Volontairement distincte de processus.exception.RoleEtapeNonAutoriseException,
 * qui joue le meme role pour le processus mensuel : la reutiliser ferait
 * dependre referentiel de processus, donc un SECOND cycle de modules rejete
 * par ModularityTests (option W-3 ecartee, voir MM.12 section 1bis).
 */
public class RoleDecisionGrilleNonAutoriseException extends RuntimeException {

    public RoleDecisionGrilleNonAutoriseException(String message) {
        super(message);
    }
}
