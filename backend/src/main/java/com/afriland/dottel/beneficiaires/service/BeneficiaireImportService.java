package com.afriland.dottel.beneficiaires.service;
import com.afriland.dottel.utilisateurs.service.AuthenticatedUserService;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.beneficiaires.exception.FichierImportInvalideException;
import com.afriland.dottel.beneficiaires.model.dto.importexcel.ImportErreurDto;
import com.afriland.dottel.beneficiaires.model.dto.importexcel.ImportRapportDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.referentiel.service.FonctionEligibleService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BeneficiaireImportService {

    private static final int COLONNE_MATRICULE = 1;
    private static final int COLONNE_NOMS_PRENOMS = 2;
    private static final int COLONNE_FONCTION = 3;
    private static final int COLONNE_UNITE = 4;
    private static final int COLONNE_CODE_UNITE = 5;
    private static final int COLONNE_NUM_COMPTE = 6;

    private static final String MOTIF_GRADE_NON_VERIFIABLE =
            "Grade non verifiable via import Excel - enrolement individuel requis pour les corps de controle";

    private static final Set<String> FONCTIONS_CORPS_CONTROLE_ET_ASSIMILES = Set.of(
            "CORPS_CONTROLE_IG", "CORPS_CONTROLE_IGA", "CONTROLEUR_GESTION",
            "CONTROLEUR_COMPTABLE", "COMPTABLE");

    private final BeneficiaireRepository beneficiaireRepository;
    private final FonctionEligibleService fonctionEligibleService;
    private final AuditService auditService;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public ImportRapportDto importer(MultipartFile fichier) {
        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();
        DataFormatter formateur = new DataFormatter();

        int inseres = 0;
        List<ImportErreurDto> erreurs = new ArrayList<>();

        try (InputStream fluxFichier = fichier.getInputStream();
             XSSFWorkbook classeur = new XSSFWorkbook(fluxFichier)) {

            Sheet feuille = classeur.getSheetAt(0);

            for (int indexLigne = 1; indexLigne <= feuille.getLastRowNum(); indexLigne++) {
                Row ligne = feuille.getRow(indexLigne);
                if (ligne == null) {
                    continue;
                }

                int numeroLigne = indexLigne + 1;
                String matricule = valeurCellule(ligne, COLONNE_MATRICULE, formateur);
                String nomPrenoms = valeurCellule(ligne, COLONNE_NOMS_PRENOMS, formateur);
                String fonction = valeurCellule(ligne, COLONNE_FONCTION, formateur);
                String unite = valeurCellule(ligne, COLONNE_UNITE, formateur);
                String codeUnite = valeurCellule(ligne, COLONNE_CODE_UNITE, formateur);
                String numCompteCourant = valeurCellule(ligne, COLONNE_NUM_COMPTE, formateur);

                String motifRejet = validerLigne(matricule, fonction);
                if (motifRejet != null) {
                    erreurs.add(ImportErreurDto.builder()
                            .ligne(numeroLigne)
                            .matricule(matricule.isEmpty() ? null : matricule)
                            .motif(motifRejet)
                            .build());
                    continue;
                }

                Beneficiaire beneficiaire = Beneficiaire.builder()
                        .matricule(matricule)
                        .nomPrenoms(nomPrenoms)
                        .fonction(fonction)
                        .uniteRattachement(unite)
                        .codeUnite(codeUnite)
                        .numCompteCourant(numCompteCourant)
                        .dateEnrolement(LocalDate.now())
                        .actif(true)
                        .build();

                beneficiaire = beneficiaireRepository.save(beneficiaire);
                inseres++;

                enregistrerAudit(utilisateurCourant, beneficiaire);
            }
        } catch (IOException exception) {
            throw new FichierImportInvalideException(
                    "Fichier illisible ou format incorrect (xlsx attendu)", exception);
        }

        return ImportRapportDto.builder()
                .inseres(inseres)
                .rejetes(erreurs.size())
                .erreurs(erreurs)
                .build();
    }

    private String validerLigne(String matricule, String fonction) {
        if (matricule.isEmpty()) {
            return "Champ MATRICULE manquant";
        }

        if (FONCTIONS_CORPS_CONTROLE_ET_ASSIMILES.contains(fonction)) {
            return MOTIF_GRADE_NON_VERIFIABLE;
        }

        if (beneficiaireRepository.existsByMatricule(matricule)) {
            return "Matricule déjà enrôlé";
        }

        Optional<Boolean> fonctionActive = fonctionEligibleService.estActive(fonction);
        if (fonctionActive.isEmpty()) {
            return "Fonction inconnue : " + fonction;
        }
        if (!fonctionActive.get()) {
            return "Fonction désactivée : " + fonction;
        }

        return null;
    }

    private String valeurCellule(Row ligne, int indexColonne, DataFormatter formateur) {
        return formateur.formatCellValue(ligne.getCell(indexColonne)).trim();
    }

    private void enregistrerAudit(Utilisateur utilisateurCourant, Beneficiaire beneficiaire) {
        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("matricule", beneficiaire.getMatricule());
        apres.put("nomPrenoms", beneficiaire.getNomPrenoms());
        apres.put("fonction", beneficiaire.getFonction());
        apres.put("uniteRattachement", beneficiaire.getUniteRattachement());
        apres.put("codeUnite", beneficiaire.getCodeUnite());
        apres.put("numCompteCourant", beneficiaire.getNumCompteCourant());
        apres.put("dateEnrolement", beneficiaire.getDateEnrolement().toString());

        auditService.enregistrer(utilisateurCourant.getId(), "IMPORT_BENEFICIAIRE", "beneficiaires",
                beneficiaire.getId(), null, apres);
    }
}