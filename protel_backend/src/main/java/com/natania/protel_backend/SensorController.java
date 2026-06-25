package com.natania.protel_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sensor")
@CrossOrigin(origins = "*")
public class SensorController {

    @Autowired
    private DashboardService dashboardService;

    // --- Endpoint Milik Temanmu ---
    @PostMapping("/mmwave")
    public ResponseEntity<Map<String, Object>> receiveMmwaveData(@RequestBody Map<String, Object> payload) {
        // ... (kode mmwave temanmu tetap di sini) ...
        return ResponseEntity.ok(Map.of("status", "success")); 
    }

    // --- 🚀 ENDPOINT BARU UNTUK NODE-RED KAMU 🚀 ---
    @PostMapping("/activity")
    public ResponseEntity<Map<String, Object>> receiveActivityData(@RequestBody Map<String, Object> payload) {
        System.out.println("====== DATA SENSOR MASUK DARI NODE-RED (ACTIVITY) ======");
        System.out.println("Payload mentah: " + payload);

        // 1. Ambil idToken (Jika di Node-RED kamu belum diset, kita beri default atau bypass sementara)
        String idToken = payload.get("idToken") != null ? payload.get("idToken").toString() : null;
        
        // PENTING: Jika Node-RED kamu belum mengirim token, sementara gunakan token dummy 
        // agar tidak terkena return badRequest()
        if (idToken == null || idToken.isBlank()) {
            System.out.println("[WARNING] idToken tidak ditemukan di payload Node-RED, menggunakan token default.");
            idToken = "DUMMY_TOKEN_NODE_RED"; 
        }

        // 2. Ekstrak data dari payload Node-RED kamu sesuai gambar debug
        String activity = payload.get("activity") != null ? payload.get("activity").toString() : "UNKNOWN";
        double confidence = payload.get("confidence") != null ? Double.parseDouble(payload.get("confidence").toString()) : 0.0;
        String predictionStatus = payload.get("prediction_status") != null ? payload.get("prediction_status").toString() : "invalid";
        String status = payload.get("status") != null ? payload.get("status").toString() : "failed";

        // 3. Bungkus ke dalam HealthLogRequest bawaan MediSync
        HealthLogRequest logRequest = new HealthLogRequest();
        logRequest.setIdToken(idToken);
        logRequest.setType("ACTIVITY"); // Kita bedakan typenya dengan mmwave (PRESENCE)
        
        // Pemetaan logika untuk dashboardService temanmu:
        // Kita masukkan string aktivitas (misal: "DUDUK") ke dalam status
        logRequest.setStatus(activity); 
        
        // Nilai value diisi berbasis persentase confidence (dikali 100) atau biarkan berupa angka desimal
        logRequest.setValue((int) (confidence * 100)); 

        // Set koordinat default jika di activity tidak menangkap GPS
        logRequest.setLatitude(0.0);
        logRequest.setLongitude(0.0);

        // 4. Tembak ke mesin utama recordLog milik temanmu
        try {
            Map<String, Object> logResult = dashboardService.recordLog(logRequest);
            System.out.println("Hasil eksekusi otomatis: " + logResult);
            System.out.println("=======================================================");
            return ResponseEntity.ok(logResult);
        } catch (Exception e) {
            System.out.println("[ERROR] Gagal memproses ke dashboardService: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error", 
                "message", "Gagal menyimpan data ke sistem: " + e.getMessage()
            ));
        }
    }
}