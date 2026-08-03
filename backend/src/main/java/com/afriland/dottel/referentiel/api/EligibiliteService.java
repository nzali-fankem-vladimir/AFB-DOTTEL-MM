package com.afriland.dottel.referentiel.api;

import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EligibiliteService {

    private static final String NON_GRADE = "NON GRADE";

    private static final Set<String> CORPS_CONTROLE_ET_ASSIMILES = Set.of(
            "CORPS_CONTROLE_IG",
            "CORPS_CONTROLE_IGA",
            "CONTROLEUR_GESTION",
            "CONTROLEUR_COMPTABLE",
            "COMPTABLE"
    );

    private final FonctionEligibleRepository fonctionEligibleRepository;

    public boolean verifierEligibilite(String codeFonction, String grade) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(codeFonction).orElse(null);

        if (fonctionEligible == null || !fonctionEligible.isActif()) {
            return false;
        }

        if (CORPS_CONTROLE_ET_ASSIMILES.contains(codeFonction)) {
            // Grade absent/vide traite comme NON GRADE : un grade inconnu ne doit
            // jamais laisser passer un corps de controle (RG-02). Comparaison
            // insensible a la casse pour ne pas dependre de la normalisation de la
            // donnee source (EHR ou saisie).
            boolean gradeConnu = grade != null && !grade.isBlank() && !NON_GRADE.equalsIgnoreCase(grade.trim());
            if (!gradeConnu) {
                return false;
            }
        }

        return true;
    }
}