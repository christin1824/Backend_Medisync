package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HeartRateLogRepository extends JpaRepository<HeartRateLog, Long> {
    List<HeartRateLog> findByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);

    Optional<HeartRateLog> findFirstByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);
}
