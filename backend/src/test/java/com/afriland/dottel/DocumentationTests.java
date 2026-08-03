package com.afriland.dottel;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.docs.Documenter;

class DocumentationTests {

    @Test
    void genereLaDocumentation() {
        new Documenter(ModularityTests.MODULES)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
