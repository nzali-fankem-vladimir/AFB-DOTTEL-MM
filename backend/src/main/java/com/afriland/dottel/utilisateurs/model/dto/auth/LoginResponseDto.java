package com.afriland.dottel.utilisateurs.model.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDto {

    private String token;
    private String matricule;
    private String role;
    private String nom;
    private String prenom;
}