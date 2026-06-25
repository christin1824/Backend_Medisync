package com.natania.protel_backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_history")
@Data
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_health_id", nullable = false)
    private UserHealth userHealth;

    @Column(name = "description")
    private String description;

    // 🚀 --- TAMBAHAN KOLOM BARU SINKRONISASI FLUTTER --- 🚀
    @Column(name = "age")
    private Integer age;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "baseline_bpm")
    private Integer baselineBpm;

    @Column(name = "baseline_spo2")
    private Integer baselineSpo2;

    @Column(name = "allergies")
    private String allergies;
    // -----------------------------------------------------

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}