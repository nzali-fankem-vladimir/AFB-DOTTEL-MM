package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.model.dto.ehr.EmployeEhrDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EhrIntegrationServiceStub implements EhrIntegrationService {

    private final Map<String, EmployeEhrDto> employesEhr = new HashMap<>();

    @PostConstruct
    private void initialiserDonnees() {
        ajouter("1847", "MBARGA", "Jean-Paul", "GFC", null,
                "Agence Douala Akwa", "DLA-AKW", "10011847002", "37210100");

        ajouter("2093", "ESSAMA", "Marie-Claire", "CHEF_DEPARTEMENT", null,
                "Direction Centrale Yaounde", "YDE-DC01", "10012093005", "37210110");

        ajouter("3164", "NKOLO", "Emmanuel", "DA", null,
                "Agence Bafoussam Centre", "BFS-CTR", "10013164008", "37210120");

        ajouter("4275", "ATANGANA", "Sylvie", "CONSEILLER", null,
                "Direction Regionale Garoua", "GRA-DR01", "10014275003", "37210130");

        ajouter("5386", "TCHINDA", "Robert", "CHEF_ANTENNE", null,
                "Antenne Bertoua", "BTA-ANT", "10015386007", "37210140");

        // Fonction totalement absente du referentiel fonction_eligible -> NON ELIGIBLE
        ajouter("6497", "FOUDA", "Alain", "AGENT_GUICHET", null,
                "Agence Ngaoundere", "NGD-AG01", "10016497001", "37210150");

        // RG-02 : corps de controle assimile avec grade NON GRADE -> NON ELIGIBLE
        ajouter("7508", "BELINGA", "Christelle", "CONTROLEUR_COMPTABLE", "NON GRADE",
                "Direction Controle Douala", "DLA-CTL", "10017508004", "37210160");

        // RG-02 : meme corps de controle mais avec un grade reel -> eligible
        ajouter("8619", "ONANA", "Patrice", "CONTROLEUR_COMPTABLE", "Grade 4",
                "Direction Controle Douala", "DLA-CTL", "10018619006", "37210160");

        ajouter("9720", "NDONGO", "Beatrice", "CORPS_CONTROLE_IGA", "Grade 6",
                "Inspection Generale Yaounde", "YDE-IG01", "10019720002", "37210170");

        ajouter("1053", "EYENGA", "Michel", "COMPTABLE", null,
                "Agence Buea", "BUE-AG01", "10010053009", "37210180");
    }

    private void ajouter(String matricule, String nom, String prenom, String fonction,
                          String grade, String uniteRattachement, String codeUnite,
                          String numCompteCourant, String chapitre) {
        employesEhr.put(matricule, EmployeEhrDto.builder()
                .matricule(matricule)
                .nom(nom)
                .prenom(prenom)
                .fonction(fonction)
                .grade(grade)
                .uniteRattachement(uniteRattachement)
                .codeUnite(codeUnite)
                .numCompteCourant(numCompteCourant)
                .chapitre(chapitre)
                .build());
    }

    @Override
    public Optional<EmployeEhrDto> rechercherEmploye(String matricule) {
        return Optional.ofNullable(employesEhr.get(matricule));
    }
}