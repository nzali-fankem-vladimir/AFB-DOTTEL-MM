package com.afriland.dottel.beneficiaires.controller;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;

import com.afriland.dottel.beneficiaires.model.dto.beneficiaire.BeneficiairePageResponseDto;
import com.afriland.dottel.beneficiaires.model.dto.beneficiaire.BeneficiaireResponseDto;
import com.afriland.dottel.beneficiaires.model.dto.beneficiaire.ModifierBeneficiaireRequestDto;
import com.afriland.dottel.beneficiaires.model.dto.ehr.UniteRattachementDto;
import com.afriland.dottel.beneficiaires.model.dto.importexcel.ImportRapportDto;
import com.afriland.dottel.beneficiaires.service.BeneficiaireExportService;
import com.afriland.dottel.beneficiaires.service.BeneficiaireImportService;
import com.afriland.dottel.beneficiaires.service.BeneficiaireService;
import com.afriland.dottel.beneficiaires.service.EhrIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/beneficiaires")
@RequiredArgsConstructor
public class BeneficiaireController {

    private final BeneficiaireImportService beneficiaireImportService;
    private final BeneficiaireService beneficiaireService;
    private final BeneficiaireExportService beneficiaireExportService;
    private final EhrIntegrationService ehrIntegrationService;

    // Sprint MM.14 (ecart E2 de l'audit, tranche avec le metier) : DRH ajoutee.
    // Cette liste n'alimente que le filtre "Unite de rattachement" de l'ecran
    // Beneficiaires, desormais ouvert a la DRH en lecture seule. Elle
    // n'expose aucune donnee que GET /beneficiaires ne montre deja a ce role
    // -- laisser le filtre en 403 rendrait l'ecran boiteux pour la DRH.
    @GetMapping("/unites-rattachement")
    @PreAuthorize("hasAnyRole('ARH', 'DRH')")
    public ResponseEntity<List<UniteRattachementDto>> listerUnitesRattachement() {
        return ResponseEntity.ok(ehrIntegrationService.listerUnitesRattachement());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ARH', 'DRH')")
    public ResponseEntity<BeneficiairePageResponseDto> rechercher(
            @RequestParam(required = false) String fonction,
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) String uniteRattachement,
            @RequestParam(required = false) Boolean actif,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {
        Pageable pageable = PageRequest.of(page, taille);
        Page<BeneficiaireResponseDto> resultat =
                beneficiaireService.rechercher(fonction, recherche, uniteRattachement, actif, pageable);
        return ResponseEntity.ok(BeneficiairePageResponseDto.depuis(resultat));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<ImportRapportDto> importer(@RequestParam MultipartFile fichier) {
        return ResponseEntity.ok(beneficiaireImportService.importer(fichier));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<BeneficiaireResponseDto> modifier(@PathVariable Long id,
                                                              @Valid @RequestBody ModifierBeneficiaireRequestDto requete) {
        return ResponseEntity.ok(beneficiaireService.modifier(id, requete));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<Map<String, String>> desactiver(@PathVariable Long id) {
        beneficiaireService.desactiver(id);
        return ResponseEntity.ok(Map.of("message", "Beneficiaire desactive."));
    }

    @PatchMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<BeneficiaireResponseDto> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(beneficiaireService.reactiver(id));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ARH', 'DRH')")
    public ResponseEntity<byte[]> exporter() {
        byte[] classeur = beneficiaireExportService.exporter();
        String nomFichier = "beneficiaires-actifs-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier).build().toString())
                .body(classeur);
    }
}