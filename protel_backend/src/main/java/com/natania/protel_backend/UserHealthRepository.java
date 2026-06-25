package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserHealthRepository extends JpaRepository<UserHealth, Long> {
    // Fungsi bantuan untuk mencari user berdasarkan UID Firebase (jika nanti dibutuhkan)
    Optional<UserHealth> findByFirebaseUid(String firebaseUid);

    Optional<UserHealth> findByEmail(String email);

    // 🚀 FUNGSI BARU: Mengecek apakah nomor HP sudah ada di database
    boolean existsByPhone(String phone);

    Optional<UserHealth> findByPhone(String phone);
}