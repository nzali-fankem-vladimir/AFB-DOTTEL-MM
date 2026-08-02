package com.afriland.dottel.referentiel.controller;

import com.afriland.dottel.referentiel.model.dto.fonctioneligible.CreerFonctionEligibleRequestDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.FonctionEligibleAdminResponseDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.FonctionEligibleResponseDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.ModifierFonctionEligibleRequestDto;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.referentiel.service.FonctionEligibleService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fonctions-eligibles")
@RequiredArgsConstructor
public class FonctionEligibleController {

    private final FonctionEligibleService fonctionEligibleService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ARH', 'DRH', 'ADMIN')")
    public ResponseEntity<List<FonctionEligibleResponseDto>> lister() {
        return ResponseEntity.ok(fonctionEligibleService.listerActives());
    }

    @GetMapping("/toutes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FonctionEligibleAdminResponseDto>> listerToutes() {
        return ResponseEntity.ok(fonctionEligibleService.listerToutes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FonctionEligibleAdminResponseDto> creer(
            @Valid @RequestBody CreerFonctionEligibleRequestDto requete) {
        Long idCreateur = authenticatedUserService.utilisateurCourant().getId();
        FonctionEligibleAdminResponseDto reponse = fonctionEligibleService.creer(requete, idCreateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PatchMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FonctionEligibleAdminResponseDto> modifier(
            @PathVariable String code, @RequestBody ModifierFonctionEligibleRequestDto requete) {
        Long idActeur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(fonctionEligibleService.modifier(code, requete, idActeur));
    }

    @PatchMapping("/{code}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FonctionEligibleAdminResponseDto> desactiver(@PathVariable String code) {
        Long idActeur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(fonctionEligibleService.desactiver(code, idActeur));
    }

    @PatchMapping("/{code}/reactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FonctionEligibleAdminResponseDto> reactiver(@PathVariable String code) {
        Long idActeur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(fonctionEligibleService.reactiver(code, idActeur));
    }
}