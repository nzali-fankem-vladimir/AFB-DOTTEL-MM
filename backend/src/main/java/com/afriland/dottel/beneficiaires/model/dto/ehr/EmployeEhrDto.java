package com.afriland.dottel.beneficiaires.model.dto.ehr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeEhrDto {

    private String matricule;
    private String nom;
    private String prenom;
    private String fonction;
    private String grade;
    private String uniteRattachement;
    private String codeUnite;
    private String numCompteCourant;
    private String chapitre;
}