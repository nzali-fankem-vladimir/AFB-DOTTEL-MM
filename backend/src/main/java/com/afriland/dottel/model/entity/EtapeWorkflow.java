package com.afriland.dottel.model.entity;

import com.afriland.dottel.model.enums.NomEtapeEnum;
import com.afriland.dottel.model.enums.StatutEtapeEnum;
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
@Table(name = "etape_workflow")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtapeWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_processus", nullable = false)
    private Long idProcessus;

    @Column(name = "id_acteur", nullable = false)
    private Long idActeur;

    @Column(name = "ordre_etape", nullable = false)
    private Integer ordreEtape;

    @Enumerated(EnumType.STRING)
    @Column(name = "nom_etape", nullable = false)
    private NomEtapeEnum nomEtape;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_etape", nullable = false)
    private StatutEtapeEnum statutEtape;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    @Column(name = "motif_retour")
    private String motifRetour;

    @Column(name = "signature_numerique")
    private String signatureNumerique;
}