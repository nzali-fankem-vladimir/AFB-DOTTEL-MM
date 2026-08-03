/**
 * Entites du module processus (chantier MM, Sprint MM.3, couplage C5).
 *
 * Expose comme NamedInterface uniquement parce que ProcessusMensuelRepository
 * (voir processus/repository/package-info.java) retourne ce type : consequence
 * directe et inevitable de l'exception R-2 accordee au module reporting, pas
 * une exposition d'API generale. Ne pas etendre a d'autres consommateurs.
 */
@org.springframework.modulith.NamedInterface("entity")
package com.afriland.dottel.processus.model.entity;
