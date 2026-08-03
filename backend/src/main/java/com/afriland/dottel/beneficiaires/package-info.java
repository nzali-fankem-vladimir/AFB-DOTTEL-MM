/**
 * Module beneficiaires (chantier MM, Sprint MM.5).
 *
 * Enrolement et gestion du referentiel des beneficiaires de la dotation
 * telephonique mensuelle : verification EHR (RG-01/RG-02 via referentiel),
 * unicite du matricule (RG-03), import/export en masse (RG-11).
 *
 * Dependances declarees :
 *  - referentiel :: api      -- eligibilite et resolution de grille (RG-01,
 *    RG-02, RG-04), API stable depuis MM.2/MM.3.
 *  - referentiel :: exception -- GrilleTarifaireIntrouvableException levee
 *    directement par EnrolementService.confirmer() : reutilisation
 *    intentionnelle actee en MM.3 pour preserver le contrat API V3.1 (meme
 *    exception, meme 400) plutot que d'en dupliquer une locale.
 *  - utilisateurs :: api     -- AuthenticatedUserService (acteur courant).
 *  - utilisateurs :: entity  -- DETTE TRACEE, voir
 *    utilisateurs/model/entity/package-info.java (prerequis MM.8).
 *  - audit :: api            -- EvenementAudit publie (MM.4).
 *
 * NB : cette dependance vers referentiel, combinee a la dependance inverse
 * de referentiel vers beneficiaires::api (cascade de renommage, couplage
 * C3 de MM.3), forme un cycle de modules detecte par ModularityTests.
 * Cycle assume et piné explicitement dans ModularityTests (pas cache) --
 * le vrai correctif (evenement applicatif remplacant l'appel direct) est
 * un prerequis reel avant la cloture du chantier, prevu en MM.8.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "referentiel :: api",
                "referentiel :: exception",
                "utilisateurs :: api",
                "utilisateurs :: entity",
                "audit :: api"
        }
)
package com.afriland.dottel.beneficiaires;
