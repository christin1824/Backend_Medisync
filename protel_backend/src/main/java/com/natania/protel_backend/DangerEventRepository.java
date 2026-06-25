package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DangerEventRepository extends JpaRepository<DangerEvent, Long> {
    List<DangerEvent> findByUserHealth_IdOrderByCreatedAtDesc(Long userHealthId);

    Optional<DangerEvent> findFirstByUserHealth_IdOrderByCreatedAtDesc(Long userHealthId);
}
