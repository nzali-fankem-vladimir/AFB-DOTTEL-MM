package com.afriland.dottel.model.dto.grille;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreerGrilleTarifaireRequestDto {

    @NotBlank
    private String codeFonction;

    @NotNull
    @Positive
    private Integer montantFcfa;

    @NotNull
    private LocalDate dateDebut;
}