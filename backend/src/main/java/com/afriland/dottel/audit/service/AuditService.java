package com.afriland.dottel.audit.service;

import com.afriland.dottel.reporting.model.dto.reporting.AuditLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface AuditService {

    void enregistrer(Long idUtilisateur, String action, String entiteCible, Long idEntite,
                      Map<String, Object> avant, Map<String, Object> apres);

    Page<AuditLogResponseDto> rechercher(Long idUtilisateur, String action, String entiteCible,
                                          LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);
}