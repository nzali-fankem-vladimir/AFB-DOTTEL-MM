package com.afriland.dottel;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'architecture Spring Modulith -- chantier MM.1 a MM.6.
 *
 * Vert depuis le Sprint MM.5, avec UNE exception explicitement pinee (voir
 * verifieLesFrontieresDeModules ci-dessous) : le cycle beneficiaires <->
 * referentiel, cause par deux dependances legitimes mais opposees
 * (beneficiaires -> referentiel::api pour l'eligibilite/la grille ;
 * referentiel -> beneficiaires::api pour 6 appels de
 * FonctionEligibleService vers BeneficiaireApi : desactiver(),
 * listerToutes() et reactiver() qui lisent compterActifsParFonction() pour
 * l'affichage/le garde-fou, modifier() qui l'appelle deux fois pour le
 * garde-fou "au plus 1 beneficiaire actif", et modifier() qui ecrit via
 * renommerFonction() pour la cascade de renommage de code de fonction,
 * couplage C3 acte en MM.3).
 *
 * Decision G-2 du 2026-08-03 (voir
 * docs/chantier-ajout-metier-mm/MM.8_anomalies_et_cloture_cycle.md) : ce
 * cycle est ASSUME DEFINITIVEMENT, il ne s'agit plus d'une dette en attente
 * de correction. L'analyse a montre que le plan initial (evenementer la
 * seule ecriture renommerFonction()) NE CASSERAIT PAS le cycle : Spring
 * Modulith detecte un cycle au niveau du module entier, et il suffit d'UN
 * SEUL des 6 appels pour que le cycle persiste. Or 5 des 6 sont des
 * lectures synchrones (compterActifsParFonction()) qui conditionnent une
 * decision immediate -- typiquement un refus HTTP 409 si plus d'un
 * beneficiaire actif reste rattache a la fonction. Un evenement
 * fire-and-forget ne peut pas repondre avant que la methode appelante ne
 * continue : ces lectures ne peuvent structurellement pas devenir des
 * evenements asynchrones. `referentiel` a une raison metier reelle de
 * consulter `beneficiaires` (compter avant de bloquer une action
 * destructrice), symetrique a la raison pour laquelle `beneficiaires`
 * consulte `referentiel` (eligibilite, grille) -- ce n'est pas un accident
 * de conception. Denormaliser le compteur cote `referentiel` (alternative
 * G-3) ajouterait un etat duplique avec risque de desynchronisation, pour
 * un gain disproportionne (25 fonctions, quelques dizaines de beneficiaires
 * chacune) -- voir beneficiaires/package-info.java et
 * referentiel/package-info.java.
 *
 * Le filtre ci-dessous ne masque PAS la dette : il isole ce cycle precis par
 * son message exact, et le test echoue si une AUTRE violation apparait
 * (nouvelle ou differente), qu'il s'agisse d'un nouveau cycle ou d'une
 * regression de visibilite. Protection anti-regression reelle, pas un test
 * desactive.
 */
class ModularityTests {

    static final ApplicationModules MODULES =
            ApplicationModules.of(DottelApplication.class);

    @Test
    void verifieLesFrontieresDeModules() {
        Violations violations = MODULES.detectViolations();

        Violations cycleConnuBeneficiairesReferentiel = violations.filter(
                v -> v.getMessage().contains("Cycle detected: Slice beneficiaires")
                        && v.getMessage().contains("Slice referentiel"));

        assertThat(cycleConnuBeneficiairesReferentiel.hasViolations())
                .as("le cycle beneficiaires <-> referentiel documente en MM.5 est ASSUME "
                        + "definitivement (decision G-2 du 2026-08-03, MM.8) -- s'il a disparu, "
                        + "quelqu'un a supprime un des 6 appels legitimes de FonctionEligibleService "
                        + "vers BeneficiaireApi ; verifier avant de retirer ce filtre")
                .isTrue();

        Violations autresViolations = violations.filter(
                v -> !(v.getMessage().contains("Cycle detected: Slice beneficiaires")
                        && v.getMessage().contains("Slice referentiel")));

        assertThat(autresViolations.hasViolations())
                .as(() -> "violation(s) de modularite non attendue(s) : " + autresViolations.getMessage())
                .isFalse();
    }
}
