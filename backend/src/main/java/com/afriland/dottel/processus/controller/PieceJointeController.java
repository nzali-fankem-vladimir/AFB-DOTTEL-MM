package com.afriland.dottel.processus.controller;

import com.afriland.dottel.processus.model.entity.PieceJointe;
import com.afriland.dottel.processus.service.ProcessusMensuelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pieces-jointes")
@RequiredArgsConstructor
public class PieceJointeController {

    private final ProcessusMensuelService processusMensuelService;

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ARH', 'CRH', 'DRH')")
    public ResponseEntity<Resource> telecharger(@PathVariable Long id) {
        PieceJointe pieceJointe = processusMensuelService.obtenirPieceJointePourTelechargement(id);
        Resource fichier = new FileSystemResource(pieceJointe.getCheminStockage());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(pieceJointe.getNomFichier()).build().toString())
                .body(fichier);
    }
}