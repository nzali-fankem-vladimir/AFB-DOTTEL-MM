package com.afriland.dottel.model.dto.processus;

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
public class ResultatAjustementDto {

    private Long idBeneficiaire;
    private Boolean applique;
    private String motifRejet;
}