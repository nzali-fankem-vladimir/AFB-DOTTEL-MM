/**
 * Repositories du module processus (chantier MM, Sprint MM.3, couplage C5).
 *
 * Expose comme NamedInterface pour que le module reporting -- seul consommateur
 * legitime a ce jour, verifie par ModularityTests -- puisse y lire directement
 * (voir reporting/package-info.java pour le motif complet de l'exception R-2).
 */
@org.springframework.modulith.NamedInterface("repository")
package com.afriland.dottel.processus.repository;
