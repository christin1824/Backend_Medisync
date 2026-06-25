package com.natania.protel_backend;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Notification {
    private Long id;

    private double latitude;
    private double longitude;

    private String title;    // Contoh: "LANSIA JATUH"
    private String message;  // Contoh: "Taman Bungkul, Surabaya"
    private LocalDateTime createdAt;
}