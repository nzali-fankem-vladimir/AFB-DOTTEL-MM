package com.afriland.dottel.referentiel.service;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.referentiel.exception.FonctionEligibleBeneficiairesActifsException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleCodeDejaUtiliseException;
import com.afriland.dottel.referentiel.exception.FonctionEligibleIntrouvableException;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.CreerFonctionEligibleRequestDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.FonctionEligibleAdminResponseDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.FonctionEligibleResponseDto;
import com.afriland.dottel.referentiel.model.dto.fonctioneligible.ModifierFonctionEligibleRequestDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.referentiel.model.entity.FonctionEligible;
import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import com.afriland.dottel.referentiel.repository.FonctionEligibleRepository;
import com.afriland.dottel.referentiel.repository.GrilleTarifaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FonctionEligibleService {

    private final FonctionEligibleRepository fonctionEligibleRepository;
    private final GrilleTarifaireRepository grilleTarifaireRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<FonctionEligibleResponseDto> listerActives() {
        return fonctionEligibleRepository.findByActifTrueOrderByLibelleAsc().stream()
                .map(fonctionEligible -> FonctionEligibleResponseDto.builder()
                        .code(fonctionEligible.getCode())
                        .libelle(fonctionEligible.getLibelle())
                        .build())
                .toList();
    }

    // Vue ADMIN complete (actives + inactives), avec le nombre de beneficiaires
    // actifs rattaches -- volume interne limite (25 fonctions eligibles), meme
    // decision de non-pagination que pour GrilleTarifaireService.rechercher().
    @Transactional(readOnly = true)
    public List<FonctionEligibleAdminResponseDto> listerToutes() {
        return fonctionEligibleRepository.findAllByOrderByLibelleAsc().stream()
                .map(fonctionEligible -> FonctionEligibleAdminResponseDto.builder()
                        .code(fonctionEligible.getCode())
                        .libelle(fonctionEligible.getLibelle())
                        .actif(fonctionEligible.isActif())
                        .nombreBeneficiairesActifs(
                                beneficiaireRepository.countByFonctionAndActifTrue(fonctionEligible.getCode()))
                        .build())
                .toList();
    }

    // Ecart cahier des charges II.1.7 comble au Sprint 6F.7bis : jusqu'ici
    // fonction_eligible n'etait alimente que par Flyway V2. Decision actee avec
    // le metier (section 3 du guide) : la grille tarifaire initiale est
    // obligatoire a la creation et statut ACTIVE directement -- creation ADMIN,
    // pas un remplacement passant par le workflow ARH/DRH (RG-10).
    @Transactional
    public FonctionEligibleAdminResponseDto creer(CreerFonctionEligibleRequestDto requete, Long idCreateur) {
        fonctionEligibleRepository.findByCode(requete.getCode())
                .ifPresent(fonction -> {
                    throw new FonctionEligibleCodeDejaUtiliseException(
                            "Une fonction éligible avec le code " + requete.getCode() + " existe déjà");
                });

        FonctionEligible fonctionEligible = FonctionEligible.builder()
                .code(requete.getCode())
                .libelle(requete.getLibelle())
                .actif(true)
                .build();
        fonctionEligibleRepository.save(fonctionEligible);

        GrilleTarifaire grille = GrilleTarifaire.builder()
                .idFonctionEligible(fonctionEligible.getId())
                .montantFcfa(requete.getMontantFcfa())
                .dateDebut(requete.getDateDebut())
                .statutValidation(StatutGrilleEnum.ACTIVE)
                .idCreateur(idCreateur)
                .idValidateur(null)
                .dateCreation(LocalDateTime.now())
                .build();
        grilleTarifaireRepository.save(grille);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("code", fonctionEligible.getCode());
        apres.put("libelle", fonctionEligible.getLibelle());
        apres.put("montantFcfa", grille.getMontantFcfa());
        apres.put("dateDebut", grille.getDateDebut());

        auditService.enregistrer(idCreateur, "CREATION_FONCTION_ELIGIBLE", "fonction_eligible",
                fonctionEligible.getId(), null, apres);

        return FonctionEligibleAdminResponseDto.builder()
                .code(fonctionEligible.getCode())
                .libelle(fonctionEligible.getLibelle())
                .actif(true)
                .nombreBeneficiairesActifs(0)
                .build();
    }

    // Decision actee avec le metier (section 3 du guide) : la desactivation est
    // autorisee meme si des beneficiaires actifs sont rattaches -- l'avertissement
    // est affiche cote frontend avant confirmation, et le nombre exact de
    // beneficiaires concernes au moment de l'action est trace dans l'audit.
    // RG-01 s'applique immediatement pour tout nouvel enrolement/ajustement.
    @Transactional
    public FonctionEligibleAdminResponseDto desactiver(String code, Long idActeur) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(code)
                .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                        "Aucune fonction éligible avec le code " + code));

        long beneficiairesActifsConcernes = beneficiaireRepository.countByFonctionAndActifTrue(code);

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("actif", fonctionEligible.isActif());

        fonctionEligible.setActif(false);
        fonctionEligibleRepository.save(fonctionEligible);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("actif", false);
        apres.put("beneficiairesActifsConcernes", beneficiairesActifsConcernes);

        auditService.enregistrer(idActeur, "DESACTIVATION_FONCTION_ELIGIBLE", "fonction_eligible",
                fonctionEligible.getId(), avant, apres);

        return FonctionEligibleAdminResponseDto.builder()
                .code(fonctionEligible.getCode())
                .libelle(fonctionEligible.getLibelle())
                .actif(false)
                .nombreBeneficiairesActifs(beneficiairesActifsConcernes)
                .build();
    }

    // Pas de revalidation RG-02 ici (contrairement a la reactivation d'un
    // beneficiaire) -- reactiver une fonction ne concerne aucun grade
    // individuel. RG-04 reprend son cours naturel selon l'etat de la grille.
    @Transactional
    public FonctionEligibleAdminResponseDto reactiver(String code, Long idActeur) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(code)
                .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                        "Aucune fonction éligible avec le code " + code));

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("actif", fonctionEligible.isActif());

        fonctionEligible.setActif(true);
        fonctionEligibleRepository.save(fonctionEligible);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("actif", true);

        auditService.enregistrer(idActeur, "REACTIVATION_FONCTION_ELIGIBLE", "fonction_eligible",
                fonctionEligible.getId(), avant, apres);

        return FonctionEligibleAdminResponseDto.builder()
                .code(fonctionEligible.getCode())
                .libelle(fonctionEligible.getLibelle())
                .actif(true)
                .nombreBeneficiairesActifs(beneficiaireRepository.countByFonctionAndActifTrue(code))
                .build();
    }

    // Ecart trouve manuellement Sprint 6F.7bis : jusqu'ici aucun moyen de
    // corriger une faute de frappe sur code/libelle apres creation. Decision
    // actee avec le metier : libelle est toujours modifiable (pur affichage,
    // aucune reference ailleurs) ; code ne l'est que si au plus 1 beneficiaire
    // actif est rattache (beneficiaires.fonction et
    // ligne_etat_mensuel.fonction_retenue sont des VARCHAR, pas des FK -- pas
    // de mise a jour automatique). Cascade volontairement limitee a
    // beneficiaires.fonction (y compris les beneficiaires inactifs restants et
    // l'eventuel beneficiaire actif tolere sous le seuil, pour eviter une
    // reference orpheline) : les lignes de ligne_etat_mensuel ne sont jamais
    // touchees, qu'elles appartiennent a un processus cloture (instantane
    // historique volontairement fige, section 4 CLAUDE.md) ou en cours -- la
    // fonction_retenue d'un etat mensuel deja genere ne doit pas etre
    // reinterpretee retroactivement par un renommage.
    @Transactional
    public FonctionEligibleAdminResponseDto modifier(String code, ModifierFonctionEligibleRequestDto requete,
                                                       Long idActeur) {
        FonctionEligible fonctionEligible = fonctionEligibleRepository.findByCode(code)
                .orElseThrow(() -> new FonctionEligibleIntrouvableException(
                        "Aucune fonction éligible avec le code " + code));

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("code", fonctionEligible.getCode());
        avant.put("libelle", fonctionEligible.getLibelle());

        if (requete.getLibelle() != null && !requete.getLibelle().isBlank()) {
            fonctionEligible.setLibelle(requete.getLibelle());
        }

        String codeFinal = fonctionEligible.getCode();
        if (requete.getNouveauCode() != null && !requete.getNouveauCode().equals(fonctionEligible.getCode())) {
            long beneficiairesActifs = beneficiaireRepository.countByFonctionAndActifTrue(code);
            if (beneficiairesActifs > 1) {
                throw new FonctionEligibleBeneficiairesActifsException(
                        "Impossible de modifier le code : " + beneficiairesActifs
                                + " bénéficiaire(s) actif(s) rattaché(s) à la fonction " + code);
            }

            fonctionEligibleRepository.findByCode(requete.getNouveauCode())
                    .ifPresent(existante -> {
                        throw new FonctionEligibleCodeDejaUtiliseException(
                                "Une fonction éligible avec le code " + requete.getNouveauCode() + " existe déjà");
                    });

            List<Beneficiaire> beneficiairesInactifsRattaches = beneficiaireRepository.findByFonction(code);
            beneficiairesInactifsRattaches.forEach(beneficiaire -> beneficiaire.setFonction(requete.getNouveauCode()));
            beneficiaireRepository.saveAll(beneficiairesInactifsRattaches);

            fonctionEligible.setCode(requete.getNouveauCode());
            codeFinal = requete.getNouveauCode();
        }

        fonctionEligibleRepository.save(fonctionEligible);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("code", fonctionEligible.getCode());
        apres.put("libelle", fonctionEligible.getLibelle());

        auditService.enregistrer(idActeur, "MODIFICATION_FONCTION_ELIGIBLE", "fonction_eligible",
                fonctionEligible.getId(), avant, apres);

        return FonctionEligibleAdminResponseDto.builder()
                .code(fonctionEligible.getCode())
                .libelle(fonctionEligible.getLibelle())
                .actif(fonctionEligible.isActif())
                .nombreBeneficiairesActifs(beneficiaireRepository.countByFonctionAndActifTrue(codeFinal))
                .build();
    }
}