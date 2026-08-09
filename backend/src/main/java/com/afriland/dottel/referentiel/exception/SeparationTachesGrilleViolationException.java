package com.afriland.dottel.referentiel.exception;

/**
 * RG-08 appliquee au workflow des grilles tarifaires (Sprint MM.12, option
 * W-2) : l'acteur qui statue sur une etape ne peut pas etre celui qui a statue
 * sur l'etape precedente.
 *
 * Un compte ne portant qu'un seul RoleEnum, le scenario protege n'est pas le
 * cumul de roles mais le CHANGEMENT de role : un ARH cree une grille, un ADMIN
 * le promeut CRH via PATCH /admin/utilisateurs/{id}/role, et il validerait sa
 * propre grille.
 *
 * Volontairement distincte de processus.exception.SeparationTachesViolationException
 * -- meme motif que RoleDecisionGrilleNonAutoriseException : eviter le second
 * cycle de modules referentiel -> processus.
 */
public class SeparationTachesGrilleViolationException extends RuntimeException {

    public SeparationTachesGrilleViolationException(String message) {
        super(message);
    }
}
