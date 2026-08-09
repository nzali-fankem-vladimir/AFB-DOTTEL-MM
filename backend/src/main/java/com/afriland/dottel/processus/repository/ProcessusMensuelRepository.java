package com.afriland.dottel.processus.repository;

import com.afriland.dottel.processus.model.entity.ProcessusMensuel;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProcessusMensuelRepository extends JpaRepository<ProcessusMensuel, Long> {

    // Sprint MM.11 : suffixees AndRattrapageFalse depuis que RG-12 evolue --
    // plusieurs ProcessusMensuel peuvent desormais partager le meme
    // mois/annee (un normal + un ou plusieurs rattrapages). Sans ce filtre,
    // findByMoisPaiementAndAnneePaiement (variante non filtree, supprimee)
    // leverait une NonUniqueResultException des qu'un rattrapage existe pour
    // la periode. Un seul processus normal reste garanti par periode par
    // idx_processus_mensuel_normal_unique (migration V6).
    boolean existsByMoisPaiementAndAnneePaiementAndRattrapageFalse(Integer moisPaiement, Integer anneePaiement);

    Optional<ProcessusMensuel> findByMoisPaiementAndAnneePaiementAndRattrapageFalse(Integer moisPaiement, Integer anneePaiement);

    Optional<ProcessusMensuel> findFirstByOrderByDateCreationDesc();

    Optional<ProcessusMensuel> findFirstByStatutNotInOrderByDateCreationDesc(Collection<StatutEnum> statuts);

    long countByStatutAndAnneePaiement(StatutEnum statut, Integer anneePaiement);

    List<ProcessusMensuel> findByStatutOrderByAnneePaiementDescMoisPaiementDesc(StatutEnum statut);

    List<ProcessusMensuel> findByStatutAndAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(
            StatutEnum statut, Integer anneePaiement);

    List<ProcessusMensuel> findAllByOrderByAnneePaiementDescMoisPaiementDesc();

    List<ProcessusMensuel> findByAnneePaiementOrderByAnneePaiementDescMoisPaiementDesc(Integer anneePaiement);
}