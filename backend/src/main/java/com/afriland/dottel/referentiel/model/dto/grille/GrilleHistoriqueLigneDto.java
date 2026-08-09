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
public class GrilleHistoriqueLigneDto {

    private Long id;
    private Integer montantFcfa;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutGrilleEnum statutValidation;
    private String motifRejet;

    // Sprint MM.12 : "CRH" ou "DRH" selon l'etage qui a rejete, null si la
    // grille n'est pas REJETEE (voir GrilleTarifaireService.origineRejet).
    private String origineRejet;
}