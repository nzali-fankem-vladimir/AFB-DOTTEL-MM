package com.afriland.dottel.utilisateurs.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.api.UtilisateurApi;
import com.afriland.dottel.utilisateurs.exception.UtilisateurIntrouvableException;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurApiImpl implements UtilisateurApi {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<DestinataireNotificationDto> destinatairesParRole(RoleEnum role) {
        return utilisateurRepository.findByRoleAndActifTrue(role).stream()
                .map(this::versDestinataire)
                .toList();
    }

    @Override
    public DestinataireNotificationDto destinataireParId(Long idUtilisateur) {
        return utilisateurRepository.findById(idUtilisateur)
                .map(this::versDestinataire)
                .orElseThrow(() -> new UtilisateurIntrouvableException(
                        "Aucun utilisateur avec l'id " + idUtilisateur));
    }

    private DestinataireNotificationDto versDestinataire(Utilisateur utilisateur) {
        return new DestinataireNotificationDto(utilisateur.getEmail(), utilisateur.getRole());
    }
}
