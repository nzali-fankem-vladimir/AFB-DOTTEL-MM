package com.afriland.dottel.model.dto.fonctioneligible;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Les deux champs sont optionnels (patch partiel) : libelle est toujours
// modifiable, nouveauCode ne l'est que si aucun beneficiaire actif n'est
// rattache a la fonction (verifie dans FonctionEligibleService.modifier()).
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifierFonctionEligibleRequestDto {

    private String nouveauCode;
    private String libelle;
}
