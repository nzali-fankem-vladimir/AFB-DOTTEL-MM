package com.afriland.dottel.referentiel.repository;

import com.afriland.dottel.referentiel.model.entity.GrilleTarifaire;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GrilleTarifaireRepository extends JpaRepository<GrilleTarifaire, Long> {

    Optional<GrilleTarifaire> findByIdFonctionEligibleAndStatutValidationAndDateFinIsNull(
            Long idFonctionEligible, StatutGrilleEnum statutValidation);

    // Sprint MM.12 : le workflow a trois acteurs cree DEUX statuts d'attente
    // (EN_ATTENTE_CRH, EN_ATTENTE_DRH). Le garde-fou "une seule grille en vol
    // par fonction" de creer() doit les couvrir tous les deux -- la variante
    // mono-statut ci-dessus laisserait creer une grille EN_ATTENTE_CRH alors
    // qu'une autre attend deja la DRH.
    boolean existsByIdFonctionEligibleAndStatutValidationIn(
            Long idFonctionEligible, Collection<StatutGrilleEnum> statutsValidation);

    // Sprint MM.12 : alimente GrilleTarifaireApi.grilleEnAttentePourFonction().
    // creer() garantit qu'il y en a au plus une, mais le tri rend la methode
    // deterministe si un jeu de donnees anterieur en contenait plusieurs.
    Optional<GrilleTarifaire> findFirstByIdFonctionEligibleAndStatutValidationInOrderByDateCreationDesc(
            Long idFonctionEligible, Collection<StatutGrilleEnum> statutsValidation);

    List<GrilleTarifaire> findByIdFonctionEligibleOrderByDateDebutDesc(Long idFonctionEligible);
}