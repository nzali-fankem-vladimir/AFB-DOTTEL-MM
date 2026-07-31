package com.afriland.dottel.service;

import com.afriland.dottel.model.dto.reporting.AuditLogResponseDto;
import com.afriland.dottel.model.entity.AuditLog;
import com.afriland.dottel.repository.AuditLogRepository;
import com.afriland.dottel.repository.AuditLogSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void enregistrer(Long idUtilisateur, String action, String entiteCible, Long idEntite,
                             Map<String, Object> avant, Map<String, Object> apres) {
        AuditLog auditLog = AuditLog.builder()
                .idUtilisateur(idUtilisateur)
                .action(action)
                .entiteCible(entiteCible)
                .idEntite(idEntite)
                .dateAction(LocalDateTime.now())
                .adresseIp(extraireAdresseIpCourante())
                .detailJson(construireDeltaJson(avant, apres))
                .build();

        auditLogRepository.save(auditLog);
    }

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

    private String extraireAdresseIpCourante() {
        try {
            ServletRequestAttributes attributs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributs.getRequest().getRemoteAddr();
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    private String construireDeltaJson(Map<String, Object> avant, Map<String, Object> apres) {
        Map<String, Object> delta = new LinkedHashMap<>();
        if (avant != null) {
            delta.put("avant", avant);
        }
        if (apres != null) {
            delta.put("apres", apres);
        }

        if (delta.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(delta);
        } catch (JacksonException exception) {
            LOGGER.error("Echec de sérialisation du delta JSON d'audit : {}", exception.getMessage());
            return null;
        }
    }
}