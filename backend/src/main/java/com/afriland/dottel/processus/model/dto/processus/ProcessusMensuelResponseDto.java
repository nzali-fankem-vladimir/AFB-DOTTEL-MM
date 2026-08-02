package com.afriland.dottel.processus.model.dto.processus;

import com.afriland.dottel.processus.model.enums.StatutEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessusMensuelResponseDto {

    private Long id;
    private Integer moisPaiement;
    private Integer anneePaiement;
    private StatutEnum statut;
    private LocalDateTime dateCreation;
    private Integer nombreBeneficiaires;
    private List<BeneficiaireExcluDto> beneficiairesExclus;
}