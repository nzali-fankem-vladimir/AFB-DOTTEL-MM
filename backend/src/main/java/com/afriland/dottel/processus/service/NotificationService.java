package com.afriland.dottel.processus.service;

import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;

public interface NotificationService {

    void notifier(Utilisateur destinataire, String sujet, String message);
}