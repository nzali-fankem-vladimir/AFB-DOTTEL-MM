package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.model.dto.ehr.EmployeEhrDto;
import com.afriland.dottel.beneficiaires.model.dto.ehr.UniteRattachementDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simulation de l'EHR (CLAUDE.md section 12), en attendance du branchement
 * reel. Toutes les donnees ci-dessous sont des donnees de stub, y compris
 * les formats code_unite (4 chiffres) et code_agence (5 chiffres) alignes
 * sur la realite de la banque au Sprint MM.10 : seuls DSI = 4060 (code
 * unite) et les 25 codes agence de la section 1.3 du guide MM.10 sont
 * CONFIRMES par le metier -- l'affectation par employe reste provisoire, a
 * remplacer le jour de l'integration EHR reelle.
 */
@Service
class EhrIntegrationServiceStub implements EhrIntegrationService {

    private final Map<String, EmployeEhrDto> employesEhr = new LinkedHashMap<>();

    /**
     * Table de correspondance unite <-> code unite (4 chiffres).
     * Seul "DSI" -> "4060" est CONFIRME par le metier. Tous les autres codes
     * sont PROVISOIRES (donnees de stub), a remplacer le jour de
     * l'integration EHR reelle.
     */
    private final Map<String, String> unitesRattachement = new LinkedHashMap<>();

    @PostConstruct
    private void initialiserDonnees() {
        initialiserUnitesRattachement();
        ajouter("1847", "MBARGA", "Jean-Paul", "GFC", null,
                "Agence Douala Akwa", "1102", "00002", "10011847002", "37210100");

        ajouter("2093", "ESSAMA", "Marie-Claire", "CHEF_DEPARTEMENT", null,
                "Direction Centrale Yaounde", "1001", "00001", "10012093005", "37210110");

        ajouter("3164", "NKOLO", "Emmanuel", "DA", null,
                "Agence Bafoussam Centre", "1305", "00003", "10013164008", "37210120");

        ajouter("4275", "ATANGANA", "Sylvie", "CONSEILLER", null,
                "Direction Regionale Garoua", "2004", "00004", "10014275003", "37210130");

        ajouter("5386", "TCHINDA", "Robert", "CHEF_ANTENNE", null,
                "Antenne Bertoua", "2508", "00008", "10015386007", "37210140");

        // Fonction totalement absente du referentiel fonction_eligible -> NON ELIGIBLE
        ajouter("6497", "FOUDA", "Alain", "AGENT_GUICHET", null,
                "Agence Ngaoundere", "2207", "00007", "10016497001", "37210150");

        // RG-02 : corps de controle assimile avec grade NON GRADE -> NON ELIGIBLE
        ajouter("7508", "BELINGA", "Christelle", "CONTROLEUR_COMPTABLE", "NON GRADE",
                "Direction Controle Douala", "3102", "00002", "10017508004", "37210160");

        // RG-02 : meme corps de controle mais avec un grade reel -> eligible
        ajouter("8619", "ONANA", "Patrice", "CONTROLEUR_COMPTABLE", "Grade 4",
                "Direction Controle Douala", "3102", "00002", "10018619006", "37210160");

        ajouter("9720", "NDONGO", "Beatrice", "CORPS_CONTROLE_IGA", "Grade 6",
                "Inspection Generale Yaounde", "3001", "00001", "10019720002", "37210170");

        ajouter("1053", "EYENGA", "Michel", "COMPTABLE", null,
                "Agence Buea", "1509", "00009", "10010053009", "37210180");
    }

    private void initialiserUnitesRattachement() {
        // "DSI" -> "4060" : seul code CONFIRME par le metier (2026-08-08).
        unitesRattachement.put("DSI", "4060");

        // Tout ce qui suit est PROVISOIRE -- code unite invente en 4 chiffres,
        // coherent avec les unites deja portees par les employes du stub.
        // A remplacer le jour de l'integration EHR reelle.
        unitesRattachement.put("Agence Douala Akwa", "1102");
        unitesRattachement.put("Direction Centrale Yaounde", "1001");
        unitesRattachement.put("Agence Bafoussam Centre", "1305");
        unitesRattachement.put("Direction Regionale Garoua", "2004");
        unitesRattachement.put("Antenne Bertoua", "2508");
        unitesRattachement.put("Agence Ngaoundere", "2207");
        unitesRattachement.put("Direction Controle Douala", "3102");
        unitesRattachement.put("Inspection Generale Yaounde", "3001");
        unitesRattachement.put("Agence Buea", "1509");
    }

    private void ajouter(String matricule, String nom, String prenom, String fonction,
                          String grade, String uniteRattachement, String codeUnite,
                          String codeAgence, String numCompteCourant, String chapitre) {
        employesEhr.put(matricule, EmployeEhrDto.builder()
                .matricule(matricule)
                .nom(nom)
                .prenom(prenom)
                .fonction(fonction)
                .grade(grade)
                .uniteRattachement(uniteRattachement)
                .codeUnite(codeUnite)
                .codeAgence(codeAgence)
                .numCompteCourant(numCompteCourant)
                .chapitre(chapitre)
                .build());
    }

    @Override
    public Optional<EmployeEhrDto> rechercherEmploye(String matricule) {
        return Optional.ofNullable(employesEhr.get(matricule));
    }

    @Override
    public List<UniteRattachementDto> listerUnitesRattachement() {
        return unitesRattachement.entrySet().stream()
                .map(entree -> UniteRattachementDto.builder()
                        .uniteRattachement(entree.getKey())
                        .codeUnite(entree.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}