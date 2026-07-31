package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.Utilisateur;

public interface SignatureService {

    String signer(Utilisateur acteur);
}