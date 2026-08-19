package com.afriland.dottel.referentiel.controller;

import com.afriland.dottel.referentiel.model.dto.grille.CreerGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.DecisionGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireListeResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.GrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.HistoriqueGrilleTarifaireResponseDto;
import com.afriland.dottel.referentiel.model.dto.grille.ModifierGrilleTarifaireRequestDto;
import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import com.afriland.dottel.utilisateurs.api.AuthenticatedUserService;
import com.afriland.dottel.referentiel.service.GrilleTarifaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grilles-tarifaires")
@RequiredArgsConstructor
public class GrilleTarifaireController {

    private final GrilleTarifaireService grilleTarifaireService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ARH', 'ADMIN')")
    public ResponseEntity<GrilleTarifaireListeResponseDto> lister(
            @RequestParam(required = false) String fonction,
            @RequestParam(required = false) StatutGrilleEnum statut) {
        GrilleTarifaireListeResponseDto reponse = GrilleTarifaireListeResponseDto.builder()
                .contenu(grilleTarifaireService.rechercher(fonction, statut))
                .build();
        return ResponseEntity.ok(reponse);
    }

    // Sprint MM.14 (ecart E3 de l'audit, tranche avec le metier) : ADMIN
    // RETIRE de la creation et de la modification. Fixer un montant de
    // dotation n'est pas un acte d'administration technique -- c'est une
    // decision metier qui part aussitot dans le circuit ARH -> CRH -> DRH
    // (RG-10, workflow a trois acteurs du Sprint MM.12), circuit dont
    // l'ADMIN n'est acteur d'aucune etape. Le laisser y injecter un montant
    // contredirait l'esprit de RG-08 (separation des taches).
    //
    // L'ADMIN CONSERVE : GET /grilles-tarifaires (lecture de la liste,
    // ci-dessus) et GET /grilles-tarifaires/fonction/{code} (historique).
    // Il conserve aussi POST /fonctions-eligibles, qui cree une fonction
    // NEUVE avec sa grille initiale directement ACTIVE -- chemin distinct,
    // interne au module referentiel, qui ne passe pas par ici (voir
    // FonctionEligibleService.creer(), decision metier du Sprint 6F.7bis).
    @PostMapping
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<GrilleTarifaireResponseDto> creer(@Valid @RequestBody CreerGrilleTarifaireRequestDto requete) {
        Long idCreateur = authenticatedUserService.utilisateurCourant().getId();
        GrilleTarifaireResponseDto reponse = grilleTarifaireService.creer(requete, idCreateur);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<GrilleTarifaireResponseDto> modifier(@PathVariable Long id,
                                                                 @Valid @RequestBody ModifierGrilleTarifaireRequestDto requete) {
        Long idModificateur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(grilleTarifaireService.modifier(id, requete, idModificateur));
    }

    // Sprint MM.12 : pendant strict de /en-attente-drh pour la premiere etape
    // du workflow a trois acteurs.
    @GetMapping("/en-attente-crh")
    @PreAuthorize("hasRole('CRH')")
    public ResponseEntity<GrilleTarifaireListeResponseDto> listerEnAttenteCrh() {
        GrilleTarifaireListeResponseDto reponse = GrilleTarifaireListeResponseDto.builder()
                .contenu(grilleTarifaireService.rechercher(null, StatutGrilleEnum.EN_ATTENTE_CRH))
                .build();
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/en-attente-drh")
    @PreAuthorize("hasRole('DRH')")
    public ResponseEntity<GrilleTarifaireListeResponseDto> listerEnAttenteDrh() {
        GrilleTarifaireListeResponseDto reponse = GrilleTarifaireListeResponseDto.builder()
                .contenu(grilleTarifaireService.rechercher(null, StatutGrilleEnum.EN_ATTENTE_DRH))
                .build();
        return ResponseEntity.ok(reponse);
    }

    // Sprint MM.12 : endpoint unique partage par le CRH et la DRH, la branche
    // etant choisie par le statut de la grille -- modele de
    // POST /processus/{id}/valider, qui sert deja trois roles. hasAnyRole ne
    // suffit donc PAS a garantir l'ordre des etapes : c'est
    // SeparationTachesGrilleService.verifierRoleAttendu(), en tete de chaque
    // branche du service, qui empeche un DRH de statuer sur une grille
    // EN_ATTENTE_CRH (403).
    @PostMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('CRH', 'DRH')")
    public ResponseEntity<GrilleTarifaireResponseDto> valider(@PathVariable Long id,
                                                                @Valid @RequestBody DecisionGrilleTarifaireRequestDto requete) {
        return ResponseEntity.ok(grilleTarifaireService.validerOuRejeter(id, requete,
                authenticatedUserService.utilisateurCourant()));
    }

    @GetMapping("/fonction/{code}")
    @PreAuthorize("hasAnyRole('ARH', 'DRH', 'ADMIN')")
    public ResponseEntity<HistoriqueGrilleTarifaireResponseDto> historique(@PathVariable String code) {
        return ResponseEntity.ok(grilleTarifaireService.historique(code));
    }

    @PostMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ARH')")
    public ResponseEntity<GrilleTarifaireResponseDto> desactiver(@PathVariable Long id) {
        Long idActeur = authenticatedUserService.utilisateurCourant().getId();
        return ResponseEntity.ok(grilleTarifaireService.desactiver(id, idActeur));
    }
}