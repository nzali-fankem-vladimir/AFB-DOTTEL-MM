package com.afriland.dottel.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ligne_etat_mensuel", uniqueConstraints = @UniqueConstraint(columnNames = {"id_processus", "id_beneficiaire"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneEtatMensuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_processus", nullable = false)
    private Long idProcessus;

    @Column(name = "id_beneficiaire", nullable = false)
    private Long idBeneficiaire;

    @Column(name = "montant_applique", nullable = false)
    private Integer montantApplique;

    @Column(name = "inclus_dans_etat", nullable = false)
    private Boolean inclusDansEtat;

    @Column(name = "fonction_retenue", nullable = false)
    private String fonctionRetenue;
}