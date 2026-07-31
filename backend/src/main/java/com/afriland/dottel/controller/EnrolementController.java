package com.afriland.dottel.controller;

import com.afriland.dottel.model.dto.enrolement.ConfirmerEnrolementRequestDto;
import com.afriland.dottel.model.dto.enrolement.ConfirmerEnrolementResponseDto;
import com.afriland.dottel.model.dto.enrolement.EnrolementVerificationResponseDto;
import com.afriland.dottel.service.EnrolementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrolement")
@RequiredArgsConstructor
public class EnrolementController {

    private final EnrolementService enrolementService;

    @GetMapping("/verifier")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<EnrolementVerificationResponseDto> verifier(@RequestParam String matricule) {
        return ResponseEntity.ok(enrolementService.verifier(matricule));
    }

    @PostMapping("/confirmer")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<ConfirmerEnrolementResponseDto> confirmer(
            @Valid @RequestBody ConfirmerEnrolementRequestDto requeteConfirmation) {
        ConfirmerEnrolementResponseDto reponse = enrolementService.confirmer(requeteConfirmation);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }
}