package com.afriland.dottel.audit.repository;

import com.afriland.dottel.audit.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    // Alimente le filtre "Action" du journal d'audit (GET /reporting/audit/actions,
    // Sprint MM.11) : les codes d'action sont des chaines libres deposees par
    // chaque module au fil de AuditService.enregistrer(), sans enum de reference
    // (CLAUDE.md section 6 fixe la liste a 5 enums, aucun ne couvre ce cas).
    // Deriver la liste depuis les valeurs reellement presentes evite qu'une
    // liste figee cote frontend se desynchronise a chaque nouvelle action.
    @Query("SELECT DISTINCT a.action FROM AuditLog a ORDER BY a.action")
    List<String> findDistinctActions();
}