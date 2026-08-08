package com.afriland.dottel.processus.model.dto.processus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Donnees necessaires a l'affichage d'une ligne du PDF d'etat mensuel.
 *
 * Introduit au Sprint MM.3 (couplage C4) : DocumentService ne va plus
 * chercher Beneficiaire ni FonctionEligible lui-meme -- l'appelant
 * (ProcessusMensuelService) assemble ce DTO via BeneficiaireApi et
 * FonctionEligibleService.libelle(). libelleFonction est deja resolu avec
 * son repli sur le code brut (ligne.getFonctionRetenue()) si le referentiel
 * ne connait plus le code : DocumentService ne fait plus ce choix, il
 * affiche ce qu'on lui donne.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneDocumentDto {

    private String nomPrenoms;
    private String codeUnite;
    private String codeAgence;
    private String numCompteCourant;
    private String chapitre;
    private String libelleFonction;
}
