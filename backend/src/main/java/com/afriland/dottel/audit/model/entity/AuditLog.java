package com.afriland.dottel.audit.model.entity;

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
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_utilisateur", nullable = false)
    private Long idUtilisateur;

    @Column(nullable = false)
    private String action;

    @Column(name = "entite_cible", nullable = false)
    private String entiteCible;

    @Column(name = "id_entite")
    private Long idEntite;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    @Column(name = "adresse_ip")
    private String adresseIp;

    @Column(name = "detail_json", columnDefinition = "TEXT")
    private String detailJson;
}