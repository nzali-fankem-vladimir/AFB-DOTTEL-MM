package com.afriland.dottel.processus.service;

import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;

public interface SignatureService {

    String signer(Utilisateur acteur);
}