package com.afriland.dottel.beneficiaires.model.dto.importexcel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportErreurDto {

    private int ligne;
    private String matricule;
    private String motif;
}