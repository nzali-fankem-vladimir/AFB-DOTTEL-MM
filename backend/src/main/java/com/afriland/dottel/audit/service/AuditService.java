package com.afriland.dottel.audit.service;

import com.afriland.dottel.reporting.model.dto.reporting.AuditLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {

    Page<AuditLogResponseDto> rechercher(Long idUtilisateur, String action, String entiteCible,
                                          LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);
}