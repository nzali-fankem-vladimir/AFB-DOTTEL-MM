package com.afriland.dottel.referentiel.model.dto.grille;

import com.afriland.dottel.referentiel.model.enums.StatutGrilleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrilleTarifaireListeLigneDto {

    private Long id;
    private String codeFonction;
    private String libelleFonction;
    private Integer montantFcfa;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutGrilleEnum statutValidation;
    private String motifRejet;

    // Sprint MM.12 : "CRH" ou "DRH" selon l'etage qui a rejete, null si la
    // grille n'est pas REJETEE. Derive cote service, jamais stocke -- equivalent
    // d'origineRetour pour le processus mensuel.
    private String origineRejet;
}