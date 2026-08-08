package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.model.dto.ehr.EmployeEhrDto;
import com.afriland.dottel.beneficiaires.model.dto.ehr.UniteRattachementDto;

import java.util.List;
import java.util.Optional;

public interface EhrIntegrationService {

    Optional<EmployeEhrDto> rechercherEmploye(String matricule);

    List<UniteRattachementDto> listerUnitesRattachement();
}