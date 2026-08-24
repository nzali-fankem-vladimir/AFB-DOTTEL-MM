package com.afriland.dottel;

import com.afriland.dottel.security.RoleJwtAuthenticationConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

/**
 * Socle d'authentification des tests d'intégration (Sprint 7.1 adapté MM).
 *
 * <h2>Pourquoi cette classe existe</h2>
 *
 * Depuis le Sprint MM.7 (décision de portée P-2), {@code JwtUtil} et
 * {@code AuthController.login()} n'existent plus : Keycloak est l'unique
 * émetteur de jetons. Un test d'intégration ne peut donc plus obtenir un
 * jeton par un {@code POST /auth/login} — il doit injecter directement une
 * {@code Authentication} dans le contexte de sécurité de la requête MockMvc.
 *
 * <h2>Les deux morceaux du problème</h2>
 *
 * <ol>
 *   <li><b>La requête.</b> {@link #jetonDe} construit un
 *       {@link RequestPostProcessor} {@code jwt()} porteur des deux claims
 *       réellement lus par le module : {@code email} (résolution d'identité,
 *       décision I-2 — {@code AuthenticatedUserService.utilisateurCourant()})
 *       et {@code realm_access.roles} (lu par
 *       {@link RoleJwtAuthenticationConverter}).</li>
 *   <li><b>Le démarrage du contexte.</b> Déclarer {@code issuer-uri} fait
 *       construire le {@code JwtDecoder} par
 *       {@code JwtDecoders.fromIssuerLocation(...)}, qui interroge
 *       {@code /.well-known/openid-configuration} <b>dès la création du
 *       bean</b> — avant qu'un seul test ne s'exécute. Sans précaution,
 *       {@code mvn test} exigerait un Keycloak démarré. {@link JwtDecoderDeTest}
 *       déclare son propre bean {@code JwtDecoder} : l'auto-configuration de
 *       Spring Boot, conditionnée par {@code @ConditionalOnMissingBean},
 *       n'essaie alors plus de construire le sien, et aucun appel réseau
 *       n'a lieu.</li>
 * </ol>
 *
 * <h2>Ce qui n'est PAS mocké ici</h2>
 *
 * Les autorités ne sont pas fabriquées à la main : elles sont dérivées du
 * jeton par le <b>convertisseur de production</b>
 * ({@link RoleJwtAuthenticationConverter}). Le filtrage des rôles techniques
 * Keycloak et le refus d'un cumul de rôles DOTTEL sont donc réellement
 * exercés par chaque requête de test, pas contournés.
 *
 * @see SupportAuthentificationTest.JwtDecoderDeTest
 */
public final class SupportAuthentificationTest {

    private SupportAuthentificationTest() {
    }

    /**
     * Valeur syntaxiquement valide pour
     * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}.
     *
     * <p>La propriété n'a aucune valeur de repli dans {@code application.yml}
     * (Sprint MM.7) : sans elle, le contexte échoue à la résolution du
     * placeholder, avant même d'atteindre l'auto-configuration. La déclarer
     * sur les classes de test évite d'exiger une variable d'environnement
     * pour un décodeur qui, de toute façon, ne sera jamais invoqué.</p>
     */
    public static final String ISSUER_URI_DE_TEST = "http://localhost:8180/realms/dottel-dev";

    /**
     * Les cinq comptes de test insérés par Flyway V3, un par rôle.
     * L'email est l'identifiant AD simulé (partie locale = prénom_nom).
     */
    public enum CompteTest {

        ARH_MBARGA("1847", "jeanpaul_mbarga@afrilandfirstbank.com", "ARH"),
        CRH_ESSAMA("2093", "marieclaire_essama@afrilandfirstbank.com", "CRH"),
        DRH_ATANGANA("1562", "paul_atangana@afrilandfirstbank.com", "DRH"),
        EMPLOYE_NKOLO("2201", "sylvie_nkolo@afrilandfirstbank.com", "EMPLOYE"),
        ADMIN_TCHINDA("1734", "marc_tchinda@afrilandfirstbank.com", "ADMIN");

        private final String matricule;
        private final String email;
        private final String roleRealm;

        CompteTest(String matricule, String email, String roleRealm) {
            this.matricule = matricule;
            this.email = email;
            this.roleRealm = roleRealm;
        }

        public String matricule() {
            return matricule;
        }

        public String email() {
            return email;
        }

        public String roleRealm() {
            return roleRealm;
        }
    }

    private static final RoleJwtAuthenticationConverter CONVERTISSEUR_ROLES =
            new RoleJwtAuthenticationConverter();

    /** Jeton Keycloak simulé pour un compte de test, avec son rôle nominal. */
    public static RequestPostProcessor jetonDe(CompteTest compte) {
        return jetonDe(compte.email(), compte.roleRealm());
    }

    /**
     * Jeton Keycloak simulé pour une identité et un rôle choisis séparément.
     *
     * <p>Indispensable au flux RG-08 (étape 4 du sprint) : après une
     * <b>promotion de rôle</b> par l'ADMIN, le même email se présente avec un
     * rôle realm différent de son rôle d'origine — exactement ce que ferait
     * Keycloak après mise à jour du realm.</p>
     */
    public static RequestPostProcessor jetonDe(String email, String roleRealm) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(constructeur -> constructeur
                        .claim("email", email)
                        .claim("preferred_username", email.substring(0, email.indexOf('@')))
                        // Les rôles techniques Keycloak sont volontairement présents :
                        // RoleJwtAuthenticationConverter doit les ignorer sans que le
                        // rôle métier en pâtisse (ROLES_DOTTEL).
                        .claim("realm_access", Map.of("roles",
                                List.of(roleRealm, "offline_access", "uma_authorization"))))
                .authorities(CONVERTISSEUR_ROLES);
    }

    /**
     * Bean {@code JwtDecoder} de test : présent pour que l'auto-configuration
     * Spring Boot recule, jamais réellement invoqué (les requêtes portent leur
     * {@code Authentication} déjà construite via {@code jwt()}).
     *
     * <p>Il lève délibérément plutôt que de retourner un jeton bidon : si un
     * jour une requête de test passe par le vrai chemin de décodage, l'échec
     * doit être bruyant et explicite, jamais silencieusement permissif.</p>
     *
     * <p>À importer explicitement — une {@code @TestConfiguration} n'est pas
     * ramassée par le scan de composants.</p>
     */
    @TestConfiguration
    public static class JwtDecoderDeTest {

        @Bean
        JwtDecoder jwtDecoder() {
            return jeton -> {
                throw new UnsupportedOperationException(
                        "Décodeur JWT de test : aucun jeton ne doit être décodé pendant mvn test. "
                                + "Les requêtes MockMvc doivent porter SupportAuthentificationTest.jetonDe(...).");
            };
        }
    }
}
