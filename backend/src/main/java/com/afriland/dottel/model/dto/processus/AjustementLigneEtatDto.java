package com.afriland.dottel.model.dto.processus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjustementLigneEtatDto {

    @NotNull
    private Long idBeneficiaire;

    private Boolean inclusDansEtat;

    private String fonctionRetenue;
}