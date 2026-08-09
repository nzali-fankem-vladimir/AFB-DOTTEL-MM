package com.afriland.dottel.audit.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

    Page<AuditLogResponseDto> rechercher(Long idUtilisateur, String action, String entiteCible,
                                          LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable);

    // Sprint MM.11 : alimente le filtre "Action" du journal d'audit sans liste
    // figee cote frontend (voir AuditLogRepository.findDistinctActions()).
    List<String> listerActionsDistinctes();
}