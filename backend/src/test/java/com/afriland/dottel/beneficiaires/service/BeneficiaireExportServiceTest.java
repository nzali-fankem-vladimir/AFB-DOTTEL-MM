package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaireExportServiceTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private BeneficiaireService beneficiaireService;

    private BeneficiaireExportService beneficiaireExportService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        beneficiaireExportService = new BeneficiaireExportService(beneficiaireRepository, beneficiaireService);
    }

    private Beneficiaire creerBeneficiaire(String matricule, String nomPrenoms, String fonction) {
        return Beneficiaire.builder()
                .id(1L)
                .matricule(matricule)
                .nomPrenoms(nomPrenoms)
                .fonction(fonction)
                .uniteRattachement("Agence Bonanjo")
                .codeUnite("BNJ")
                .numCompteCourant("11012345678901")
                .dateEnrolement(LocalDate.of(2026, 1, 15))
                .actif(true)
                .build();
    }

    @Test
    void exporter_produitUnClasseurAvecUneLigneParBeneficiaireActif() throws Exception {
        Beneficiaire sylvie = creerBeneficiaire("2093", "Sylvie NKOLO", "GFC");
        Beneficiaire jean = creerBeneficiaire("2094", "Jean ESSAMA", "COMPTABLE");

        when(beneficiaireRepository.findByActifTrue()).thenReturn(List.of(sylvie, jean));
        when(beneficiaireService.resoudreMontantCourant("GFC")).thenReturn(40000);
        when(beneficiaireService.resoudreMontantCourant("COMPTABLE")).thenReturn(35000);

        byte[] classeur = beneficiaireExportService.exporter();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(classeur))) {
            Sheet feuille = workbook.getSheetAt(0);

            assertThat(feuille.getLastRowNum()).isEqualTo(2);

            Row entete = feuille.getRow(0);
            assertThat(entete.getCell(1).getStringCellValue()).isEqualTo("Matricule");

            Row ligneSylvie = feuille.getRow(1);
            assertThat(ligneSylvie.getCell(0).getNumericCellValue()).isEqualTo(1);
            assertThat(ligneSylvie.getCell(1).getStringCellValue()).isEqualTo("2093");
            assertThat(ligneSylvie.getCell(2).getStringCellValue()).isEqualTo("Sylvie NKOLO");
            assertThat(ligneSylvie.getCell(6).getNumericCellValue()).isEqualTo(40000);

            Row ligneJean = feuille.getRow(2);
            assertThat(ligneJean.getCell(1).getStringCellValue()).isEqualTo("2094");
            assertThat(ligneJean.getCell(6).getNumericCellValue()).isEqualTo(35000);
        }
    }

    @Test
    void exporter_beneficiaireInactifAbsentDeLexport() throws Exception {
        Beneficiaire marie = creerBeneficiaire("2095", "Marie FOUDA", "GFC");

        when(beneficiaireRepository.findByActifTrue()).thenReturn(List.of(marie));
        when(beneficiaireService.resoudreMontantCourant("GFC")).thenReturn(40000);

        byte[] classeur = beneficiaireExportService.exporter();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(classeur))) {
            Sheet feuille = workbook.getSheetAt(0);

            assertThat(feuille.getLastRowNum()).isEqualTo(1);
            assertThat(feuille.getRow(1).getCell(1).getStringCellValue()).isEqualTo("2095");
        }
    }
}