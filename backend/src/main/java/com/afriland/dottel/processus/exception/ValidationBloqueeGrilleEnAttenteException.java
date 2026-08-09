package com.afriland.dottel.processus.exception;

/**
 * Validation ARH refusee parce qu'au moins une ligne incluse porte une fonction
 * dont la grille tarifaire attend encore une signature CRH ou DRH (Sprint
 * MM.12, option P-2 arbitree le 2026-08-09). Traduite en 409 par
 * GlobalExceptionHandler.
 *
 * <p><b>Pourquoi bloquer ici, alors que "bloquer" avait ete ecarte le 9 aout
 * pour le cas general.</b> L'argument d'alors etait qu'obtenir une grille
 * exigeait de relancer un cycle complet ARH-CRH-DRH, ce qui gelait la paie sans
 * gain. Il ne s'applique pas a ce cas precis : le cycle est DEJA EN VOL, il ne
 * s'agit que d'attendre une signature en cours. Exclure un beneficiaire de la
 * paie du mois parce qu'une signature tarde de deux jours n'a aucun sens
 * metier.</p>
 *
 * <p>L'ARH dispose de deux sorties, toutes deux explicites dans le message :
 * faire aboutir la signature (le beneficiaire sera paye au montant recale), ou
 * exclure deliberement la ligne via PATCH /processus/{id} (le beneficiaire ne
 * sera pas paye, mais c'est une decision prise, pas un effet de bord).</p>
 *
 * <p>Le cas DURABLE — aucune grille, ou uniquement des grilles rejetees — ne
 * passe pas par ici : la ligne y est simplement exclue, conformement a
 * l'arbitrage du 2026-08-09.</p>
 */
public class ValidationBloqueeGrilleEnAttenteException extends RuntimeException {

    public ValidationBloqueeGrilleEnAttenteException(String message) {
        super(message);
    }
}
