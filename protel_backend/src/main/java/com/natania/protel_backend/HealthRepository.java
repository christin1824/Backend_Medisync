package com.natania.protel_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HealthRepository extends JpaRepository<UserHealth, Long> {
    Optional<UserHealth> findByFirebaseUid(String firebaseUid);
    Optional<UserHealth> findByEmail(String email);
    Optional<UserHealth> findByUsername(String username);
    Optional<UserHealth> findByUserName(String userName);
}