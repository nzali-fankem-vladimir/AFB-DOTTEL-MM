package com.afriland.dottel.referentiel.service;

import com.afriland.dottel.referentiel.exception.RoleDecisionGrilleNonAutoriseException;
import com.afriland.dottel.referentiel.exception.SeparationTachesGrilleViolationException;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Garde-fous d'autorisation du workflow des grilles tarifaires (Sprint MM.12),
 * tous deux traduits en 403 par GlobalExceptionHandler :
 * <ul>
 *   <li>{@link #verifierRoleAttendu} (RG-05) : le role de l'acteur correspond
 *       bien a l'etape declenchee par le statut de la grille.</li>
 *   <li>{@link #verifier} (RG-08) : l'acteur n'est pas celui qui a statue sur
 *       l'etape precedente.</li>
 * </ul>
 *
 * <h2>Pourquoi ce service existe plutot que SeparationTachesService</h2>
 *
 * Option W-2 arbitree le 2026-08-09 (MM.12 section 1bis). SeparationTachesService,
 * EtapeWorkflow et EtapeWorkflowRepository appartiennent au module
 * {@code processus}, qui depend deja de {@code referentiel :: api} depuis MM.2.
 * Les reutiliser ici ferait dependre {@code referentiel} de {@code processus} et
 * creerait un SECOND cycle de modules -- ModularityTests, dont le filtre ne
 * couvre que le cycle {@code beneficiaires <-> referentiel} assume par la
 * decision G-2, echouerait. C'est sa raison d'etre.
 *
 * <h2>Ce n'est pas une duplication d'algorithme</h2>
 *
 * Les deux implementations resolvent la meme regle sur des structures de
 * donnees differentes, et ne peuvent pas converger :
 * <ul>
 *   <li>{@code processus} : EtapeWorkflow est append-only avec cycles
 *       retour/resoumission, il faut filtrer sur VALIDEE et retenir la plus
 *       recente par dateAction (anomalie corrigee en MM.8).</li>
 *   <li>{@code referentiel} : REJETEE est TERMINAL -- une grille rejetee n'est
 *       jamais resoumise, l'ARH en cree une nouvelle. Le decideur precedent est
 *       donc un champ unique, et RG-08 se reduit a une egalite.</li>
 * </ul>
 * Le risque de divergence evoque par le guide porte donc sur une regle de deux
 * lignes, pas sur une logique partagee.
 */
@Service
public class SeparationTachesGrilleService {

    private static final Map<StatutGrilleEnum, RoleEnum> ROLE_ATTENDU_PAR_STATUT = Map.of(
            StatutGrilleEnum.EN_ATTENTE_CRH, RoleEnum.CRH,
            StatutGrilleEnum.EN_ATTENTE_DRH, RoleEnum.DRH);

    // RG-05 : POST /grilles-tarifaires/{id}/valider porte hasAnyRole('CRH','DRH')
    // et la branche est choisie par le statut de la grille, pas par le role de
    // l'appelant. Sans ce controle, une DRH statuerait sur une grille
    // EN_ATTENTE_CRH -- l'etape CRH serait purement et simplement sautee.
    // A appeler AVANT verifier() (RG-08) pour que le motif du 403 soit le bon.
    public void verifierRoleAttendu(StatutGrilleEnum statutGrille, Utilisateur acteurCourant) {
        RoleEnum roleAttendu = ROLE_ATTENDU_PAR_STATUT.get(statutGrille);

        if (acteurCourant.getRole() != roleAttendu) {
            throw new RoleDecisionGrilleNonAutoriseException(
                    "Une grille au statut " + statutGrille + " doit être traitée par un utilisateur de rôle "
                            + roleAttendu + " (rôle de l'acteur courant : " + acteurCourant.getRole() + ")");
        }
    }

    // RG-08. idDecideurPrecedent null n'est PAS une violation : le cas se
    // presente pour les grilles heritees du circuit a deux acteurs, laissees en
    // EN_ATTENTE_DRH par la migration V7 (decision du 2026-08-09) et donc sans
    // decideur CRH. Bloquer ces grilles les rendrait definitivement
    // invalidables. Population finie et transitoire.
    public void verifier(Long idDecideurPrecedent, Long idActeurCourant, String libelleEtapePrecedente) {
        if (idDecideurPrecedent != null && idDecideurPrecedent.equals(idActeurCourant)) {
            throw new SeparationTachesGrilleViolationException(
                    "L'acteur de l'étape " + libelleEtapePrecedente
                            + " ne peut pas statuer sur l'étape suivante de la même grille tarifaire");
        }
    }
}
