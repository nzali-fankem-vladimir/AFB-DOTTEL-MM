/**
 * API publique du module processus (chantier MM, Sprint MM.5).
 *
 * Seul EvenementClotureDto vit ici : l'evenement de cloture publie sur Kafka
 * (EvenementClotureService), consomme en dehors du module DOTTEL par le
 * systeme comptable externe, mais aussi serialise par config/ (transverse),
 * qui a donc besoin d'un acces declare au type.
 */
@org.springframework.modulith.NamedInterface("api")
package com.afriland.dottel.processus.api;
