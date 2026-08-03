package com.afriland.dottel.audit.service;

import com.afriland.dottel.reporting.model.dto.reporting.AuditLogResponseDto;
import com.afriland.dottel.audit.model.entity.AuditLog;
import com.afriland.dottel.audit.repository.AuditLogRepository;
import com.afriland.dottel.audit.repository.AuditLogSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> rechercher(Long idUtilisateur, String action, String entiteCible,
                                                 LocalDateTime dateDebut, LocalDateTime dateFin, Pageable pageable) {
        Pageable pageableTrie = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "dateAction"));

        Page<AuditLog> page = auditLogRepository.findAll(
                AuditLogSpecifications.avecFiltres(idUtilisateur, action, entiteCible, dateDebut, dateFin),
                pageableTrie);

        return page.map(this::versDto);
    }

    private AuditLogResponseDto versDto(AuditLog auditLog) {
        return AuditLogResponseDto.builder()
                .id(auditLog.getId())
                .idUtilisateur(auditLog.getIdUtilisateur())
                .action(auditLog.getAction())
                .entiteCible(auditLog.getEntiteCible())
                .idEntite(auditLog.getIdEntite())
                .dateAction(auditLog.getDateAction())
                .adresseIp(auditLog.getAdresseIp())
                .detailJson(auditLog.getDetailJson())
                .build();
    }

}