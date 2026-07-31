package com.afriland.dottel.model.dto.enrolement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrolementVerificationResponseDto {

    private String matricule;
    private String nom;
    private String prenom;
    private String fonction;
    private String libelleFonction;
    private String grade;
    private String uniteRattachement;
    private String codeUnite;
    private String numCompteCourant;
    private boolean eligible;
}