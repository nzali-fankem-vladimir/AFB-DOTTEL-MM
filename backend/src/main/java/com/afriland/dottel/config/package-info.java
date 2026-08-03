/**
 * Module config (chantier MM, Sprint MM.5).
 *
 * Transverse par nature (confirme en MM.0) : KafkaConfig et
 * EvenementClotureSerializer ont besoin de processus.api.EvenementClotureDto
 * pour (de)serialiser l'evenement de cloture publie sur Kafka. Marque OPEN
 * pour la meme raison que security (voir security/package-info.java).
 */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.afriland.dottel.config;
