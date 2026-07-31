package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.Utilisateur;

public interface NotificationService {

    void notifier(Utilisateur destinataire, String sujet, String message);
}