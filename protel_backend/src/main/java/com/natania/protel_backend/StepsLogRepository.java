package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StepsLogRepository extends JpaRepository<StepsLog, Long> {
    List<StepsLog> findByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);

    Optional<StepsLog> findFirstByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);

    @Query("SELECT COALESCE(SUM(s.totalSteps), 0) FROM StepsLog s WHERE s.userHealth.id = :userHealthId AND s.recordedAt >= CURRENT_DATE")
    Integer getTotalStepsToday(@Param("userHealthId") Long userHealthId);
}
