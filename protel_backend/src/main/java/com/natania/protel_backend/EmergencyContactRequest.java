package com.natania.protel_backend;

import lombok.Data;

@Data
public class EmergencyContactRequest {
    private Long userId; // Untuk melacak kontak ini milik lansia ID berapa
    private String name;
    private String phone;
    private String relation;
}