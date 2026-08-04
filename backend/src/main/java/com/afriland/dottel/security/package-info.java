/**
 * Module security (chantier MM, Sprint MM.5 ; jeton Keycloak depuis MM.7).
 *
 * Transverse par nature (confirme en MM.0, section "Ce qui reste transverse
 * dans tous les cas") : GlobalExceptionHandler doit voir les exceptions
 * levees par les 6 modules metier (@RestControllerAdvice global).
 *
 * Depuis MM.7 (decision de portee P-2) : Keycloak est l'unique emetteur de
 * jetons, JwtUtil et AuthService (qui creaient un aller-retour security <->
 * utilisateurs) ont ete supprimes. RoleJwtAuthenticationConverter lit desormais
 * les roles directement depuis les claims Keycloak (realm_access.roles), sans
 * dependre d'une entite ou d'un enum du module utilisateurs.
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
