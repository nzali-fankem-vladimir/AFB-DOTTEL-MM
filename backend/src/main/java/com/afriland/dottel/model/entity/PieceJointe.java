package com.afriland.dottel.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "piece_jointe")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_processus", nullable = false, unique = true)
    private Long idProcessus;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    @Column(name = "date_generation_initiale", nullable = false)
    private LocalDateTime dateGenerationInitiale;

    @Column(name = "date_derniere_mise_a_jour", nullable = false)
    private LocalDateTime dateDerniereMiseAJour;

    @Column(name = "nombre_signatures", nullable = false)
    private Integer nombreSignatures;
}