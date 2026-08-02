package com.afriland.dottel.referentiel.model.dto.fonctioneligible;

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
public class FonctionEligibleResponseDto {

    private String code;
    private String libelle;
}