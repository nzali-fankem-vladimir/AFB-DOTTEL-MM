package com.afriland.dottel.processus.service;

import com.afriland.dottel.beneficiaires.api.BeneficiaireApi;
import com.afriland.dottel.beneficiaires.api.BeneficiaireClotureDto;
import com.afriland.dottel.processus.api.EvenementClotureDto;
import com.afriland.dottel.processus.api.LigneClotureDto;
import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.repository.LigneEtatMensuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvenementClotureService {

    private final KafkaTemplate<String, EvenementClotureDto> kafkaTemplateEvenementCloture;
    private final LigneEtatMensuelRepository ligneEtatMensuelRepository;
    private final BeneficiaireApi beneficiaireApi;

    @Value("${dottel.kafka.topic-cloture}")
    private String topicCloture;

    // Meme repli que DocumentService (Sprint 3.4) sur la meme propriete : la
    // donnee EHR chapitre manque aux beneficiaires enroles avant son
    // introduction. Une seconde logique de repli divergerait tot ou tard du
    // chapitre imprime sur le PDF signe.
    @Value("${dottel.documents.chapitre-defaut}")
    private String chapitreDefaut;

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
                .lignes(construireLignes(processus.getId()))
                .build();

        kafkaTemplateEvenementCloture.send(topicCloture, String.valueOf(processus.getId()), evenement);
    }

    /**
     * Une entree par beneficiaire INCLUS (Sprint MM.13).
     *
     * <p>Un beneficiaire exclu de l'etat ne doit jamais partir en
     * comptabilite : il n'a pas ete paye, une ecriture le concernant serait
     * fausse. D'ou findByIdProcessusAndInclusDansEtatTrue, jamais findAll.</p>
     *
     * <p>fonctionRetenue et montantApplique proviennent de LigneEtatMensuel :
     * ce sont les valeurs figees a la validation, jamais recalculees depuis la
     * grille courante.</p>
     */
    private List<LigneClotureDto> construireLignes(Long idProcessus) {
        List<LigneEtatMensuel> lignesIncluses =
                ligneEtatMensuelRepository.findByIdProcessusAndInclusDansEtatTrue(idProcessus);

        // Un SEUL appel par lot, jamais un par ligne : un processus porte
        // plusieurs dizaines de beneficiaires, un appel unitaire serait un N+1.
        Map<Long, BeneficiaireClotureDto> beneficiairesParId = beneficiaireApi.donneesClotureParId(
                lignesIncluses.stream().map(LigneEtatMensuel::getIdBeneficiaire).toList());

        return lignesIncluses.stream()
                .map(ligne -> {
                    BeneficiaireClotureDto beneficiaire = beneficiairesParId.get(ligne.getIdBeneficiaire());
                    return LigneClotureDto.builder()
                            .codeUnite(beneficiaire != null ? beneficiaire.codeUnite() : null)
                            .codeAgence(beneficiaire != null ? beneficiaire.codeAgence() : null)
                            .numCompteCourant(beneficiaire != null ? beneficiaire.numCompteCourant() : null)
                            .chapitre(chapitreDe(beneficiaire))
                            .nomPrenoms(beneficiaire != null ? beneficiaire.nomPrenoms() : null)
                            .fonctionRetenue(ligne.getFonctionRetenue())
                            .montantAttribue(ligne.getMontantApplique())
                            .build();
                })
                .toList();
    }

    private String chapitreDe(BeneficiaireClotureDto beneficiaire) {
        if (beneficiaire == null || beneficiaire.chapitre() == null) {
            return chapitreDefaut;
        }
        return beneficiaire.chapitre();
    }
}
