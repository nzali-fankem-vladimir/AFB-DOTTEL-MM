package com.afriland.dottel.utilisateurs.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank
    private String matricule;

    @NotBlank
    private String motDePasse;
}