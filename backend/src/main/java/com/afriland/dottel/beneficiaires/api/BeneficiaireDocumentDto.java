package com.afriland.dottel.beneficiaires.api;

/**
 * Donnees d'affichage d'un beneficiaire pour la generation du PDF d'etat
 * mensuel (module processus).
 *
 * Introduit au Sprint MM.3 (couplage C4) pour que DocumentService cesse
 * d'injecter BeneficiaireRepository. Porte exactement les quatre champs
 * utilises dans le tableau du PDF -- ni la fonction (figee sur la ligne
 * d'etat via fonctionRetenue, pas sur le beneficiaire), ni le matricule,
 * ni le grade.
 */
public record BeneficiaireDocumentDto(Long id, String nomPrenoms, String codeUnite, String numCompteCourant,
                                       String chapitre) {
}
