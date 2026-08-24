package com.afriland.dottel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Sprint 7.1 (adapté MM) : import du {@code JwtDecoder} de test.
 *
 * <p>Ce test est le seul, avec les tests d'intégration, à démarrer un contexte
 * Spring complet — {@code ModularityTests} et {@code DocumentationTests} n'en
 * démarrent aucun. Sans cet import, il restait le dernier point du build à
 * exiger un Keycloak réellement joignable : {@code issuer-uri} déclenche
 * {@code JwtDecoders.fromIssuerLocation(...)}, qui appelle
 * {@code /.well-known/openid-configuration} dès la création du bean. Le laisser
 * ainsi rendrait faux, au niveau du build, le critère « aucune dépendance à un
 * Keycloak démarré pour {@code mvn test} ».</p>
 *
 * <p>Aucune logique de test n'est modifiée : {@code contextLoads()} vérifie
 * toujours exactement la même chose.</p>
 */
@SpringBootTest(properties =
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + SupportAuthentificationTest.ISSUER_URI_DE_TEST)
@Import(SupportAuthentificationTest.JwtDecoderDeTest.class)
class DottelApplicationTests {

	@Test
	void contextLoads() {
	}

}
