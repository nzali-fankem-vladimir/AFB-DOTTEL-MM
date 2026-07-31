package com.afriland.dottel.model.dto.enrolement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmerEnrolementResponseDto {

    private Long id;
    private String matricule;
    private String nomPrenoms;
    private String fonction;
    private LocalDate dateEnrolement;
}