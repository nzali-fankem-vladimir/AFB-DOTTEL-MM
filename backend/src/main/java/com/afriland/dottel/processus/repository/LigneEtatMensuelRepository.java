package com.afriland.dottel.processus.repository;

import com.afriland.dottel.processus.model.entity.LigneEtatMensuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LigneEtatMensuelRepository extends JpaRepository<LigneEtatMensuel, Long> {

    List<LigneEtatMensuel> findByIdProcessus(Long idProcessus);

    List<LigneEtatMensuel> findByIdProcessusAndInclusDansEtatTrue(Long idProcessus);

    Optional<LigneEtatMensuel> findByIdProcessusAndIdBeneficiaire(Long idProcessus, Long idBeneficiaire);

    long countByIdProcessusAndInclusDansEtatTrue(Long idProcessus);

    // Somme des montants retenus pour l'etat mensuel (RG utilisee pour
    // l'evenement de cloture Kafka, et reutilisable pour le tableau de bord
    // du Sprint 6). COALESCE evite un null quand aucune ligne n'est incluse.
    @Query("SELECT COALESCE(SUM(l.montantApplique), 0) FROM LigneEtatMensuel l "
            + "WHERE l.idProcessus = :idProcessus AND l.inclusDansEtat = true")
    long sumMontantAppliqueByIdProcessus(@Param("idProcessus") Long idProcessus);
}