package com.afriland.dottel.model.dto.reporting;

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
public class AuditLogPageResponseDto {

    private List<AuditLogResponseDto> contenu;
    private long total;
    private int page;
    private int taille;

    public static AuditLogPageResponseDto depuis(Page<AuditLogResponseDto> page) {
        return AuditLogPageResponseDto.builder()
                .contenu(page.getContent())
                .total(page.getTotalElements())
                .page(page.getNumber())
                .taille(page.getSize())
                .build();
    }
}