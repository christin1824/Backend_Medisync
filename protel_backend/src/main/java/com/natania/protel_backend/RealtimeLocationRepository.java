package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RealtimeLocationRepository extends JpaRepository<RealtimeLocation, Long> {

    // 🔥 PERBAIKAN: Ubah dari List menjadi Optional agar cocok dengan logika update-if-exist di DashboardService 🔥
    Optional<RealtimeLocation> findByUserHealth_Id(Long userHealthId);

    // Jalur pelacakan data paling update untuk kebutuhan dashboard harian
    Optional<RealtimeLocation> findFirstByUserHealth_IdOrderByUpdatedAtDesc(Long userHealthId);
}