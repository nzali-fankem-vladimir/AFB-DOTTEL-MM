package com.afriland.dottel.beneficiaires.api;

/**
 * Donnees d'affichage d'un beneficiaire pour la generation du PDF d'etat
 * mensuel (module processus).
 *
 * Introduit au Sprint MM.3 (couplage C4) pour que DocumentService cesse
 * d'injecter BeneficiaireRepository. Porte exactement les champs utilises
 * dans le tableau du PDF -- ni la fonction (figee sur la ligne d'etat via
 * fonctionRetenue, pas sur le beneficiaire), ni le matricule, ni le grade.
 *
 * codeAgence ajoute au Sprint MM.10 : corrige la colonne AGENCE du PDF, qui
 * affichait par erreur codeUnite (unite d'affectation professionnelle) a la
 * place du code de l'agence de domiciliation du compte courant.
 */
public record BeneficiaireDocumentDto(Long id, String nomPrenoms, String codeUnite, String codeAgence,
                                       String numCompteCourant, String chapitre) {
}
