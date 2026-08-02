package com.afriland.dottel.referentiel.repository;

import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FonctionEligibleRepository extends JpaRepository<FonctionEligible, Long> {

    Optional<FonctionEligible> findByCode(String code);

    List<FonctionEligible> findByActifTrueOrderByLibelleAsc();

    List<FonctionEligible> findAllByOrderByLibelleAsc();
}