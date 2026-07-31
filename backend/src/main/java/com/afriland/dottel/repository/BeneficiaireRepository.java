package com.afriland.dottel.repository;

import com.afriland.dottel.model.entity.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long>,
        JpaSpecificationExecutor<Beneficiaire> {

    Optional<Beneficiaire> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    List<Beneficiaire> findByActifTrue();

    long countByActifTrue();

    long countByFonctionAndActifTrue(String fonction);

    List<Beneficiaire> findByFonction(String fonction);
}