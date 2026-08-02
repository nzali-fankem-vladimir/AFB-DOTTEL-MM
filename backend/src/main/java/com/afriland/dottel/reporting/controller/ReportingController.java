package com.afriland.dottel.reporting.controller;

import com.afriland.dottel.reporting.model.dto.reporting.AuditLogPageResponseDto;
import com.afriland.dottel.reporting.model.dto.reporting.AuditLogResponseDto;
import com.afriland.dottel.reporting.model.dto.reporting.DashboardResponseDto;
import com.afriland.dottel.reporting.model.dto.reporting.HistoriqueResponseDto;
import com.afriland.dottel.audit.service.AuditService;
import com.afriland.dottel.reporting.service.HistoriqueExportService;
import com.afriland.dottel.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/reporting")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;
    private final HistoriqueExportService historiqueExportService;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ARH', 'DRH')")
    public ResponseEntity<DashboardResponseDto> tableauDeBord() {
        DashboardResponseDto reponse = reportingService.tableauDeBord();
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/historique")
    @PreAuthorize("hasRole('DRH')")
    public ResponseEntity<HistoriqueResponseDto> historique(@RequestParam(required = false) Integer annee) {
        HistoriqueResponseDto reponse = reportingService.historique(annee);
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/historique/export")
    @PreAuthorize("hasRole('DRH')")
    public ResponseEntity<byte[]> exporterHistorique(@RequestParam(required = false) Integer annee) {
        byte[] classeur = historiqueExportService.exporter(annee);
        String nomFichier = "historique-processus-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier).build().toString())
                .body(classeur);
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('DRH')")
    public ResponseEntity<AuditLogPageResponseDto> audit(
            @RequestParam(required = false) Long idUtilisateur,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entiteCible,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {
        Pageable pageable = PageRequest.of(page, taille);
        Page<AuditLogResponseDto> resultat =
                auditService.rechercher(idUtilisateur, action, entiteCible, dateDebut, dateFin, pageable);
        return ResponseEntity.ok(AuditLogPageResponseDto.depuis(resultat));
    }
}