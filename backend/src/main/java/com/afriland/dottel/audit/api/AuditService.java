package com.afriland.dottel.audit.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {

    Page<AuditLogResponseDto> rechercher(Long idUtilisateur, String action, String entiteCible,
                                          LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);
}