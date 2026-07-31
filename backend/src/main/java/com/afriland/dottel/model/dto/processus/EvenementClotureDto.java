package com.afriland.dottel.model.dto.processus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvenementClotureDto {

    private Long idProcessus;
    private Integer moisPaiement;
    private Integer anneePaiement;
    private long montantTotal;
    private LocalDateTime dateCloture;
}