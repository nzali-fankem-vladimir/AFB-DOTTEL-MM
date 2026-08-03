package com.afriland.dottel.audit.service;

import com.afriland.dottel.reporting.model.dto.reporting.AuditLogResponseDto;
import com.afriland.dottel.audit.model.entity.AuditLog;
import com.afriland.dottel.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditLogRepository);
    }

    @Test
    void rechercher_sansFiltre_retourneTouteLesEntrees() {
        AuditLog connexionSylvie = creerAuditLog(1L, "CONNEXION", "utilisateurs", 1L,
                LocalDateTime.of(2026, 7, 20, 8, 30), null);
        AuditLog enrolementJean = creerAuditLog(2L, "ENROLEMENT", "beneficiaires", 10L,
                LocalDateTime.of(2026, 7, 21, 9, 0), "{\"apres\":{\"matricule\":\"AFB2026010\"}}");
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(enrolementJean, connexionSylvie), pageable, 2));

        Page<AuditLogResponseDto> resultat = auditService.rechercher(null, null, null, null, null, pageable);

        assertThat(resultat.getContent()).hasSize(2);
        assertThat(resultat.getContent()).extracting(AuditLogResponseDto::getAction)
                .containsExactly("ENROLEMENT", "CONNEXION");
    }

    @Test
    void rechercher_filtreParUtilisateur_retourneUniquementSesActions() {
        AuditLog validationArh = creerAuditLog(3L, "VALIDATION_PROCESSUS_ARH", "processus_mensuel", 5L,
                LocalDateTime.of(2026, 7, 22, 10, 0), null);
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(validationArh), pageable, 1));

        Page<AuditLogResponseDto> resultat = auditService.rechercher(3L, null, null, null, null, pageable);

        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().get(0).getIdUtilisateur()).isEqualTo(3L);
    }

    @Test
    void rechercher_filtreParPeriode_respecteLesBornes() {
        LocalDateTime dateDebut = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime dateFin = LocalDateTime.of(2026, 7, 31, 23, 59);
        AuditLog auditLogDansPeriode = creerAuditLog(4L, "MODIFICATION_BENEFICIAIRE", "beneficiaires", 15L,
                LocalDateTime.of(2026, 7, 15, 14, 0), "{\"avant\":{\"grade\":\"NON GRADE\"}}");
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(auditLogDansPeriode), pageable, 1));

        Page<AuditLogResponseDto> resultat =
                auditService.rechercher(null, null, null, dateDebut, dateFin, pageable);

        assertThat(resultat.getContent()).hasSize(1);
        assertThat(resultat.getContent().get(0).getDateAction()).isBetween(dateDebut, dateFin);
    }

    @Test
    void rechercher_detailJsonPresentPourLesModifications() {
        String deltaJson = "{\"avant\":{\"fonction\":\"CONSEILLER\"},\"apres\":{\"fonction\":\"GFC\"}}";
        AuditLog modification = creerAuditLog(5L, "MODIFICATION_BENEFICIAIRE", "beneficiaires", 20L,
                LocalDateTime.of(2026, 7, 23, 11, 0), deltaJson);
        Pageable pageable = PageRequest.of(0, 20);

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(modification), pageable, 1));

        Page<AuditLogResponseDto> resultat = auditService.rechercher(null, null, null, null, null, pageable);

        assertThat(resultat.getContent().get(0).getDetailJson())
                .contains("\"avant\"").contains("CONSEILLER")
                .contains("\"apres\"").contains("GFC");
    }

    private AuditLog creerAuditLog(Long idUtilisateur, String action, String entiteCible, Long idEntite,
                                    LocalDateTime dateAction, String detailJson) {
        return AuditLog.builder()
                .id(idEntite)
                .idUtilisateur(idUtilisateur)
                .action(action)
                .entiteCible(entiteCible)
                .idEntite(idEntite)
                .dateAction(dateAction)
                .detailJson(detailJson)
                .build();
    }
}