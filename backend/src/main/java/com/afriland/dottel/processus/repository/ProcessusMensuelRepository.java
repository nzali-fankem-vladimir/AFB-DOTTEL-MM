package com.afriland.dottel.processus.repository;

import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProcessusMensuelRepository extends JpaRepository<ProcessusMensuel, Long> {

    boolean existsByMoisPaiementAndAnneePaiement(Integer moisPaiement, Integer anneePaiement);

    Optional<ProcessusMensuel> findByMoisPaiementAndAnneePaiement(Integer moisPaiement, Integer anneePaiement);

    Optional<ProcessusMensuel> findFirstByOrderByDateCreationDesc();

    Optional<ProcessusMensuel> findFirstByStatutNotInOrderByDateCreationDesc(Collection<StatutEnum> statuts);

    long countByStatutAndAnneePaiement(StatutEnum statut, Integer anneePaiement);

    List<ProcessusMensuel> findByStatutOrderByAnneePaiementDescMoisPaiementDesc(StatutEnum statut);

    List<ProcessusMensuel> findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(
            StatutEnum statut, Integer anneePaiement);

    List<ProcessusMensuel> findAllByOrderByAnneePaiementDescMoisPaiementDesc();

    List<ProcessusMensuel> findByAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(Integer anneePaiement);
}