package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OxygenLogRepository extends JpaRepository<OxygenLog, Long> {
    List<OxygenLog> findByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);

    Optional<OxygenLog> findFirstByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);
}
