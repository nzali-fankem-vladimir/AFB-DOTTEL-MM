package com.afriland.dottel.beneficiaires.model.dto.importexcel;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRapportDto {

    private int inseres;
    private int rejetes;
    private List<ImportErreurDto> erreurs;
}