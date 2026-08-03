package com.afriland.dottel.audit.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {

    private Long id;
    private Long idUtilisateur;
    private String action;
    private String entiteCible;
    private Long idEntite;
    private LocalDateTime dateAction;
    private String adresseIp;
    private String detailJson;
}