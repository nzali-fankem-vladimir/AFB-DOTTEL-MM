package com.afriland.dottel.service;

import com.afriland.dottel.exception.EtapeWorkflowIntrouvableException;
import com.afriland.dottel.exception.RoleEtapeNonAutoriseException;
import com.afriland.dottel.exception.SeparationTachesViolationException;
import com.afriland.dottel.model.entity.EtapeWorkflow;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.NomEtapeEnum;
import com.afriland.dottel.model.enums.RoleEnum;
import com.afriland.dottel.repository.EtapeWorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Garde-fous d'autorisation du workflow de validation, tous deux traduits en
 * 403 par GlobalExceptionHandler :
 * <ul>
 *   <li>{@link #verifierRoleAttendu} (RG-05) : le rôle de l'acteur correspond
 *       bien à l'étape déclenchée par le statut du processus.</li>
 *   <li>{@link #verifier} (RG-08) : l'acteur n'est pas celui qui a validé
 *       l'étape précédente.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SeparationTachesService {

    private static final Map<NomEtapeEnum, RoleEnum> ROLE_ATTENDU_PAR_ETAPE = Map.of(
            NomEtapeEnum.VALIDATION_ARH, RoleEnum.ARH,
            NomEtapeEnum.VALIDATION_CRH, RoleEnum.CRH,
            NomEtapeEnum.VALIDATION_DRH, RoleEnum.DRH);

    private final EtapeWorkflowRepository etapeWorkflowRepository;

    // RG-05 : la branche de validation est choisie par le statut du processus,
    // pas par le role de l'appelant. Sans ce controle, le @PreAuthorize du
    // controleur (hasAnyRole ARH/CRH/DRH) laisse un CRH ou un DRH executer la
    // branche ARH d'un processus EN_COURS_ARH ou RETOURNE -- l'ordre ARH puis
    // CRH puis DRH ne serait plus garanti. A appeler en tete de chaque branche,
    // avant verifier() (RG-08), pour que le motif du 403 soit le bon.
    public void verifierRoleAttendu(NomEtapeEnum etapeDeclenchee, Utilisateur acteurCourant) {
        RoleEnum roleAttendu = ROLE_ATTENDU_PAR_ETAPE.get(etapeDeclenchee);

        if (acteurCourant.getRole() != roleAttendu) {
            throw new RoleEtapeNonAutoriseException(
                    "L'étape " + etapeDeclenchee + " doit être validée par un utilisateur de rôle "
                            + roleAttendu + " (rôle de l'acteur courant : " + acteurCourant.getRole() + ")");
        }
    }

    public void verifier(Long idProcessus, Long idActeurCourant, NomEtapeEnum etapePrecedente) {
        List<EtapeWorkflow> etapes = etapeWorkflowRepository.findByIdProcessusOrderByOrdreEtapeAsc(idProcessus);

        EtapeWorkflow etapeAnterieure = etapes.stream()
                .filter(etape -> etape.getNomEtape() == etapePrecedente)
                .findFirst()
                .orElseThrow(() -> new EtapeWorkflowIntrouvableException(
                        "Aucune étape " + etapePrecedente + " trouvée pour le processus " + idProcessus));

        if (etapeAnterieure.getIdActeur().equals(idActeurCourant)) {
            throw new SeparationTachesViolationException(
                    "L'acteur de l'étape " + etapePrecedente + " ne peut pas valider l'étape suivante du même processus");
        }
    }
}