package com.natania.protel_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
// 🚀 INI KUNCI ROUTE UTAMA AGAR SINKRON DENGAN URL FLUTTER_BASEURL KAMU, TA!
@RequestMapping("/api/medical-history")
@CrossOrigin(origins = "*") // Biar aman anti-blocking CORS saat demo lintas device
public class MedicalHistoryController {

    @Autowired
    private DashboardService dashboardService;

    // 🚀 INI GERBANG UNTUK MENANGKAP URL /add YANG DI-POST OLEH FLUTTER
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMedicalHistory(@RequestBody HealthLogRequest request) {
        try {
            // Melemparkan data kiriman Jackson Parser langsung ke method baru di DashboardService kamu
            Map<String, Object> result = dashboardService.saveMedicalHistory(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Jika ada kendala null pointer atau SQL constraint, server mengembalikan status bad request
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Gagal menyimpan rekam medis: " + e.getMessage()
            ));
        }
    }
}