package com.afriland.dottel.beneficiaires.service;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.beneficiaires.model.dto.importexcel.ImportRapportDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.referentiel.service.FonctionEligibleService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaireImportServiceTest {

    @Mock
    private BeneficiaireRepository beneficiaireRepository;

    @Mock
    private FonctionEligibleService fonctionEligibleService;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private BeneficiaireImportService beneficiaireImportService;

    // 1. toutesLignesValides
    @Test
    void importer_toutesLignesValides_insereChaqueLigneEtNeRejeteRien() throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());
        when(fonctionEligibleService.estActive("DA")).thenReturn(Optional.of(true));
        when(beneficiaireRepository.existsByMatricule("8210")).thenReturn(false);
        when(beneficiaireRepository.existsByMatricule("8211")).thenReturn(false);
        when(beneficiaireRepository.save(any(Beneficiaire.class))).thenAnswer(this::simulerSauvegarde);

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "8210", "TCHINDA Robert", "DA", "Agence Douala Akwa", "DLA-AKW", "10018210003"},
                new String[]{"2", "8211", "NDONGO Patricia", "DA", "Agence Douala Akwa", "DLA-AKW", "10018211004"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(2);
        assertThat(rapport.getRejetes()).isEqualTo(0);
        assertThat(rapport.getErreurs()).isEmpty();
        verify(beneficiaireRepository, times(2)).save(any(Beneficiaire.class));
    }

    // 2. matriculeDejaEnrole
    @Test
    void importer_matriculeDejaEnrole_rejeteAvecMotifMatriculeDejaEnrole() throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());
        when(beneficiaireRepository.existsByMatricule("9999")).thenReturn(true);

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "9999", "MBARGA Joseph", "DA", "Agence Bertoua", "BTA-AG01", "10019999002"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(0);
        assertThat(rapport.getRejetes()).isEqualTo(1);
        assertThat(rapport.getErreurs().get(0).getMotif()).isEqualTo("Matricule déjà enrôlé");
        verify(beneficiaireRepository, never()).save(any());
    }

    // 3. fonctionInconnue
    @Test
    void importer_fonctionInconnue_rejeteAvecMotifFonctionInconnue() throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());
        when(beneficiaireRepository.existsByMatricule("8420")).thenReturn(false);
        when(fonctionEligibleService.estActive("STAGIAIRE")).thenReturn(Optional.empty());

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "8420", "ESSAMA Bertrand", "STAGIAIRE", "Agence Buea", "BUE-AG01", "10018420005"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(0);
        assertThat(rapport.getRejetes()).isEqualTo(1);
        assertThat(rapport.getErreurs().get(0).getMotif()).isEqualTo("Fonction inconnue : STAGIAIRE");
        verify(beneficiaireRepository, never()).save(any());
    }

    // 4. melangeValideEtInvalide
    @Test
    void importer_melangeValideEtInvalide_insereLesLignesValidesEtRejetteLesAutresSansInterrompreLaBoucle()
            throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());
        when(fonctionEligibleService.estActive("DA")).thenReturn(Optional.of(true));
        when(fonctionEligibleService.estActive("STAGIAIRE")).thenReturn(Optional.empty());
        when(beneficiaireRepository.existsByMatricule("8210")).thenReturn(false);
        when(beneficiaireRepository.existsByMatricule("8211")).thenReturn(false);
        when(beneficiaireRepository.existsByMatricule("8420")).thenReturn(false);
        when(beneficiaireRepository.save(any(Beneficiaire.class))).thenAnswer(this::simulerSauvegarde);

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "8210", "TCHINDA Robert", "DA", "Agence Douala Akwa", "DLA-AKW", "10018210003"},
                new String[]{"2", "8420", "ESSAMA Bertrand", "STAGIAIRE", "Agence Buea", "BUE-AG01", "10018420005"},
                new String[]{"3", "8211", "NDONGO Patricia", "DA", "Agence Douala Akwa", "DLA-AKW", "10018211004"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(2);
        assertThat(rapport.getRejetes()).isEqualTo(1);
        assertThat(rapport.getErreurs()).hasSize(1);
        assertThat(rapport.getErreurs().get(0).getLigne()).isEqualTo(3);
        assertThat(rapport.getErreurs().get(0).getMotif()).isEqualTo("Fonction inconnue : STAGIAIRE");
        verify(beneficiaireRepository, times(2)).save(any(Beneficiaire.class));
    }

    // 5. champMatriculeManquant
    @Test
    void importer_champMatriculeManquant_rejeteAvecMotifChampMatriculeManquantEtMatriculeNull() throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "", "NKOLO Emmanuel", "DA", "Agence Bafoussam Centre", "BFS-CTR", "10013164008"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(0);
        assertThat(rapport.getRejetes()).isEqualTo(1);
        assertThat(rapport.getErreurs().get(0).getMatricule()).isNull();
        assertThat(rapport.getErreurs().get(0).getMotif()).isEqualTo("Champ MATRICULE manquant");
        verify(beneficiaireRepository, never()).save(any());
        verify(beneficiaireRepository, never()).existsByMatricule(any());
    }

    // 6. corps de contrôle et assimilés (RG-02 non vérifiable via import Excel)
    @Test
    void importer_ligneCorpsControleEtAssimiles_estRejeteeAvecMotifGradeNonVerifiable() throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());
        when(fonctionEligibleService.estActive("DA")).thenReturn(Optional.of(true));
        when(beneficiaireRepository.existsByMatricule("8210")).thenReturn(false);
        when(beneficiaireRepository.save(any(Beneficiaire.class))).thenAnswer(this::simulerSauvegarde);

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "9312", "ATANGANA Sylvie", "CONTROLEUR_GESTION", "Direction Controle Yaounde", "YDE-CTL", "10019312007"},
                new String[]{"2", "8210", "TCHINDA Robert", "DA", "Agence Douala Akwa", "DLA-AKW", "10018210003"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(1);
        assertThat(rapport.getRejetes()).isEqualTo(1);
        assertThat(rapport.getErreurs()).hasSize(1);
        assertThat(rapport.getErreurs().get(0).getLigne()).isEqualTo(2);
        assertThat(rapport.getErreurs().get(0).getMatricule()).isEqualTo("9312");
        assertThat(rapport.getErreurs().get(0).getMotif())
                .isEqualTo("Grade non verifiable via import Excel - enrolement individuel requis pour les corps de controle");

        verify(beneficiaireRepository, never()).existsByMatricule("9312");
    }

    // 7. fonction desactivee (RG-01 : actif=false doit etre refuse comme une fonction inconnue,
    // mais avec un motif distinct pour l'ARH qui lit le rapport)
    @Test
    void importer_fonctionDesactivee_rejeteeAvecMotifFonctionDesactivee() throws IOException {
        when(authenticatedUserService.utilisateurCourant()).thenReturn(utilisateurConnecte());
        when(fonctionEligibleService.estActive("JURISTE")).thenReturn(Optional.of(false));

        MockMultipartFile fichier = fichierExcel(
                new String[]{"1", "8500", "FOUDA Christian", "JURISTE", "Agence Ngaoundéré", "NGA-AG01", "10018500009"});

        ImportRapportDto rapport = beneficiaireImportService.importer(fichier);

        assertThat(rapport.getInseres()).isEqualTo(0);
        assertThat(rapport.getRejetes()).isEqualTo(1);
        assertThat(rapport.getErreurs().get(0).getMotif()).isEqualTo("Fonction désactivée : JURISTE");
        verify(beneficiaireRepository, never()).save(any());
    }

    private Utilisateur utilisateurConnecte() {
        return Utilisateur.builder().id(7L).matricule("2001").build();
    }

    private Beneficiaire simulerSauvegarde(org.mockito.invocation.InvocationOnMock invocation) {
        Beneficiaire beneficiaire = invocation.getArgument(0);
        beneficiaire.setId(200L);
        return beneficiaire;
    }

    private MockMultipartFile fichierExcel(String[]... lignes) throws IOException {
        return new MockMultipartFile(
                "fichier", "import.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                construireClasseurExcel(lignes));
    }

    private byte[] construireClasseurExcel(String[]... lignes) throws IOException {
        try (XSSFWorkbook classeur = new XSSFWorkbook();
             ByteArrayOutputStream flux = new ByteArrayOutputStream()) {
            Sheet feuille = classeur.createSheet("Beneficiaires");

            Row entete = feuille.createRow(0);
            String[] colonnes = {"N°ORDRE", "MATRICULE", "NOMS & PRENOMS", "FONCTION", "UNITE", "CODE_UNITE", "N°COMPTE"};
            for (int i = 0; i < colonnes.length; i++) {
                entete.createCell(i).setCellValue(colonnes[i]);
            }

            for (int i = 0; i < lignes.length; i++) {
                Row ligne = feuille.createRow(i + 1);
                ecrireLigne(ligne, lignes[i]);
            }

            classeur.write(flux);
            return flux.toByteArray();
        }
    }

    private void ecrireLigne(Row ligne, String... valeurs) {
        for (int i = 0; i < valeurs.length; i++) {
            ligne.createCell(i).setCellValue(valeurs[i]);
        }
    }
}