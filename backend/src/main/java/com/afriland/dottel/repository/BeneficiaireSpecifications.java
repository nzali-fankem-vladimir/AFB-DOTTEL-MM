package com.afriland.dottel.repository;

import com.afriland.dottel.model.entity.Beneficiaire;
import org.springframework.data.jpa.domain.Specification;

public final class BeneficiaireSpecifications {

    private BeneficiaireSpecifications() {
    }

    public static Specification<Beneficiaire> avecFiltres(String fonction, String uniteRattachement, Boolean actif) {
        return (root, query, cb) -> {
            var predicats = cb.conjunction();
            if (fonction != null) {
                predicats = cb.and(predicats, cb.equal(root.get("fonction"), fonction));
            }
            if (uniteRattachement != null) {
                // Recherche partielle insensible a la casse : le champ libre du frontend
                // (ex. "Douala") doit trouver "Agence Douala Akwa", "Direction Regionale
                // Douala", etc. -- une egalite stricte ne trouvait jamais rien (bug
                // constate au test manuel du Sprint 6F.5).
                predicats = cb.and(predicats,
                        cb.like(cb.lower(root.get("uniteRattachement")), "%" + uniteRattachement.toLowerCase() + "%"));
            }
            if (actif != null) {
                predicats = cb.and(predicats, cb.equal(root.get("actif"), actif));
            }
            return predicats;
        };
    }
}