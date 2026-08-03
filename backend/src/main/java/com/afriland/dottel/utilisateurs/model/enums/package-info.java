/**
 * Enumerations du module utilisateurs (chantier MM, Sprint MM.5).
 *
 * Expose comme NamedInterface car RoleEnum est manipule par processus
 * (SeparationTachesService, ProcessusMensuelService, RG-08) et par
 * security.JwtUtil (transverse, RG-05 -- encodage du role dans le token JWT).
 */
@org.springframework.modulith.NamedInterface("enums")
package com.afriland.dottel.utilisateurs.model.enums;
