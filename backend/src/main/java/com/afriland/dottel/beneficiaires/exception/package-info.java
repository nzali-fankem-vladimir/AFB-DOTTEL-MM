/**
 * Exceptions metier du module beneficiaires (chantier MM, Sprint MM.5).
 *
 * Expose comme NamedInterface car security.GlobalExceptionHandler, unique
 * @RestControllerAdvice global de l'application, doit intercepter les
 * exceptions des 6 modules metier pour produire les codes HTTP attendus par
 * le contrat API (section 8 de CLAUDE.md). Un handler global qui ne verrait
 * que les exceptions de son propre module ne remplirait pas son role.
 */
@org.springframework.modulith.NamedInterface("exception")
package com.afriland.dottel.beneficiaires.exception;
