package com.afriland.dottel.model.dto.processus;

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
public class PieceJointeMetadonneesResponseDto {

    private Long id;
    private String nomFichier;
    private LocalDateTime dateGenerationInitiale;
    private LocalDateTime dateDerniereMiseAJour;
    private Integer nombreSignatures;
}