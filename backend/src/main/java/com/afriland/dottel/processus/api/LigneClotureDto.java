package com.afriland.dottel.processus.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Detail d'un beneficiaire INCLUS dans l'etat mensuel cloture (Sprint MM.13).
 *
 * <p>SCHEMA PROVISOIRE, A VALIDER AVEC LA COMPTABILITE (decision D de M.0).
 * Ne pas le prendre pour un contrat fige : le contenu exact attendu par le
 * module comptable n'est pas confirme, DOTTEL en propose un.</p>
 *
 * <p>Alimente les deux lignes d'ecriture comptable cibles (CLAUDE.md section
 * 11), que DOTTEL ne genere PAS lui-meme (hors perimetre) :</p>
 * <pre>
 *   DEBIT  : codeUnite  - 64310090002       - montantAttribue - DOT TEL MM/AAAA
 *   CREDIT : codeAgence - numCompteCourant  - montantAttribue - DOT TEL MM/AAAA
 * </pre>
 *
 * <p>fonctionRetenue et montantAttribue sont les valeurs FIGEES de la ligne
 * d'etat mensuel, pas les valeurs courantes du beneficiaire ni de la grille :
 * la comptabilite doit recevoir ce qui a ete valide par la DRH, meme si la
 * grille change ensuite.</p>
 *
 * <p>Montant en FCFA ENTIER (long), jamais BigDecimal -- coherent avec
 * montantTotal.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneClotureDto {

    private String codeUnite;
    private String codeAgence;
    private String numCompteCourant;
    private String chapitre;
    private String nomPrenoms;
    private String fonctionRetenue;
    private long montantAttribue;
}
