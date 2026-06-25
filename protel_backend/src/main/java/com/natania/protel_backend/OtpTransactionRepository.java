package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpTransactionRepository extends JpaRepository<OtpTransaction, Long> {
    // Mencari OTP terbaru berdasarkan nomor HP
    Optional<OtpTransaction> findTopByPhoneOrderByExpiredAtDesc(String phone);
}