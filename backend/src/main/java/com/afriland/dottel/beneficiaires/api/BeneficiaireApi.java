package com.afriland.dottel.beneficiaires.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API publique du module beneficiaires, destinee aux autres modules.
 *
 * Introduite au Sprint MM.2 pour que ProcessusMensuelService cesse d'injecter
 * BeneficiaireRepository. Aucune methode ne retourne l'entite JPA Beneficiaire.
 */
public interface BeneficiaireApi {

    /**
     * Beneficiaires actifs a inclure au declenchement d'un processus mensuel.
     */
    List<BeneficiaireDotationDto> listerActifsPourDotation();

    /**
     * Identites des beneficiaires demandes, indexees par id.
     *
     * Un id sans correspondance est simplement absent de la Map : l'appelant
     * obtient null via get(), comme aujourd'hui avec findAllById().
     */
    Map<Long, BeneficiaireIdentiteDto> identitesParId(Collection<Long> idsBeneficiaires);

    /**
     * Grade d'un beneficiaire, pour la reverification de RG-02 lors d'un
     * ajustement d'etat mensuel.
     *
     * Optional vide dans DEUX cas indistincts et volontairement traites pareil :
     * beneficiaire introuvable, ou grade non renseigne. Les deux donnaient deja
     * null cote appelant, et EligibiliteService les traite en NON GRADE
     * (fail-closed sur les corps de controle, neutre ailleurs).
     *
     * Ne leve JAMAIS d'exception : un ajustement porte sur un lot de lignes, et
     * une ligne fautive ne doit pas faire echouer les autres (RG-11).
     */
    Optional<String> gradeDe(Long idBeneficiaire);
}
