package com.afriland.dottel.referentiel.controller;

import com.afriland.dottel.referentiel.model.dto.grille.CreerGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.DecisionGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireListeResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.HistoriqueGrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.ModifierGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.referentiel.service.GrilleTarifaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grilles-tarifaires")
@RequiredArgsConstructor
public class GrilleTarifaireController {

    private final GrilleTarifaireService grilleTarifaireService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ARH', 'ADMIN')")
    public ResponseEntity<GrilleTarifaireListeResponseDto> lister(
            @RequestParam(required = false) String fonction,
            @RequestParam(required = false) StatutGrilleEnum statut) {
        GrilleTarifaireListeResponseDto reponse = GrilleTarifaireListeResponseDto.builder()
                .contenu(grilleTarifaireService.rechercher(fonction, statut))
                .build();
        return ResponseEntity.ok(reponse);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ARH', 'ADMIN')")
    public ResponseEntity<GrilleTarifaireResponseDto> creer(@Valid @RequestBody CreerGrilleTarifaireRequestDto requete) {
        Long idCreateur = authenticatedUserService.utilisateurCourant().getId();
        GrilleTarifaireResponseDto reponse = grilleTarifaireService.creer(requete, idCreateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARH', 'ADMIN')")
    public ResponseEntity<GrilleTarifaireResponseDto> modifier(@PathVariable Long id,
                                                                 @Valid @RequestBody ModifierGrilleTarifaireRequestDto requete) {
        Long idModificateur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(grilleTarifaireService.modifier(id, requete, idModificateur));
    }

    @GetMapping("/en-attente-drh")
    @PreAuthorize("hasRole('DRH')")
    public ResponseEntity<GrilleTarifaireListeResponseDto> listerEnAttenteDrh() {
        GrilleTarifaireListeResponseDto reponse = GrilleTarifaireListeResponseDto.builder()
                .contenu(grilleTarifaireService.rechercher(null, StatutGrilleEnum.EN_ATTENTE_DRH))
                .build();
        return ResponseEntity.ok(reponse);
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasRole('DRH')")
    public ResponseEntity<GrilleTarifaireResponseDto> valider(@PathVariable Long id,
                                                                @Valid @RequestBody DecisionGrilleTarifaireRequestDto requete) {
        Long idValidateur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(grilleTarifaireService.validerOuRejeter(id, requete, idValidateur));
    }

    @GetMapping("/fonction/{code}")
    @PreAuthorize("hasAnyRole('ARH', 'DRH', 'ADMIN')")
    public ResponseEntity<HistoriqueGrilleTarifaireResponseDto> historique(@PathVariable String code) {
        return ResponseEntity.ok(grilleTarifaireService.historique(code));
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<GrilleTarifaireResponseDto> desactiver(@PathVariable Long id) {
        Long idActeur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(grilleTarifaireService.desactiver(id, idActeur));
    }
}