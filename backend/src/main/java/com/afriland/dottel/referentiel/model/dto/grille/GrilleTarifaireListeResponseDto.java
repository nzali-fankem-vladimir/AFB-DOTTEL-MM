package com.afriland.dottel.referentiel.model.dto.grille;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrilleTarifaireListeResponseDto {

    private List<GrilleTarifaireListeLigneDto> contenu;
}