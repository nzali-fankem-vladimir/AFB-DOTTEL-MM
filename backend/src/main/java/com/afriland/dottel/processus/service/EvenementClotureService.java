package com.afriland.dottel.processus.service;

import com.afriland.dottel.processus.model.dto.processus.EvenementClotureDto;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvenementClotureService {

    private final KafkaTemplate<String, EvenementClotureDto> kafkaTemplateEvenementCloture;

    @Value("${dottel.kafka.topic-cloture}")
    private String topicCloture;

    // DOTTEL est producer uniquement (voir KafkaConfig) : aucune reponse
    // n'est attendue du module comptable ici, l'envoi est fire-and-forget.
    // La cle du message (idProcessus) garantit que tous les evenements d'un
    // meme processus (s'il y en avait plusieurs) restent sur la meme
    // partition, dans l'ordre d'envoi.
    public void publier(ProcessusMensuel processus, long montantTotal) {
        EvenementClotureDto evenement = EvenementClotureDto.builder()
                .idProcessus(processus.getId())
                .moisPaiement(processus.getMoisPaiement())
                .anneePaiement(processus.getAnneePaiement())
                .montantTotal(montantTotal)
                .dateCloture(processus.getDateCloture())
                .build();

        kafkaTemplateEvenementCloture.send(topicCloture, String.valueOf(processus.getId()), evenement);
    }
}