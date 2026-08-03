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
 * referentiel -> beneficiaires::api pour la cascade de renommage de code de
 * fonction, couplage C3 acte en MM.3). Spring Modulith n'offre aucun
 * mecanisme pour DECLARER un cycle comme legitime (contrairement a
 * allowedDependencies pour une simple dependance) : le casser reellement
 * exige un refactor evenementiel (sur le modele de MM.4) qui depasse le
 * perimetre "visibilite + annotations" de MM.5. Decide avec l'utilisateur le
 * 2026-08-03 : PREREQUIS REEL avant la cloture du chantier (MM.8), pas une
 * intention vague -- voir beneficiaires/package-info.java.
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
                .as("le cycle beneficiaires <-> referentiel documente en MM.5 doit toujours exister -- "
                        + "s'il a disparu, le prerequis MM.8 est rempli : retirer ce filtre et repasser "
                        + "sur MODULES.verify() simple")
                .isTrue();

        Violations autresViolations = violations.filter(
                v -> !(v.getMessage().contains("Cycle detected: Slice beneficiaires")
                        && v.getMessage().contains("Slice referentiel")));

        assertThat(autresViolations.hasViolations())
                .as(() -> "violation(s) de modularite non attendue(s) : " + autresViolations.getMessage())
                .isFalse();
    }
}
