package com.afriland.dottel.service;

import com.afriland.dottel.model.dto.reporting.HistoriqueLigneDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueExportService {

    private static final String[] EN_TETES = {
            "Mois", "Année", "Statut", "Nombre de bénéficiaires", "Montant total (FCFA)"
    };

    private final ReportingService reportingService;

    @Transactional(readOnly = true)
    public byte[] exporter(Integer annee) {
        List<HistoriqueLigneDto> lignes = reportingService.historique(annee).getLignes();

        try (XSSFWorkbook classeur = new XSSFWorkbook()) {
            Sheet feuille = classeur.createSheet("Historique des processus");

            Row ligneEntete = feuille.createRow(0);
            for (int colonne = 0; colonne < EN_TETES.length; colonne++) {
                ligneEntete.createCell(colonne).setCellValue(EN_TETES[colonne]);
            }

            int numeroLigne = 1;
            for (HistoriqueLigneDto ligneHistorique : lignes) {
                Row ligne = feuille.createRow(numeroLigne);
                ligne.createCell(0).setCellValue(ligneHistorique.getMoisPaiement());
                ligne.createCell(1).setCellValue(ligneHistorique.getAnneePaiement());
                ligne.createCell(2).setCellValue(ligneHistorique.getStatut().name());
                ligne.createCell(3).setCellValue(ligneHistorique.getNombreBeneficiaires());
                ligne.createCell(4).setCellValue(ligneHistorique.getMontantTotal());
                numeroLigne++;
            }

            for (int colonne = 0; colonne < EN_TETES.length; colonne++) {
                feuille.autoSizeColumn(colonne);
            }

            ByteArrayOutputStream flux = new ByteArrayOutputStream();
            classeur.write(flux);
            return flux.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("Erreur lors de la génération de l'export Excel", exception);
        }
    }
}