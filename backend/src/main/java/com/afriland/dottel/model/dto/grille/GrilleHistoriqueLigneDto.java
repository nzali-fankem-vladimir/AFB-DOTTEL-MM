package com.afriland.dottel.model.dto.grille;

import com.afriland.dottel.model.enums.StatutGrilleEnum;
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
}