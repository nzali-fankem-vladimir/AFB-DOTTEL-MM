package com.afriland.dottel.processus.model.dto.processus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ligne dont le montant a ete (ou va etre) recale sur la grille ACTIVE
 * courante -- variante B2-RESYNC de la decision B, Sprint MM.12.
 *
 * Consequence pour le beneficiaire : il EST paye, mais a un autre montant.
 * Volontairement d'un type DIFFERENT de {@link LigneExclueResynchronisationDto},
 * dont la consequence est tout autre (pas paye du tout) : le recapitulatif doit
 * presenter les deux categories en blocs separes, jamais dans une liste
 * indifferenciee ou un simple drapeau booleen, ou un ARH qui survole pourrait
 * ne pas voir qu'il vient de retirer quelqu'un de la paie du mois (exigence
 * utilisateur du 2026-08-09).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneResynchroniseeDto {

    private Long idBeneficiaire;
    private String matricule;
    private String nomPrenoms;
    private String fonctionRetenue;
    private Integer ancienMontant;
    private Integer nouveauMontant;
}
