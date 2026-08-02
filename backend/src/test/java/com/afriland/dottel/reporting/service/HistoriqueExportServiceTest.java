package com.afriland.dottel.reporting.service;

import com.afriland.dottel.reporting.model.dto.reporting.HistoriqueLigneDto;
import com.afriland.dottel.reporting.model.dto.reporting.HistoriqueResponseDto;
import com.afriland.dottel.processus.model.enums.StatutEnum;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoriqueExportServiceTest {

    @Mock
    private ReportingService reportingService;

    private HistoriqueExportService historiqueExportService;

    @BeforeEach
    void setUp() {
        historiqueExportService = new HistoriqueExportService(reportingService);
    }

    @Test
    void exporterHistorique_produitUnClasseurValide() throws Exception {
        HistoriqueLigneDto ligneJuin = HistoriqueLigneDto.builder()
                .moisPaiement(6)
                .anneePaiement(2026)
                .statut(StatutEnum.CLOTURE)
                .montantTotal(2_100_000L)
                .nombreBeneficiaires(28L)
                .build();

        when(reportingService.historique(2026))
                .thenReturn(HistoriqueResponseDto.builder().lignes(List.of(ligneJuin)).build());

        byte[] classeur = historiqueExportService.exporter(2026);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(classeur))) {
            Sheet feuille = workbook.getSheetAt(0);

            assertThat(feuille.getLastRowNum()).isEqualTo(1);

            Row entete = feuille.getRow(0);
            assertThat(entete.getCell(0).getStringCellValue()).isEqualTo("Mois");
            assertThat(entete.getCell(4).getStringCellValue()).isEqualTo("Montant total (FCFA)");

            Row ligne = feuille.getRow(1);
            assertThat(ligne.getCell(0).getNumericCellValue()).isEqualTo(6);
            assertThat(ligne.getCell(1).getNumericCellValue()).isEqualTo(2026);
            assertThat(ligne.getCell(2).getStringCellValue()).isEqualTo("CLOTURE");
            assertThat(ligne.getCell(3).getNumericCellValue()).isEqualTo(28);
            assertThat(ligne.getCell(4).getNumericCellValue()).isEqualTo(2_100_000);
        }
    }
}