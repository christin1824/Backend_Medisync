package com.natania.protel_backend;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthLog {
    private Long id;

    private String type; // "HEART_RATE", "STEPS", "OXYGEN", atau "FALL"
    private int value;
    private String status;
    private LocalDateTime createdAt;

    private double latitude;
    private double longitude;   

    private Long userId;

    private String firebaseUid;
}