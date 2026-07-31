package com.afriland.dottel.model.dto.grille;

import jakarta.validation.constraints.NotBlank;
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
public class DecisionGrilleTarifaireRequestDto {

    @NotBlank
    private String decision;

    private String motifRejet;
}