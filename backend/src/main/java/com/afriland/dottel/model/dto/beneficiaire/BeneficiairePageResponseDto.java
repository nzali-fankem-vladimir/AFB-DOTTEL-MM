package com.afriland.dottel.model.dto.beneficiaire;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiairePageResponseDto {

    private List<BeneficiaireResponseDto> contenu;
    private long total;
    private int page;
    private int taille;

    public static BeneficiairePageResponseDto depuis(Page<BeneficiaireResponseDto> page) {
        return BeneficiairePageResponseDto.builder()
                .contenu(page.getContent())
                .total(page.getTotalElements())
                .page(page.getNumber())
                .taille(page.getSize())
                .build();
    }
}