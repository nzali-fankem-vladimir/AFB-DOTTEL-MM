package com.afriland.dottel.audit.api;

import java.util.Map;

public record EvenementAudit(
        Long idUtilisateur,
        String action,
        String entiteCible,
        Long idEntite,
        Map<String, Object> avant,
        Map<String, Object> apres
) {
}
