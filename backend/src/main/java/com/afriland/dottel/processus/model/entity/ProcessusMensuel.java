package com.afriland.dottel.processus.model.entity;

import com.afriland.dottel.processus.model.enums.StatutEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "processus_mensuel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessusMensuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mois_paiement", nullable = false)
    private Integer moisPaiement;

    @Column(name = "annee_paiement", nullable = false)
    private Integer anneePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutEnum statut;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(name = "id_createur", nullable = false)
    private Long idCreateur;

    // Sprint MM.11 : RG-12 evolue (voir CLAUDE.md section 7). false pour tout
    // processus normal, true pour un rattrapage. @Builder.Default indispensable
    // ici : sans lui, @Builder laisserait le champ a null pour tout appelant
    // qui ne le renseigne pas explicitement (tous les processus normaux
    // existants avant ce sprint).
    @Builder.Default
    @Column(name = "rattrapage", nullable = false)
    private Boolean rattrapage = false;

    // Trace, pour un rattrapage, le processus normal CLOTURE dont il decoule.
    // Null pour un processus normal.
    @Column(name = "id_processus_original")
    private Long idProcessusOriginal;
}