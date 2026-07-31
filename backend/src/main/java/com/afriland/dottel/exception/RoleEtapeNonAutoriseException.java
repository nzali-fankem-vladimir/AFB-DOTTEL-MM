package com.afriland.dottel.exception;

/**
 * RG-05 : l'acteur courant n'a pas le rôle attendu pour l'étape de workflow
 * déclenchée par le statut du processus (ex. un DRH qui tenterait de valider
 * l'étape ARH d'un processus RETOURNE). Distincte de
 * {@link SeparationTachesViolationException} (RG-08), qui vise le cas où le
 * rôle est correct mais où l'acteur a déjà validé l'étape précédente.
 */
public class RoleEtapeNonAutoriseException extends RuntimeException {

    public RoleEtapeNonAutoriseException(String message) {
        super(message);
    }
}