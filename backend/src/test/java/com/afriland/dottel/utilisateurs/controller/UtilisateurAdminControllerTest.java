package com.afriland.dottel.utilisateurs.controller;
import com.afriland.dottel.utilisateurs.controller.UtilisateurAdminController;

import com.afriland.dottel.utilisateurs.model.dto.utilisateur.UtilisateurResponseDto;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.api.AuthenticatedUserService;
import com.afriland.dottel.utilisateurs.service.UtilisateurAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sprint MM.7 : spring.security.oauth2.resourceserver.jwt.issuer-uri est
// desormais une propriete Spring Boot standard (avant : dottel.security.jwt-secret,
// propriete custom invisible de l'autoconfiguration). Une tranche @WebMvcTest
// charge l'environnement complet meme si SecurityConfig n'est pas importe :
// sans cette exclusion, OAuth2ResourceServerAutoConfiguration s'auto-active et
// tente de construire sa propre SecurityFilterChain de repli, qui echoue faute
// de bean HttpSecurity (@EnableWebSecurity absent de ce contexte reduit). Ce
// test verifie @PreAuthorize via @WithMockUser, pas la chaine OAuth2 reelle.
@WebMvcTest(
        controllers = UtilisateurAdminController.class,
        excludeAutoConfiguration = {
                OAuth2ResourceServerAutoConfiguration.class,
                OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@Import(UtilisateurAdminControllerTest.MethodSecurityConfig.class)
class UtilisateurAdminControllerTest {

    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurAdminService utilisateurAdminService;

    @MockitoBean
    private AuthenticatedUserService authenticatedUserService;

    @Test
    @WithMockUser(roles = "DRH")
    void lister_avecRoleDrh_retourne200() throws Exception {
        when(utilisateurAdminService.rechercher(null, null)).thenReturn(
                List.of(UtilisateurResponseDto.builder()
                        .id(1L)
                        .matricule("AFB1024")
                        .nomPrenoms("ONANA Serge")
                        .role(RoleEnum.DRH)
                        .actif(true)
                        .build()));

        mockMvc.perform(get("/admin/utilisateurs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void lister_avecRoleAdmin_retourne200() throws Exception {
        when(utilisateurAdminService.rechercher(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/admin/utilisateurs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ARH")
    void lister_avecRoleArh_retourne403() throws Exception {
        mockMvc.perform(get("/admin/utilisateurs"))
                .andExpect(status().isForbidden());
    }
}