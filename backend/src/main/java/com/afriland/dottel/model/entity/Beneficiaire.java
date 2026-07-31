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

import java.time.LocalDate;

@Entity
@Table(name = "beneficiaires")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricule;

    @Column(name = "nom_prenoms", nullable = false)
    private String nomPrenoms;

    @Column(nullable = false)
    private String fonction;

    @Column
    private String grade;

    @Column(name = "unite_rattachement", nullable = false)
    private String uniteRattachement;

    @Column(name = "code_unite", nullable = false)
    private String codeUnite;

    @Column(name = "num_compte_courant", nullable = false)
    private String numCompteCourant;

    @Column
    private String chapitre;

    @Column(name = "date_enrolement", nullable = false)
    private LocalDate dateEnrolement;

    @Column(nullable = false)
    private boolean actif;
}