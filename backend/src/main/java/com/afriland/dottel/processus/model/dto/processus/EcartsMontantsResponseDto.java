package com.afriland.dottel.processus.model.dto.processus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Reponse de GET /processus/{id}/ecarts-montants (Sprint MM.12).
 *
 * Premier temps de la variante B2-RESYNC : DETECTE les ecarts entre le montant
 * stocke sur chaque ligne incluse et la grille ACTIVE courante, SANS RIEN
 * MODIFIER. L'ARH voit le recapitulatif, puis confirme -- c'est le second appel
 * (POST /processus/{id}/valider?confirmerResynchronisation=true) qui ecrit.
 *
 * Interpretation "en deux temps" arbitree le 2026-08-09 : le montant alimente
 * un PDF signe puis la comptabilite, un point de non-retour merite un point
 * d'arret.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcartsMontantsResponseDto {

    private Long idProcessus;

    /** Lignes payees a un autre montant. */
    private List<LigneResynchroniseeDto> lignesResynchronisees;

    /** Lignes qui ne seront PAS payees ce mois-ci (plus de grille ACTIVE). */
    private List<LigneExclueResynchronisationDto> lignesExclues;

    /** true si aucune des deux listes n'est peuplee : validation directe possible. */
    public boolean isAucunEcart() {
        return (lignesResynchronisees == null || lignesResynchronisees.isEmpty())
                && (lignesExclues == null || lignesExclues.isEmpty());
    }
}
