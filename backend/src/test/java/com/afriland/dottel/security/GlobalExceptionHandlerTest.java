package com.afriland.dottel.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void gererValidationInvalide_retourne400AvecChampsInvalides() {
        FieldError erreurMatricule = new FieldError("loginRequestDto", "matricule", "ne doit pas être vide");
        FieldError erreurMotDePasse = new FieldError("loginRequestDto", "motDePasse", "ne doit pas être vide");

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(erreurMatricule, erreurMotDePasse));

        ResponseEntity<Map<String, Object>> reponse = handler.gererValidationInvalide(methodArgumentNotValidException);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(reponse.getBody()).containsEntry("statut", 400);

        @SuppressWarnings("unchecked")
        Map<String, String> champs = (Map<String, String>) reponse.getBody().get("champs");
        assertThat(champs)
                .containsEntry("matricule", "ne doit pas être vide")
                .containsEntry("motDePasse", "ne doit pas être vide");
    }

    @Test
    void gererAccesRefuse_retourne403() {
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        ResponseEntity<Map<String, Object>> reponse = handler.gererAccesRefuse(exception);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(reponse.getBody()).containsEntry("statut", 403);
    }

    @Test
    void gererErreurGenerique_retourne500SansFuiteDeStackTrace() {
        Exception exception = new RuntimeException("Erreur technique interne sensible");

        ResponseEntity<Map<String, Object>> reponse = handler.gererErreurGenerique(exception);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(reponse.getBody()).containsEntry("statut", 500);
        assertThat(reponse.getBody()).containsEntry("erreur", "Une erreur interne est survenue");
        assertThat(reponse.getBody().toString()).doesNotContain("Erreur technique interne sensible");
        assertThat(reponse.getBody()).doesNotContainKey("stackTrace");
        assertThat(reponse.getBody()).doesNotContainKey("trace");
    }
}