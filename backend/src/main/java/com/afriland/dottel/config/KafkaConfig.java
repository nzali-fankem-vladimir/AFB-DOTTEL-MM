package com.afriland.dottel.config;

import com.afriland.dottel.processus.api.EvenementClotureDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

// DOTTEL est producer Kafka uniquement (aucun @KafkaListener/consumer) :
// c'est le module comptable, hors perimetre DOTTEL, qui consomme le topic
// dottel.processus.cloture (voir CLAUDE.md section 11 et sprint actuel.md
// section 1). KAFKA_BOOTSTRAP_SERVERS est externalise en variable
// d'environnement (spring.kafka.bootstrap-servers dans application*.yml),
// jamais en dur, meme traitement que DB_PASSWORD et DOTTEL_JWT_SECRET.
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, EvenementClotureDto> producerFactoryEvenementCloture(ObjectMapper objectMapper) {
        Map<String, Object> configs = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(configs, new StringSerializer(),
                new EvenementClotureSerializer(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, EvenementClotureDto> kafkaTemplateEvenementCloture(
            ProducerFactory<String, EvenementClotureDto> producerFactoryEvenementCloture) {
        return new KafkaTemplate<>(producerFactoryEvenementCloture);
    }
}