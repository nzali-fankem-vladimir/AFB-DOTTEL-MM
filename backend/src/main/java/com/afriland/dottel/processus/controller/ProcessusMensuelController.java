package com.afriland.dottel.processus.controller;

import com.afriland.dottel.processus.model.dto.processus.DeclencherProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.PatchProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.PatchProcessusResponseDto;
import com.afriland.dottel.processus.model.dto.processus.PieceJointeMetadonneesResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ProcessusDetailResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ProcessusListItemDto;
import com.afriland.dottel.processus.model.dto.processus.ProcessusMensuelResponseDto;
import com.afriland.dottel.processus.model.dto.processus.RetournerProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.RetournerProcessusResponseDto;
import com.afriland.dottel.processus.model.dto.processus.ValiderProcessusRequestDto;
import com.afriland.dottel.processus.model.dto.processus.ValiderProcessusResponseDto;
import com.afriland.dottel.processus.model.dto.processus.EcartsMontantsResponseDto;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import com.afriland.dottel.processus.service.ProcessusMensuelService;
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

import java.util.List;

@RestController
@RequestMapping("/processus")
@RequiredArgsConstructor
public class ProcessusMensuelController {

    private final ProcessusMensuelService processusMensuelService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ARH', 'CRH', 'DRH')")
    public ResponseEntity<List<ProcessusListItemDto>> lister(@RequestParam(required = false) StatutEnum statut,
                                                               @RequestParam(required = false) Integer annee) {
        return ResponseEntity.ok(processusMensuelService.lister(statut, annee));
    }

    @PostMapping("/declencher")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<ProcessusMensuelResponseDto> declencher(
            @Valid @RequestBody DeclencherProcessusRequestDto requeteDeclenchement) {
        ProcessusMensuelResponseDto reponse = processusMensuelService.declencher(requeteDeclenchement);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARH', 'CRH', 'DRH')")
    public ResponseEntity<ProcessusDetailResponseDto> consulterDetail(@PathVariable Long id) {
        ProcessusDetailResponseDto reponse = processusMensuelService.consulterDetail(id);
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/{id}/piece-jointe")
    @PreAuthorize("hasAnyRole('ARH', 'CRH', 'DRH')")
    public ResponseEntity<PieceJointeMetadonneesResponseDto> obtenirMetadonneesPieceJointe(@PathVariable Long id) {
        PieceJointeMetadonneesResponseDto reponse = processusMensuelService.obtenirMetadonneesPieceJointe(id);
        return ResponseEntity.ok(reponse);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<PatchProcessusResponseDto> ajuster(
            @PathVariable Long id,
            @Valid @RequestBody PatchProcessusRequestDto requeteAjustement) {
        PatchProcessusResponseDto reponse = processusMensuelService.ajuster(id, requeteAjustement);
        return ResponseEntity.ok(reponse);
    }

    // Sprint MM.12, premier temps de la variante B2-RESYNC : l'ARH consulte les
    // ecarts de montant AVANT de confirmer la validation. Lecture pure, aucune
    // ecriture -- l'ARH doit pouvoir renoncer apres avoir lu le recapitulatif.
    // Reserve a l'ARH : seule sa branche de validation resynchronise.
    @GetMapping("/{id}/ecarts-montants")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<EcartsMontantsResponseDto> detecterEcartsMontants(@PathVariable Long id) {
        return ResponseEntity.ok(processusMensuelService.detecterEcartsMontants(id));
    }

    // Un seul endpoint pour les trois roles ; le branchement reel (quel statut
    // accepte quel role) est fait par ProcessusMensuelService.valider() selon
    // le statut courant du processus (contrat API section 8).
    //
    // confirmerResynchronisation (Sprint MM.12) : second temps de B2-RESYNC.
    // Sans lui, une validation ARH portant des montants obsoletes est REFUSEE
    // (409) plutot que resynchronisee silencieusement. Sans effet sur les
    // branches CRH et DRH, qui ne resynchronisent rien.
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('ARH', 'CRH', 'DRH')")
    public ResponseEntity<ValiderProcessusResponseDto> valider(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean confirmerResynchronisation,
            @RequestBody(required = false) ValiderProcessusRequestDto requeteValidation) {
        String commentaire = requeteValidation != null ? requeteValidation.getCommentaire() : null;
        ValiderProcessusResponseDto reponse =
                processusMensuelService.valider(id, commentaire, confirmerResynchronisation);
        return ResponseEntity.ok(reponse);
    }

    // US-14 : le CRH ou la DRH retourne le processus a l'ARH avec un motif
    // obligatoire (RG-07), depuis EN_ATTENTE_CRH ou EN_ATTENTE_DRH.
    @PostMapping("/{id}/retourner")
    @PreAuthorize("hasAnyRole('CRH', 'DRH')")
    public ResponseEntity<RetournerProcessusResponseDto> retourner(
            @PathVariable Long id,
            @Valid @RequestBody RetournerProcessusRequestDto requeteRetour) {
        RetournerProcessusResponseDto reponse = processusMensuelService.retourner(id, requeteRetour);
        return ResponseEntity.ok(reponse);
    }
}