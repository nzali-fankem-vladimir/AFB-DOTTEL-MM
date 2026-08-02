package com.afriland.dottel.audit.repository;

import com.afriland.dottel.audit.model.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> avecFiltres(Long idUtilisateur, String action, String entiteCible,
                                                        LocalDateTime dateDebut, LocalDateTime dateFin) {
        return (root, query, cb) -> {
            var predicats = cb.conjunction();
            if (idUtilisateur != null) {
                predicats = cb.and(predicats, cb.equal(root.get("idUtilisateur"), idUtilisateur));
            }
            if (action != null) {
                predicats = cb.and(predicats, cb.equal(root.get("action"), action));
            }
            if (entiteCible != null) {
                predicats = cb.and(predicats, cb.equal(root.get("entiteCible"), entiteCible));
            }
            if (dateDebut != null) {
                predicats = cb.and(predicats, cb.greaterThanOrEqualTo(root.get("dateAction"), dateDebut));
            }
            if (dateFin != null) {
                predicats = cb.and(predicats, cb.lessThanOrEqualTo(root.get("dateAction"), dateFin));
            }
            return predicats;
        };
    }
}