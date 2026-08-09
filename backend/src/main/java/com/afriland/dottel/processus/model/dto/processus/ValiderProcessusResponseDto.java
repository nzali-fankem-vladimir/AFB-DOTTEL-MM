package com.afriland.dottel.processus.model.dto.processus;

import com.afriland.dottel.processus.model.enums.StatutEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    // Sprint MM.12 : recapitulatif de ce qui a REELLEMENT ete applique a la
    // validation ARH (vide ou null pour les branches CRH et DRH, qui ne
    // resynchronisent rien). Reprend les deux categories separees de
    // EcartsMontantsResponseDto : l'ARH doit retrouver apres coup, a
    // l'identique, ce qu'il a confirme avant.
    private List<LigneResynchroniseeDto> lignesResynchronisees;
    private List<LigneExclueResynchronisationDto> lignesExclues;
}