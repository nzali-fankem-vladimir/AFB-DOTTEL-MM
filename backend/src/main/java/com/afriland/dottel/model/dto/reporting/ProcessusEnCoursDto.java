package com.afriland.dottel.model.dto.reporting;

import com.afriland.dottel.model.enums.StatutEnum;
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
public class ProcessusEnCoursDto {

    private Long id;
    private StatutEnum statut;
}