package com.natania.protel_backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserHealthService userHealthService;
    private final WhatsAppOtpService whatsAppOtpService; // 🚀 INJEKSI SERVICE WA

    public AuthController(UserHealthService userHealthService, WhatsAppOtpService whatsAppOtpService) {
        this.userHealthService = userHealthService;
        this.whatsAppOtpService = whatsAppOtpService;
    }

    @PostMapping("/register-sync")
    public ResponseEntity<?> registerSync(@RequestBody RegisterSyncRequest request) {
        // ... (Kode tetap sama)
        try {
            UserHealth saved = userHealthService.registerOrUpdate(request);
            String username = userHealthService.resolveUsername(saved);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", saved.getId());
            data.put("username", username);
            data.put("user_name", username);
            data.put("fullName", saved.getFullName());
            data.put("full_name", saved.getFullName());
            data.put("email", saved.getEmail());
            data.put("phone", saved.getPhone());
            data.put("firebaseUid", saved.getFirebaseUid());

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", "ok");
            resp.put("data", data);
            resp.putAll(data);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body("Gagal Sinkronisasi: " + ex.getMessage());
        }
    }

    @PostMapping("/login-check")
    public ResponseEntity<?> loginCheck(@RequestBody Map<String, String> request) {
        // ... (Kode tetap sama)
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.isBlank()) {
                return ResponseEntity.badRequest().body("Token Firebase tidak ditemukan");
            }
            boolean isRegistered = userHealthService.checkUserExists(idToken);
            Map<String, Object> resp = new LinkedHashMap<>();
            if (isRegistered) {
                resp.put("status", "ok");
                resp.put("isRegistered", true);
                resp.put("message", "Akun ditemukan, silakan masuk.");
                return ResponseEntity.ok(resp);
            } else {
                resp.put("status", "not_found");
                resp.put("isRegistered", false);
                resp.put("message", "Akun belum terdaftar.");
                return ResponseEntity.status(404).body(resp);
            }
        } catch (Exception ex) {
            return ResponseEntity.status(401).body("Akses ditolak: " + ex.getMessage());
        }
    }

    @PostMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestBody Map<String, String> request) {
        // ... (Kode tetap sama)
        try {
            String phone = request.get("phone");
            if (phone == null || phone.isBlank()) return ResponseEntity.badRequest().body("Nomor kosong");
            boolean isRegistered = userHealthService.checkPhoneIsRegistered(phone);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", "ok");
            resp.put("isRegistered", isRegistered);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Error: " + ex.getMessage());
        }
    }

    // 🚀 ENDPOINT BARU 1: Meminta OTP WA (Ditembak saat Flutter klik tombol)
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            if (phone == null || phone.isBlank()) {
                return ResponseEntity.badRequest().body("Nomor telepon tidak boleh kosong");
            }

            // Minta WhatsAppOtpService mengirim pesan
            whatsAppOtpService.requestOtp(phone);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", "ok");
            resp.put("message", "OTP berhasil dikirim ke WhatsApp");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Terjadi kesalahan server: " + ex.getMessage());
        }
    }

    // 🚀 ENDPOINT BARU 2: Memvalidasi OTP WA dan memberikan Token
    @PostMapping("/verify-wa-otp")
    public ResponseEntity<?> verifyWaOtp(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String otp = request.get("otp");

            boolean isValid = whatsAppOtpService.verifyOtp(phone, otp);

            if (isValid) {
                // Jika benar, buatkan tiket VIP Firebase
                String customToken = userHealthService.generateCustomTokenForPhone(phone);

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("status", "ok");
                resp.put("customToken", customToken);
                resp.put("message", "Login berhasil");
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(401).body("Kode OTP salah atau sudah kedaluwarsa");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Terjadi kesalahan server: " + ex.getMessage());
        }
    }
}