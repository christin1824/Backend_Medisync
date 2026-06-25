package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserHealth_IdOrderByCreatedAtDesc(Long userHealthId);
}
