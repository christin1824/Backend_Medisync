package com.natania.protel_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emergency-contacts")
@CrossOrigin(origins = "*")
public class EmergencyContactController {

    @Autowired
    private DashboardService dashboardService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addContact(@RequestBody EmergencyContactRequest request) {
        System.out.println("====== SEEDING DATA: TAMBAH KONTAK DARURAT ======");
        System.out.println("Target User ID : " + request.getUserId());
        System.out.println("Nama Kontak    : " + request.getName());
        System.out.println("Hubungan       : " + request.getRelation());

        Map<String, Object> result = dashboardService.addEmergencyContact(request);

        System.out.println("Hasil Eksekusi : " + result);
        System.out.println("=================================================");
        return ResponseEntity.ok(result);
    }
}