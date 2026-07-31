package com.afriland.dottel.security;

import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.model.enums.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class JwtUtilTest {

    private static final String SECRET_TEST = "cleSecreteDeTestPourJwtUtilTest1234567890AFBDottelHS384";
    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000;

    private JwtUtil jwtUtil;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_TEST);

        utilisateur = Utilisateur.builder()
                .id(1L)
                .matricule("1562")
                .nom("ATANGANA")
                .prenom("Paul")
                .email("paul.atangana@afrilandfirstbank.com")
                .role(RoleEnum.ARH)
                .motDePasseHash("hashBcrypt")
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .build();
    }

    @Test
    void genererToken_casNominal_contientLesClaimsAttendus() {
        String token = jwtUtil.genererToken(utilisateur);

        Claims claims = Jwts.parser()
                .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET_TEST.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("1562");
        assertThat(claims.get("role", String.class)).isEqualTo("ARH");
    }

    @Test
    void genererToken_casNominal_expirationCorrecte() {
        Instant avant = Instant.now();
        String token = jwtUtil.genererToken(utilisateur);

        Claims claims = Jwts.parser()
                .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET_TEST.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Instant expirationAttendue = avant.plusMillis(EXPIRATION_MS);
        Instant expirationReelle = claims.getExpiration().toInstant();

        // Tolerance de 5 secondes : la generation du jeton tronque au niveau de
        // la seconde (format JWT) et prend un temps non nul entre la lecture de
        // "avant" et l'appel a genererToken(), une egalite stricte est flaky.
        assertThat(expirationReelle).isCloseTo(expirationAttendue, within(5, ChronoUnit.SECONDS));
    }

    @Test
    void genererToken_utilisateurNull_leveNullPointerException() {
        // Documente le comportement ACTUEL de JwtUtil : aucune verification de
        // nullite n'est faite avant l'appel a utilisateur.getMatricule(), donc
        // une NullPointerException generique du JDK est levee.
        // Ce n'est PAS le comportement souhaitable a terme.
        // BACKLOG TECHNIQUE : JwtUtil.genererToken() devrait lever une exception
        // metier explicite (ex. IllegalArgumentException ou exception dediee)
        // plutot qu'une NullPointerException generique si utilisateur == null.
        // A traiter dans un futur sprint, pas dans ce sprint (tests uniquement).
        assertThatThrownBy(() -> jwtUtil.genererToken(null))
                .isInstanceOf(NullPointerException.class);
    }
}