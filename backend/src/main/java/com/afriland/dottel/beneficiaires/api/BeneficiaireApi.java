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

    /**
     * Nombre de beneficiaires actifs rattaches a un code fonction.
     *
     * Introduite au Sprint MM.3 (couplage C3) pour que le module referentiel
     * cesse d'injecter BeneficiaireRepository. Utilisee comme avertissement
     * avant desactivation d'une fonction eligible, et comme garde-fou avant
     * un renommage de code (au plus 1 beneficiaire actif tolere).
     */
    long compterActifsParFonction(String codeFonction);

    /**
     * Renomme le code fonction sur tous les beneficiaires qui le portent
     * (y compris inactifs), sans toucher aux lignes ligne_etat_mensuel.
     *
     * Introduite au Sprint MM.3 (couplage C3) : la cascade de renommage,
     * jusqu'ici executee directement par FonctionEligibleService sur
     * BeneficiaireRepository, est deplacee ici en appel synchrone pour
     * rester dans la meme transaction que le renommage cote referentiel
     * (contrat API v3 section 3bis -- l'atomicite doit etre preservee).
     *
     * beneficiaires.fonction et ligne_etat_mensuel.fonction_retenue sont des
     * VARCHAR, pas des FK : cette methode ne touche jamais a
     * ligne_etat_mensuel, instantane historique volontairement fige.
     */
    void renommerFonction(String ancienCode, String nouveauCode);

    /**
     * Donnees d'affichage des beneficiaires demandes, pour la generation du
     * PDF d'etat mensuel.
     *
     * Introduite au Sprint MM.3 (couplage C4) pour que DocumentService cesse
     * d'injecter BeneficiaireRepository. Un id sans correspondance est
     * simplement absent de la Map, comme identitesParId().
     */
    Map<Long, BeneficiaireDocumentDto> donneesDocumentParId(Collection<Long> idsBeneficiaires);
}
