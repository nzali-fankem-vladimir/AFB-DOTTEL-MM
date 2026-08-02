package com.afriland.dottel;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Test d'architecture Spring Modulith -- chantier MM.1 a MM.6.
 *
 * Ce test est VOLONTAIREMENT rouge pendant toute la duree du chantier de
 * modularisation (MM.1 a MM.5). Il sert de compteur de dette architecturale :
 * chaque violation de frontiere de module corrigee dans MM.2, MM.3 ou MM.4
 * rapproche ce test du vert. Il ne doit passer vert qu'a la fin de MM.6,
 * une fois toutes les frontieres de modules etablies et respectees.
 */
class ModularityTests {

    static final ApplicationModules MODULES =
            ApplicationModules.of(DottelApplication.class);

    @Test
    void verifieLesFrontieresDeModules() {
        MODULES.verify();
    }
}
