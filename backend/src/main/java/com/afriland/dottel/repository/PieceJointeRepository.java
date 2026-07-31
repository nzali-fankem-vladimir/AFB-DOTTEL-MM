package com.afriland.dottel.repository;

import com.afriland.dottel.model.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {

    Optional<PieceJointe> findByIdProcessus(Long idProcessus);
}