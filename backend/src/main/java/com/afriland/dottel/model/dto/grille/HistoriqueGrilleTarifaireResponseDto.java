package com.afriland.dottel.model.dto.grille;

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
public class HistoriqueGrilleTarifaireResponseDto {

    private String codeFonction;
    private String libelleFonction;
    private List<GrilleHistoriqueLigneDto> grilles;
}