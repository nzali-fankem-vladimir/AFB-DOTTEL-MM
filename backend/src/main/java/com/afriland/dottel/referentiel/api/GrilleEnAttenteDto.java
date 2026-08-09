package com.afriland.dottel.referentiel.api;

import java.time.LocalDateTime;

/**
 * Grille tarifaire engagee dans le circuit de validation mais pas encore en
 * vigueur (Sprint MM.12, options P-1 + P-2 arbitrees le 2026-08-09).
 *
 * <p>Existe pour distinguer deux absences de grille ACTIVE que
 * {@link ResolutionGrilleDto#MOTIF_GRILLE_INTROUVABLE} confond aujourd'hui :</p>
 * <ul>
 *   <li><b>durable</b> — aucune grille, ou seulement des grilles rejetees : la
 *       ligne est exclue de l'etat mensuel, comme arbitre le 2026-08-09 ;</li>
 *   <li><b>transitoire</b> — une grille attend une signature CRH ou DRH : la
 *       validation est BLOQUEE, car exclure quelqu'un de la paie parce qu'une
 *       signature tarde n'a aucun sens metier.</li>
 * </ul>
 *
 * <p>MM.12 rend ce cas transitoire plus frequent qu'avant : le circuit des
 * grilles est passe de deux a trois acteurs, la fenetre entre la fermeture de
 * l'ancienne grille et l'activation de la nouvelle s'est donc allongee.</p>
 *
 * @param montantFcfa    montant propose par la grille en attente
 * @param etape          "CRH" ou "DRH" — etage qui doit encore statuer
 * @param dateSoumission date de creation de la grille, pour mesurer l'attente
 */
public record GrilleEnAttenteDto(Integer montantFcfa, String etape, LocalDateTime dateSoumission) {
}
