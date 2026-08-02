package com.afriland.dottel.processus.model.dto.processus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PatchProcessusRequestDto {

    @NotEmpty
    @Valid
    private List<AjustementLigneEtatDto> ajustements;
}