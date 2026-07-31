package com.afriland.dottel.config;

import com.afriland.dottel.model.dto.processus.EvenementClotureDto;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

// Serializer Kafka minimal ecrit a la main plutot que
// org.springframework.kafka.support.serializer.JsonSerializer : ce dernier
// s'appuie sur com.fasterxml.jackson (Jackson 2), alors que tout le reste du
// projet utilise tools.jackson (Jackson 3, voir AuditServiceImpl). L'instance
// est fournie directement au ProducerFactory (voir KafkaConfig), donc pas
// besoin d'un constructeur sans argument pour l'instanciation par reflexion
// habituelle de Kafka.
public class EvenementClotureSerializer implements Serializer<EvenementClotureDto> {

    private final ObjectMapper objectMapper;

    public EvenementClotureSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(String topic, EvenementClotureDto data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JacksonException exception) {
            throw new SerializationException(
                    "Echec de serialisation de l'evenement de cloture pour le topic " + topic, exception);
        }
    }
}