/**
 * Enumerations du module processus (chantier MM, Sprint MM.3, couplage C5).
 *
 * Expose comme NamedInterface uniquement parce que StatutEnum est manipule par
 * ReportingService via ProcessusMensuel (voir processus/model/entity/package-info.java) :
 * consequence directe de l'exception R-2 accordee au module reporting, pas une
 * exposition d'API generale.
 */
@org.springframework.modulith.NamedInterface("enums")
package com.afriland.dottel.processus.model.enums;
