package com.afriland.dottel.beneficiaires.model.dto.beneficiaire;

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
public class ModifierBeneficiaireRequestDto {

    private String fonction;
    private String grade;
    private String uniteRattachement;
}