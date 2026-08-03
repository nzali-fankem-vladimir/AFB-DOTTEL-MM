/**
 * Module security (chantier MM, Sprint MM.5).
 *
 * Transverse par nature (confirme en MM.0, section "Ce qui reste transverse
 * dans tous les cas") : GlobalExceptionHandler doit voir les exceptions
 * levees par les 6 modules metier (@RestControllerAdvice global), et JwtUtil
 * a besoin de Utilisateur (entite) et RoleEnum pour encoder le token JWT.
 * A l'inverse, utilisateurs.AuthService appelle security.JwtUtil pour
 * generer ce token -- creant un aller-retour reel entre security et
 * utilisateurs (cycle detecte par ModularityTests avant cette annotation).
 *
 * Marque OPEN (plutot que de multiplier les allowedDependencies un par un) :
 * un module OPEN n'est soumis ni a l'encapsulation (ses propres types sont
 * tous consideres exposes) ni a la verification d'acyclicite -- c'est
 * l'idiome Spring Modulith standard pour une infrastructure transverse
 * de ce type, et non une derogation ad hoc.
 */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.afriland.dottel.security;
