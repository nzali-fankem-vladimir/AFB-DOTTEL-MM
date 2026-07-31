package com.afriland.dottel.service;

import com.afriland.dottel.model.dto.ehr.EmployeEhrDto;

import java.util.Optional;

public interface EhrIntegrationService {

    Optional<EmployeEhrDto> rechercherEmploye(String matricule);
}