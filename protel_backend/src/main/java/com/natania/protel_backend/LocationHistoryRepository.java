package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    List<LocationHistory> findByUserHealth_IdOrderByRecordedAtDesc(Long userHealthId);
}
