/**
 * Enumerations du module utilisateurs (chantier MM, Sprint MM.5).
 *
 * Expose comme NamedInterface car RoleEnum est manipule par processus
 * (SeparationTachesService, ProcessusMensuelService, RG-08).
 *
 * Depuis MM.7 : security.RoleJwtAuthenticationConverter ne depend plus de
 * RoleEnum -- il compare les roles Keycloak (realm_access.roles) a un
 * ensemble de chaines litterales (ROLES_DOTTEL), pour rester independant du
 * cycle de vie de cet enum cote module security (transverse, OPEN).
 */
@org.springframework.modulith.NamedInterface("enums")
package com.afriland.dottel.utilisateurs.model.enums;
