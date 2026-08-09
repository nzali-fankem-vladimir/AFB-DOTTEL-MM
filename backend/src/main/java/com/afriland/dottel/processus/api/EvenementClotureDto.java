package com.afriland.dottel.processus.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evenement publie sur le topic dottel.processus.cloture a la validation DRH,
 * consomme par le module comptable (hors perimetre DOTTEL, CLAUDE.md section
 * 11 et section 17 point 9).
 *
 * <p>SCHEMA PROVISOIRE, A VALIDER AVEC LA COMPTABILITE (decision D de M.0).</p>
 *
 * <p>Enrichi au Sprint MM.13 : les champs agreges d'origine sont CONSERVES
 * (aucun consommateur existant casse), et une liste de lignes est ajoutee --
 * une entree par beneficiaire INCLUS dans l'etat, jamais un exclu.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvenementClotureDto {

    private Long idProcessus;
    private Integer moisPaiement;
    private Integer anneePaiement;
    private long montantTotal;
    private LocalDateTime dateCloture;

    private List<LigneClotureDto> lignes;
}
