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

    // Sprint MM.11 : false (defaut) pour un declenchement normal, true pour
    // un rattrapage d'une periode deja traitee. Pas de @NotNull -- un client
    // qui omet le champ obtient le comportement normal historique.
    private Boolean rattrapage = false;
}