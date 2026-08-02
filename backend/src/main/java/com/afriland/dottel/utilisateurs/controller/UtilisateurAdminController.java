package com.afriland.dottel.utilisateurs.controller;

import com.afriland.dottel.utilisateurs.model.dto.utilisateur.ChangerRoleRequestDto;
import com.afriland.dottel.utilisateurs.model.dto.utilisateur.ChangerStatutRequestDto;
import com.afriland.dottel.utilisateurs.model.dto.utilisateur.CreerUtilisateurRequestDto;
import com.afriland.dottel.utilisateurs.model.dto.utilisateur.UtilisateurListeResponseDto;
import com.afriland.dottel.utilisateurs.model.dto.utilisateur.UtilisateurResponseDto;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.utilisateurs.service.UtilisateurAdminService;
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
@RequestMapping("/admin/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurAdminController {

    private final UtilisateurAdminService utilisateurAdminService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DRH')")
    public ResponseEntity<UtilisateurListeResponseDto> lister(@RequestParam(required = false) RoleEnum role,
                                                                @RequestParam(required = false) Boolean actif) {
        UtilisateurListeResponseDto reponse = UtilisateurListeResponseDto.builder()
                .contenu(utilisateurAdminService.rechercher(role, actif))
                .build();
        return ResponseEntity.ok(reponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDto> creer(@Valid @RequestBody CreerUtilisateurRequestDto requete) {
        Long idCreateur = authenticatedUserService.utilisateurCourant().getId();
        UtilisateurResponseDto reponse = utilisateurAdminService.creer(requete, idCreateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDto> changerStatut(@PathVariable Long id,
                                                                  @Valid @RequestBody ChangerStatutRequestDto requete) {
        Long idAdminConnecte = authenticatedUserService.utilisateurCourant().getId();
        UtilisateurResponseDto reponse = utilisateurAdminService.changerStatut(id, requete.getActif(), idAdminConnecte);
        return ResponseEntity.ok(reponse);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDto> changerRole(@PathVariable Long id,
                                                                @Valid @RequestBody ChangerRoleRequestDto requete) {
        Long idAdminConnecte = authenticatedUserService.utilisateurCourant().getId();
        UtilisateurResponseDto reponse = utilisateurAdminService.changerRole(id, requete.getRole(), idAdminConnecte);
        return ResponseEntity.ok(reponse);
    }
}