package com.afriland.dottel.beneficiaires.service;

import com.afriland.dottel.beneficiaires.api.BeneficiaireApi;
import com.afriland.dottel.beneficiaires.api.BeneficiaireDotationDto;
import com.afriland.dottel.beneficiaires.api.BeneficiaireIdentiteDto;
import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import com.afriland.dottel.beneficiaires.repository.BeneficiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BeneficiaireApiImpl implements BeneficiaireApi {

    private final BeneficiaireRepository beneficiaireRepository;

    @Override
    public List<BeneficiaireDotationDto> listerActifsPourDotation() {
        return beneficiaireRepository.findByActifTrue().stream()
                .map(beneficiaire -> new BeneficiaireDotationDto(
                        beneficiaire.getId(),
                        beneficiaire.getMatricule(),
                        beneficiaire.getNomPrenoms(),
                        beneficiaire.getFonction()))
                .toList();
    }

    @Override
    public Map<Long, BeneficiaireIdentiteDto> identitesParId(Collection<Long> idsBeneficiaires) {
        return beneficiaireRepository.findAllById(idsBeneficiaires).stream()
                .collect(Collectors.toMap(Beneficiaire::getId, beneficiaire -> new BeneficiaireIdentiteDto(
                        beneficiaire.getId(),
                        beneficiaire.getMatricule(),
                        beneficiaire.getNomPrenoms())));
    }

    // Optional.map() rend un Optional vide aussi bien pour un beneficiaire
    // introuvable que pour un grade null : c'est exactement l'equivalence
    // recherchee, l'appelant obtenait deja null dans les deux cas.
    @Override
    public Optional<String> gradeDe(Long idBeneficiaire) {
        return beneficiaireRepository.findById(idBeneficiaire).map(Beneficiaire::getGrade);
    }
}
