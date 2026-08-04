package com.afriland.dottel.audit.service;

import com.afriland.dottel.audit.api.EvenementAudit;
import com.afriland.dottel.audit.model.entity.AuditLog;
import com.afriland.dottel.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ecouteur synchrone (T-1) : s'execute dans la meme transaction que
 * l'appelant, juste avant le commit. Un rollback metier annule donc
 * toujours l'entree d'audit, comme avant le decouplage par evenements.
 * fallbackExecution=true : si un futur appelant publie un EvenementAudit
 * hors de toute transaction, sans ce drapeau l'evenement serait
 * silencieusement ignore au lieu de s'executer immediatement comme avant.
 */
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void surEvenementAudit(EvenementAudit evenement) {
        AuditLog auditLog = AuditLog.builder()
                .idUtilisateur(evenement.idUtilisateur())
                .action(evenement.action())
                .entiteCible(evenement.entiteCible())
                .idEntite(evenement.idEntite())
                .dateAction(LocalDateTime.now())
                .adresseIp(extraireAdresseIpCourante())
                .detailJson(construireDeltaJson(evenement.avant(), evenement.apres()))
                .build();

        auditLogRepository.save(auditLog);
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
