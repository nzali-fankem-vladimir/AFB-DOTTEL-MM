package com.afriland.dottel.model.dto.beneficiaire;

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
public class BeneficiaireResponseDto {

    private Long id;
    private String matricule;
    private String nomPrenoms;
    private String fonction;
    private Integer montantCourant;
    private String uniteRattachement;
    private boolean actif;
}