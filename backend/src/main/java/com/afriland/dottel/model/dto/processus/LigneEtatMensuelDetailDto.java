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
public class LigneEtatMensuelDetailDto {

    private Long idBeneficiaire;
    private String matricule;
    private String nomPrenoms;
    private String fonctionRetenue;
    private Integer montantApplique;
    private Boolean inclusDansEtat;
}