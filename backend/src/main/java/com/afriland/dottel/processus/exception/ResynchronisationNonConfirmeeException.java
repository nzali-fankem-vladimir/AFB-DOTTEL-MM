package com.afriland.dottel.processus.exception;

/**
 * Validation ARH demandee alors que des ecarts de montant existent et que
 * l'ARH ne les a pas confirmes (Sprint MM.12, variante B2-RESYNC en deux
 * temps). Traduite en 409 par GlobalExceptionHandler.
 *
 * C'est le garde-fou qui rend le "deux temps" reel plutot que conventionnel :
 * sans lui, tout client qui ignore GET /processus/{id}/ecarts-montants (Postman,
 * script, futur module) resynchroniserait silencieusement -- exactement la
 * variante B3 que la decision B a ecartee.
 */
public class ResynchronisationNonConfirmeeException extends RuntimeException {

    public ResynchronisationNonConfirmeeException(String message) {
        super(message);
    }
}
