package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.Utilisateur;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SignatureServiceAutonome implements SignatureService {

    private static final DateTimeFormatter FORMAT_HORODATAGE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Trace de validation geree en autonomie par DOTTEL (nom de l'acteur +
    // horodatage) : ce n'est PAS une signature electronique certifiee.
    // Remplacable plus tard par une integration a un service de signature
    // type INTRA (signatures pre-enrolees, insertion automatique sur les
    // documents), meme principe que le stub EHR (EhrIntegrationServiceStub).
    @Override
    public String signer(Utilisateur acteur) {
        String horodatage = LocalDateTime.now().format(FORMAT_HORODATAGE);
        return acteur.getPrenom() + " " + acteur.getNom() + " (matricule " + acteur.getMatricule() + ") - " + horodatage;
    }
}