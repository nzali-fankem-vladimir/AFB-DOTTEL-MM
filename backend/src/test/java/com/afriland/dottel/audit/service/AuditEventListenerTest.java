package com.afriland.dottel.audit.service;

import com.afriland.dottel.audit.api.EvenementAudit;
import com.afriland.dottel.audit.model.entity.AuditLog;
import com.afriland.dottel.audit.repository.AuditLogRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditEventListener auditEventListener;

    @BeforeEach
    void setUp() {
        auditEventListener = new AuditEventListener(auditLogRepository, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void surEvenementAudit_horsContexteHttp_neLevePasException() {
        RequestContextHolder.resetRequestAttributes();

        assertThatCode(() -> auditEventListener.surEvenementAudit(new EvenementAudit(
                1L, "ENROLEMENT", "beneficiaires", 10L,
                null, Map.of("matricule", "AFB2026001"))))
                .doesNotThrowAnyException();

        ArgumentCaptor<AuditLog> captureur = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captureur.capture());

        AuditLog auditLog = captureur.getValue();
        assertThat(auditLog.getAdresseIp()).isNull();
        assertThat(auditLog.getDetailJson()).contains("matricule").contains("AFB2026001");
    }

    @Test
    void surEvenementAudit_creationSansAvant_ometLaCleAvant() {
        RequestContextHolder.resetRequestAttributes();

        auditEventListener.surEvenementAudit(new EvenementAudit(
                1L, "ENROLEMENT", "beneficiaires", 10L,
                null, Map.of("matricule", "AFB2026001")));

        ArgumentCaptor<AuditLog> captureur = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captureur.capture());

        assertThat(captureur.getValue().getDetailJson()).doesNotContain("\"avant\"");
    }
}
