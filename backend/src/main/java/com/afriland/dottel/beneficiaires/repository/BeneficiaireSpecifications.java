package com.afriland.dottel.beneficiaires.repository;

import com.afriland.dottel.beneficiaires.model.entity.Beneficiaire;
import org.springframework.data.jpa.domain.Specification;

public final class BeneficiaireSpecifications {

    private BeneficiaireSpecifications() {
    }

    public static Specification<Beneficiaire> avecFiltres(String fonction, String recherche,
                                                            String uniteRattachement, Boolean actif) {
        return (root, query, cb) -> {
            var predicats = cb.conjunction();
            if (fonction != null) {
                predicats = cb.and(predicats, cb.equal(root.get("fonction"), fonction));
            }
            if (recherche != null) {
                // Meme semantique que le filtre du modal d'ajustement (volet 1) : recherche
                // OR insensible a la casse sur matricule ou nomPrenoms.
                String terme = "%" + recherche.toLowerCase() + "%";
                predicats = cb.and(predicats, cb.or(
                        cb.like(cb.lower(root.get("matricule")), terme),
                        cb.like(cb.lower(root.get("nomPrenoms")), terme)));
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