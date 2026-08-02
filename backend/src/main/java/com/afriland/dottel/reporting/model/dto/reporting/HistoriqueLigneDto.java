package com.afriland.dottel.reporting.model.dto.reporting;

import com.afriland.dottel.processus.model.enums.StatutEnum;
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
public class HistoriqueLigneDto {

    private Integer moisPaiement;
    private Integer anneePaiement;
    private StatutEnum statut;
    private long montantTotal;
    private long nombreBeneficiaires;
}