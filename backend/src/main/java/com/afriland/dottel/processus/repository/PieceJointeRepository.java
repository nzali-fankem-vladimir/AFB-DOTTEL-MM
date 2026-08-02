package com.afriland.dottel.processus.repository;

import com.afriland.dottel.processus.model.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {

    Optional<PieceJointe> findByIdProcessus(Long idProcessus);
}