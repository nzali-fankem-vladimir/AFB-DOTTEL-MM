package com.afriland.dottel.processus.model.dto.processus;

import com.afriland.dottel.processus.model.enums.StatutEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValiderProcessusResponseDto {

    private Long id;
    private StatutEnum statut;
    private String etapeValidee;
    private Long idPieceJointe;
}