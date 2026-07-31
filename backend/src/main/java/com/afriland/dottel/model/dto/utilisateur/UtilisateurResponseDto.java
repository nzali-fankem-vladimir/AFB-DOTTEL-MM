package com.afriland.dottel.model.dto.utilisateur;

import com.afriland.dottel.model.enums.RoleEnum;
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
public class UtilisateurResponseDto {

    private Long id;
    private String matricule;
    private String nomPrenoms;
    private RoleEnum role;
    private boolean actif;
}