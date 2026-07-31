package com.afriland.dottel.service;

import com.afriland.dottel.exception.GrilleTarifaireIntrouvableException;
import com.afriland.dottel.exception.MatriculeDejaEnroleException;
import com.afriland.dottel.exception.MatriculeInconnuException;
import com.afriland.dottel.exception.NonEligibleException;
import com.afriland.dottel.model.dto.ehr.EmployeEhrDto;
import com.afriland.dottel.model.dto.enrolement.ConfirmerEnrolementRequestDto;
import com.afriland.dottel.model.dto.enrolement.ConfirmerEnrolementResponseDto;
import com.afriland.dottel.model.dto.enrolement.EnrolementVerificationResponseDto;
import com.afriland.dottel.model.entity.Beneficiaire;
import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.service.rules.EligibiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrolementService {

    private final BeneficiaireRepository beneficiaireRepository;
    private final EhrIntegrationService ehrIntegrationService;
    private final EligibiliteService eligibiliteService;
    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final GrilleTarifaireRepository grilleTarifaireRepository;
    private final AuditService auditService;
    private final AuthenticatedUserService authenticatedUserService;

    public EnrolementVerificationResponseDto verifier(String matricule) {
        if (beneficiaireRepository.existsByMatricule(matricule)) {
            throw new MatriculeDejaEnroleException("Déjà enrolé");
        }

        EmployeEhrDto employeEhr = ehrIntegrationService.rechercherEmploye(matricule)
                .orElseThrow(() -> new MatriculeInconnuException("Matricule inconnu de l'EHR"));

        boolean eligible = eligibiliteService.verifierEligibilite(employeEhr.getFonction(), employeEhr.getGrade());

        String libelleFonction = fonctionEligibleRepository.findByCode(employeEhr.getFonction())
                .map(FonctionEligible::getLibelle)
                .orElse(null);

        return EnrolementVerificationResponseDto.builder()
                .matricule(employeEhr.getMatricule())
                .nom(employeEhr.getNom())
                .prenom(employeEhr.getPrenom())
                .fonction(employeEhr.getFonction())
                .libelleFonction(libelleFonction)
                .grade(employeEhr.getGrade())
                .uniteRattachement(employeEhr.getUniteRattachement())
                .codeUnite(employeEhr.getCodeUnite())
                .numCompteCourant(employeEhr.getNumCompteCourant())
                .eligible(eligible)
                .build();
    }

    @Transactional
    public ConfirmerEnrolementResponseDto confirmer(ConfirmerEnrolementRequestDto requeteConfirmation) {
        String matricule = requeteConfirmation.getMatricule();

        if (beneficiaireRepository.existsByMatricule(matricule)) {
            throw new MatriculeDejaEnroleException("Déjà enrolé");
        }

        EmployeEhrDto employeEhr = ehrIntegrationService.rechercherEmploye(matricule)
                .orElseThrow(() -> new MatriculeInconnuException("Matricule inconnu de l'EHR"));

        // requeteConfirmation.getGrade() est volontairement ignoré : seul le grade
        // retourné par l'EHR fait foi pour RG-01/RG-02. Accepter un grade saisi par
        // l'employé permettrait de contourner RG-02 (ex. un CONTROLEUR_COMPTABLE au
        // grade réel NON GRADE pourrait déclarer un faux grade pour passer la
        // vérification). Le champ reste dans le DTO pour la conformité au contrat
        // API, mais ne doit pas être exploité dans cette logique.
        String grade = employeEhr.getGrade();

        boolean eligible = eligibiliteService.verifierEligibilite(employeEhr.getFonction(), grade);
        if (!eligible) {
            throw new NonEligibleException("Fonction ou grade non éligible à la dotation téléphonique");
        }

        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(employeEhr.getFonction())
                .orElseThrow(() -> new NonEligibleException("Fonction inconnue du référentiel des fonctions éligibles"));

        grilleTarifaireRepository.findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                        fonctionEligible.getId(), StatutGrilleEnum.ACTIVE)
                .orElseThrow(() -> new GrilleTarifaireIntrouvableException(
                        "Aucune grille tarifaire ACTIVE pour la fonction " + employeEhr.getFonction()));

        Beneficiaire beneficiaire = Beneficiaire.builder()
                .matricule(employeEhr.getMatricule())
                .nomPrenoms(employeEhr.getNom() + " " + employeEhr.getPrenom())
                .fonction(employeEhr.getFonction())
                .grade(grade)
                .uniteRattachement(employeEhr.getUniteRattachement())
                .codeUnite(employeEhr.getCodeUnite())
                .numCompteCourant(employeEhr.getNumCompteCourant())
                .chapitre(employeEhr.getChapitre())
                .dateEnrolement(LocalDate.now())
                .actif(true)
                .build();

        beneficiaire = beneficiaireRepository.save(beneficiaire);

        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("matricule", beneficiaire.getMatricule());
        apres.put("nomPrenoms", beneficiaire.getNomPrenoms());
        apres.put("fonction", beneficiaire.getFonction());
        apres.put("grade", beneficiaire.getGrade());
        apres.put("uniteRattachement", beneficiaire.getUniteRattachement());
        apres.put("codeUnite", beneficiaire.getCodeUnite());
        apres.put("numCompteCourant", beneficiaire.getNumCompteCourant());
        apres.put("chapitre", beneficiaire.getChapitre());
        apres.put("dateEnrolement", beneficiaire.getDateEnrolement().toString());

        auditService.enregistrer(utilisateurCourant.getId(), "ENROLEMENT", "beneficiaires", beneficiaire.getId(),
                null, apres);

        return ConfirmerEnrolementResponseDto.builder()
                .id(beneficiaire.getId())
                .matricule(beneficiaire.getMatricule())
                .nomPrenoms(beneficiaire.getNomPrenoms())
                .fonction(beneficiaire.getFonction())
                .dateEnrolement(beneficiaire.getDateEnrolement())
                .build();
    }
}