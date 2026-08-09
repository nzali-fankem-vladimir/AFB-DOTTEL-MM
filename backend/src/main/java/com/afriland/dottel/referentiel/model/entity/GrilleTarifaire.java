package com.afriland.dottel.referentiel.model.entity;

import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "grille_tarifaire")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrilleTarifaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_fonction_eligible", nullable = false)
    private Long idFonctionEligible;

    @Column(name = "montant_fcfa", nullable = false)
    private Integer montantFcfa;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false)
    private StatutGrilleEnum statutValidation;

    @Column(name = "id_createur")
    private Long idCreateur;

    // Sprint MM.12 : decision CRH, premiere etape du workflow a trois acteurs.
    // Renseigne a la VALIDATION COMME AU REJET -- d'ou "decideur" plutot que
    // "validateur". La derivation de l'origine d'un rejet (CRH ou DRH) repose
    // sur cet invariant ; le renommer sans le respecter la casserait
    // silencieusement. Voir migration V7 pour le motif complet du nommage.
    @Column(name = "id_decideur_crh")
    private Long idDecideurCrh;

    @Column(name = "date_decision_crh")
    private LocalDateTime dateDecisionCrh;

    // Decision DRH, seconde etape. Nom historique : ces deux champs sont eux
    // aussi renseignes au rejet, malgre "validateur"/"validation".
    @Column(name = "id_validateur")
    private Long idValidateur;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;
}