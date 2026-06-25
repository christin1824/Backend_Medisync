package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ElderProfileRepository extends JpaRepository<ElderProfile, Long> {
    Optional<ElderProfile> findByUserHealth_Id(Long userHealthId);
}