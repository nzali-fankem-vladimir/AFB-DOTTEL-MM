package com.afriland.dottel.repository;

import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByMatricule(String matricule);

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findByRoleAndActifTrue(RoleEnum role);
}