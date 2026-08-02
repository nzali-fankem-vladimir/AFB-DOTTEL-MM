package com.afriland.dottel.beneficiaires.model.dto.enrolement;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmerEnrolementRequestDto {

    @NotBlank
    private String matricule;

    private String grade;
}