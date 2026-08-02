package com.afriland.dottel.beneficiaires.api;

/**
 * Beneficiaire actif tel que le declenchement d'un processus mensuel en a besoin.
 *
 * Porte les quatre seuls champs reellement consommes : l'id pour rattacher la
 * ligne d'etat, la fonction pour resoudre la grille (RG-01/RG-04), le matricule
 * et le nom pour le rapport d'exclusion.
 */
public record BeneficiaireDotationDto(Long id, String matricule, String nomPrenoms, String fonction) {
}
