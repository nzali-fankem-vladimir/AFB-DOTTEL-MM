package com.afriland.dottel.processus.model.dto.processus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ligne retiree de l'etat mensuel a la validation ARH parce que sa fonction n'a
 * PLUS de grille ACTIVE (grille desactivee entre le declenchement et la
 * validation) -- cas limite non couvert par la decision B, arbitre le
 * 2026-08-09 : exclure et signaler.
 *
 * Consequence pour le beneficiaire : il n'est PAS paye ce mois-ci. C'est une
 * consequence bien plus lourde qu'un simple recalage de montant, d'ou un type
 * distinct de {@link LigneResynchroniseeDto} -- voir la Javadoc de ce dernier.
 *
 * Motifs ecartes a l'arbitrage : conserver l'ancien montant paierait sur une
 * grille qui n'est plus en vigueur (contraire a RG-04, et le PDF signe puis la
 * comptabilite en heriteraient) ; bloquer la validation n'offrirait en pratique
 * a l'ARH que l'exclusion manuelle comme issue, puisque reactiver une grille
 * exige desormais le cycle complet ARH->CRH->DRH (MM.12) -- meme resultat, plus
 * lent, cycle de paie gele entre-temps.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneExclueResynchronisationDto {

    private Long idBeneficiaire;
    private String matricule;
    private String nomPrenoms;
    private String fonctionRetenue;
    private Integer ancienMontant;
    private String motifExclusion;

    // Sprint MM.12, options P-1 + P-2 (arbitrage du 2026-08-09). Renseignes
    // UNIQUEMENT quand l'absence de grille ACTIVE est TRANSITOIRE, c'est-a-dire
    // qu'une grille attend encore une signature. Dans ce cas la validation est
    // BLOQUEE (voir ValidationBloqueeGrilleEnAttenteException) : exclure
    // quelqu'un de la paie du mois parce qu'une signature tarde de deux jours
    // n'a aucun sens metier.
    //
    // Null quand l'absence est DURABLE (aucune grille, ou uniquement des
    // grilles rejetees) : la ligne est alors simplement exclue, comportement
    // arbitre le 2026-08-09 et inchange.
    private Integer montantGrilleEnAttente;
    private String etapeGrilleEnAttente;
    private LocalDateTime dateSoumissionGrilleEnAttente;

    public boolean isBloquante() {
        return etapeGrilleEnAttente != null;
    }
}
