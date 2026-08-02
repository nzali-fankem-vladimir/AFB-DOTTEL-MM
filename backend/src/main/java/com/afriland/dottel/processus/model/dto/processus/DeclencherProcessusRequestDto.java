package com.afriland.dottel.processus.model.dto.processus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeclencherProcessusRequestDto {

    @NotNull
    @Min(1)
    @Max(12)
    private Integer moisPaiement;

    @NotNull
    private Integer anneePaiement;
}