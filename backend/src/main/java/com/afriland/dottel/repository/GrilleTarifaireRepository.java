package com.afriland.dottel.repository;

import com.afriland.dottel.model.entity.GrilleTarifaire;
import com.afriland.dottel.model.enums.StatutGrilleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrilleTarifaireRepository extends JpaRepository<GrilleTarifaire, Long> {

    Optional<GrilleTarifaire> findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
            Long idFonctionEligible, StatutGrilleEnum statutValidation);

    List<GrilleTarifaire> findByIdFonctionEligibleOrderByDateDebutDesc(Long idFonctionEligible);
}