package com.afriland.dottel.repository;

import com.afriland.dottel.model.entity.EtapeWorkflow;
import com.afriland.dottel.model.enums.StatutEtapeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapeWorkflowRepository extends JpaRepository<EtapeWorkflow, Long> {

    List<EtapeWorkflow> findByIdProcessusOrderByOrdreEtapeAsc(Long idProcessus);

    // RETOURNE n'est pas terminal : un processus peut etre retourne plusieurs
    // fois au fil de ses resoumissions. Trie par dateAction (pas ordreEtape,
    // qui peut se repeter d'un cycle a l'autre) pour ne jamais remonter un
    // motif obsolete d'un retour anterieur.
    Optional<EtapeWorkflow> findFirstByIdProcessusAndStatutEtapeOrderByDateActionDesc(
            Long idProcessus, StatutEtapeEnum statutEtape);
}