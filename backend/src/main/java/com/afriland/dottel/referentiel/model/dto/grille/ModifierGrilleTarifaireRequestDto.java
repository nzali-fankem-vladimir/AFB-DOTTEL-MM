package com.afriland.dottel.referentiel.model.dto.grille;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifierGrilleTarifaireRequestDto {

    @NotNull
    @Positive
    private Integer montantFcfa;
}