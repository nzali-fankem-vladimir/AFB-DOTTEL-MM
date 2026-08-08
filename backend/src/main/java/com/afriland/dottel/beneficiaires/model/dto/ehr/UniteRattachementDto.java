package com.afriland.dottel.beneficiaires.model.dto.ehr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniteRattachementDto {

    private String uniteRattachement;
    private String codeUnite;
}
