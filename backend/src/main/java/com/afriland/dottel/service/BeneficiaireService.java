package com.afriland.dottel.service;

import com.afriland.dottel.exception.BeneficiaireIntrouvableException;
import com.afriland.dottel.exception.NonEligibleException;
import com.afriland.dottel.model.dto.beneficiaire.BeneficiaireResponseDto;
import com.afriland.dottel.model.dto.beneficiaire.ModifierBeneficiaireRequestDto;
import com.afriland.dottel.model.entity.Beneficiaire;
import com.afriland.dottel.model.entity.FonctionEligible;
import com.afriland.dottel.model.entity.GrilleTarifaire;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.repository.BeneficiaireRepository;
import com.afriland.dottel.repository.BeneficiaireSpecifications;
import com.afriland.dottel.repository.FonctionEligibleRepository;
import com.afriland.dottel.repository.GrilleTarifaireRepository;
import com.afriland.dottel.service.rules.EligibiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BeneficiaireService {

    private final BeneficiaireRepository beneficiaireRepository;
    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final GrilleTarifaireRepository grilleTarifaireRepository;
    private final EligibiliteService eligibiliteService;
    private final AuditService auditService;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional(readOnly = true)
    public Page<BeneficiaireResponseDto> rechercher(String fonction, String uniteRattachement, Boolean actif,
                                                      Pageable pageable) {
        Page<Beneficiaire> page = beneficiaireRepository.findAll(
                BeneficiaireSpecifications.avecFiltres(fonction, uniteRattachement, actif), pageable);

        return page.map(this::versDto);
    }

    // RG-01 revalidee car l'ARH saisit la fonction directement (pas d'appel EHR
    // ici, contrairement a l'enrolement) -- decision explicite du Sprint 4.2 :
    // refuser plutot que de faire confiance a l'autorite de l'ARH.
    @Transactional
    public BeneficiaireResponseDto modifier(Long id, ModifierBeneficiaireRequestDto requete) {
        Beneficiaire beneficiaire = beneficiaireRepository.findById(id)
                .orElseThrow(() -> new BeneficiaireIntrouvableException("Aucun bénéficiaire avec l'id " + id));

        Map<String, Object> avant = new LinkedHashMap<>();
        Map<String, Object> apres = new LinkedHashMap<>();

        // requete.getGrade() est volontairement ignore : seul le grade deja stocke
        // sur le beneficiaire (issu de l'EHR a l'enrolement) fait foi pour RG-02.
        // Accepter un grade saisi par l'ARH permettrait de contourner RG-02 (ex. un
        // CONTROLEUR_COMPTABLE au grade reel NON GRADE pourrait etre requalifie avec
        // un faux grade pour passer la verification). Le champ reste dans le DTO
        // pour la conformite au contrat API, mais ne doit pas etre exploite ici.
        boolean fonctionModifiee = requete.getFonction() != null && !requete.getFonction().equals(beneficiaire.getFonction());

        if (fonctionModifiee) {
            String gradeActuel = beneficiaire.getGrade();

            if (!eligibiliteService.verifierEligibilite(requete.getFonction(), gradeActuel)) {
                throw new NonEligibleException(
                        "Fonction ou grade non éligible à la dotation téléphonique après modification");
            }

            avant.put("fonction", beneficiaire.getFonction());
            beneficiaire.setFonction(requete.getFonction());
            apres.put("fonction", beneficiaire.getFonction());
        }

        if (requete.getUniteRattachement() != null && !requete.getUniteRattachement().equals(beneficiaire.getUniteRattachement())) {
            avant.put("uniteRattachement", beneficiaire.getUniteRattachement());
            beneficiaire.setUniteRattachement(requete.getUniteRattachement());
            apres.put("uniteRattachement", beneficiaire.getUniteRattachement());
        }

        beneficiaireRepository.save(beneficiaire);

        if (!apres.isEmpty()) {
            Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();
            auditService.enregistrer(utilisateurCourant.getId(), "MODIFICATION_BENEFICIAIRE", "beneficiaires",
                    beneficiaire.getId(), avant, apres);
        }

        return versDto(beneficiaire);
    }

    @Transactional
    public void desactiver(Long id) {
        Beneficiaire beneficiaire = beneficiaireRepository.findById(id)
                .orElseThrow(() -> new BeneficiaireIntrouvableException("Aucun bénéficiaire avec l'id " + id));

        Map<String, Object> avant = Map.of("actif", beneficiaire.isActif());
        beneficiaire.setActif(false);
        beneficiaireRepository.save(beneficiaire);
        Map<String, Object> apres = Map.of("actif", beneficiaire.isActif());

        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();
        auditService.enregistrer(utilisateurCourant.getId(), "DESACTIVATION_BENEFICIAIRE", "beneficiaires",
                beneficiaire.getId(), avant, apres);
    }

    // Symetrique de desactiver(), mais revalide RG-01/RG-02 avant de reactiver :
    // la fonction/grade du beneficiaire a pu devenir non eligible (referentiel
    // desactive, grille supprimee) pendant qu'il etait inactif.
    @Transactional
    public BeneficiaireResponseDto reactiver(Long id) {
        Beneficiaire beneficiaire = beneficiaireRepository.findById(id)
                .orElseThrow(() -> new BeneficiaireIntrouvableException("Aucun bénéficiaire avec l'id " + id));

        if (!eligibiliteService.verifierEligibilite(beneficiaire.getFonction(), beneficiaire.getGrade())) {
            throw new NonEligibleException(
                    "Fonction ou grade non éligible à la dotation téléphonique, réactivation impossible");
        }

        Map<String, Object> avant = Map.of("actif", beneficiaire.isActif());
        beneficiaire.setActif(true);
        beneficiaireRepository.save(beneficiaire);
        Map<String, Object> apres = Map.of("actif", beneficiaire.isActif());

        Utilisateur utilisateurCourant = authenticatedUserService.utilisateurCourant();
        auditService.enregistrer(utilisateurCourant.getId(), "REACTIVATION_BENEFICIAIRE", "beneficiaires",
                beneficiaire.getId(), avant, apres);

        return versDto(beneficiaire);
    }

    private BeneficiaireResponseDto versDto(Beneficiaire beneficiaire) {
        return BeneficiaireResponseDto.builder()
                .id(beneficiaire.getId())
                .matricule(beneficiaire.getMatricule())
                .nomPrenoms(beneficiaire.getNomPrenoms())
                .fonction(beneficiaire.getFonction())
                .montantCourant(resoudreMontantCourant(beneficiaire.getFonction()))
                .uniteRattachement(beneficiaire.getUniteRattachement())
                .actif(beneficiaire.isActif())
                .build();
    }

    // RG-04 : montant toujours derive de grille_tarifaire ACTIVE, jamais stocke.
    // Absence de fonction eligible ou de grille active -> montant manquant (null),
    // n'empeche pas l'affichage du beneficiaire dans la liste.
    public Integer resoudreMontantCourant(String codeFonction) {
        Optional<FonctionEligible> fonctionEligible = fonctionEligibleRepository.findByCode(codeFonction);
        if (fonctionEligible.isEmpty()) {
            return null;
        }

        Optional<GrilleTarifaire> grilleActive = grilleTarifaireRepository
                .findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
                        fonctionEligible.get().getId(), StatutGrilleEnum.ACTIVE);

        return grilleActive.map(GrilleTarifaire::getMontantFcfa).orElse(null);
    }
}