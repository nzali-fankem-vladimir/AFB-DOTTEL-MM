package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
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
public class BeneficiaireExportService {

    private static final String[] EN_TETES = {
            "N° ordre", "Matricule", "Nom et prénoms", "Fonction",
            "Unité de rattachement", "Code unité", "Montant courant (FCFA)", "N° compte courant"
    };

    private final BeneficiaireRepository beneficiaireRepository;
    private final BeneficiaireService beneficiaireService;

    @Transactional(readOnly = true)
    public byte[] exporter() {
        List<Beneficiaire> beneficiairesActifs = beneficiaireRepository.findByActifTrue();

        try (XSSFWorkbook classeur = new XSSFWorkbook()) {
            Sheet feuille = classeur.createSheet("Bénéficiaires actifs");

            Row ligneEntete = feuille.createRow(0);
            for (int colonne = 0; colonne < EN_TETES.length; colonne++) {
                ligneEntete.createCell(colonne).setCellValue(EN_TETES[colonne]);
            }

            int numeroLigne = 1;
            for (Beneficiaire beneficiaire : beneficiairesActifs) {
                Row ligne = feuille.createRow(numeroLigne);
                ligne.createCell(0).setCellValue(numeroLigne);
                ligne.createCell(1).setCellValue(beneficiaire.getMatricule());
                ligne.createCell(2).setCellValue(beneficiaire.getNomPrenoms());
                ligne.createCell(3).setCellValue(beneficiaire.getFonction());
                ligne.createCell(4).setCellValue(beneficiaire.getUniteRattachement());
                ligne.createCell(5).setCellValue(beneficiaire.getCodeUnite());

                Integer montantCourant = beneficiaireService.resoudreMontantCourant(beneficiaire.getFonction());
                Cell celluleMontant = ligne.createCell(6);
                if (montantCourant != null) {
                    celluleMontant.setCellValue(montantCourant);
                }

                ligne.createCell(7).setCellValue(beneficiaire.getNumCompteCourant());

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