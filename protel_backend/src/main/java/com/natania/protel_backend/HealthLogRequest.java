package com.natania.protel_backend;

import lombok.Data;

@Data
public class HealthLogRequest {
    private String idToken;
    private String type;
    private int value;
    private String status;
    private double latitude;
    private double longitude;
    private Long userId;
    private String firebaseUid; // Tetap dipertahankan aman, Ta!

    // 🚀 --- TAMBAHAN FIELD PENAMPUNG DATA MEDIS LANSIA DARI FLUTTER --- 🚀
    private String description;

    private Integer age;
    private String bloodType;
    private Integer baselineBpm;
    private Integer baselineSpo2;
    private String allergies;
}