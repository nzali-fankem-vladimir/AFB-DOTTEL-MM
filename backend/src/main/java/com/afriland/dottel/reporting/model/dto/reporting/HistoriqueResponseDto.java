package com.afriland.dottel.reporting.model.dto.reporting;

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
public class HistoriqueResponseDto {

    private List<HistoriqueLigneDto> lignes;
}