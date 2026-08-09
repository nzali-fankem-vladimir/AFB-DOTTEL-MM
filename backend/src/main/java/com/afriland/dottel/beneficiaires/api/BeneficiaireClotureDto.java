package com.afriland.dottel.beneficiaires.api;

/**
 * Donnees d'un beneficiaire destinees a l'evenement de cloture publie vers le
 * module comptable (Sprint MM.13).
 *
 * <p>SCHEMA PROVISOIRE, A VALIDER AVEC LA COMPTABILITE (decision D de M.0) :
 * le contrat exact attendu n'est pas confirme, DOTTEL en propose un.</p>
 *
 * <p>Volontairement DISTINCT de BeneficiaireDocumentDto, dont il porte
 * aujourd'hui les memes champs -- par coincidence, pas par nature. Ce sont
 * deux contrats de mondes differents : un document INTERNE dont DOTTEL decide
 * librement le contenu, contre un contrat INTER-SYSTEMES dont le contenu final
 * appartient a la comptabilite. Les partager signifierait qu'ajouter une
 * colonne au PDF modifie le payload envoye a un systeme tiers, et
 * reciproquement. Meme discipline qu'en MM.2, ou identitesParId() et
 * donneesDocumentParId() ont ete separes plutot que fusionnes en un
 * superset.</p>
 *
 * <p>Alimente les DEUX lignes d'ecriture comptable cibles (CLAUDE.md section
 * 11) : codeUnite le DEBIT, codeAgence + numCompteCourant le CREDIT.</p>
 */
public record BeneficiaireClotureDto(Long id, String nomPrenoms, String codeUnite, String codeAgence,
                                      String numCompteCourant, String chapitre) {
}
