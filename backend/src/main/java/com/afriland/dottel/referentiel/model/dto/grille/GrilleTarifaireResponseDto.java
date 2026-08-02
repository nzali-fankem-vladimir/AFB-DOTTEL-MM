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
public class GrilleTarifaireResponseDto {

    private Long id;
    private String codeFonction;
    private Integer montantFcfa;
    private LocalDate dateDebut;
    private StatutGrilleEnum statutValidation;
}