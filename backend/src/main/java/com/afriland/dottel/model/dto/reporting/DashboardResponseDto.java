package com.afriland.dottel.model.dto.reporting;

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
public class DashboardResponseDto {

    private long nombreBeneficiairesActifs;
    private long montantTotalMensuel;
    private ProcessusEnCoursDto processusEnCours;
    private long processusClotureesCetteAnnee;
}