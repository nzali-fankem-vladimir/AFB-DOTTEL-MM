package com.afriland.dottel.model.dto.enrolement;

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