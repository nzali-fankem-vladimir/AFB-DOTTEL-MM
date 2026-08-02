package com.afriland.dottel.beneficiaires.api;

/**
 * Identite d'un beneficiaire, pour affichage du detail d'un processus mensuel.
 *
 * Ni fonction ni montant : LigneEtatMensuel porte deja fonctionRetenue et
 * montantApplique, instantanes volontairement figes (CLAUDE.md section 4).
 */
public record BeneficiaireIdentiteDto(Long id, String matricule, String nomPrenoms) {
}
