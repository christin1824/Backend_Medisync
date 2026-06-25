package com.natania.protel_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping; // <--- AMAN: Import ini wajib ada agar tidak error
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "Authorization")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserHealthService userHealthService;

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @PostMapping("/me")
    public ResponseEntity<?> getCurrentUserDashboard(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body("idToken is required");
        }

        try {
            return ResponseEntity.ok(dashboardService.buildDashboard(idToken));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @GetMapping("/elder-profile")
    public ResponseEntity<?> getElderProfile(@RequestParam("idToken") String idToken) {
        try {
            DashboardResponse dashboard = dashboardService.buildDashboard(idToken);
            Map<String, Object> data = dashboard.getData();
            return ResponseEntity.ok(data.get("elderProfile"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PostMapping("/register-device-token")
    public ResponseEntity<?> registerDeviceToken(@RequestBody RegisterDeviceTokenRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        if (request.getDeviceToken() == null || request.getDeviceToken().isBlank()) {
            return ResponseEntity.badRequest().body("deviceToken is required");
        }

        UserHealth saved = userHealthService.registerDeviceToken(request.getUserId(), request.getDeviceToken());
        return ResponseEntity.ok(userHealthService.toSafeProfile(saved));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            return ResponseEntity.badRequest().body("Missing idToken");
        }

        try {
            UserHealth saved = userHealthService.updateProfile(request);
            return ResponseEntity.ok(userHealthService.toSafeProfile(saved));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PostMapping(value = "/upload-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(@RequestParam("idToken") String idToken,
                                                @RequestParam("image") MultipartFile image) {
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body("idToken is required");
        }
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body("image is required");
        }

        try {
            String publicBaseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            return ResponseEntity.ok(userHealthService.updateProfileImageFromUpload(idToken, image, publicBaseUrl));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PostMapping("/log")
    public ResponseEntity<?> saveLog(@RequestBody HealthLogRequest request) {
        try {
            return ResponseEntity.ok(dashboardService.recordLog(request));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @GetMapping("/history/{type}")
    public ResponseEntity<?> getHistory(@PathVariable("type") String type, @RequestParam("idToken") String idToken) {
        try {
            return ResponseEntity.ok(dashboardService.buildMetricHistory(idToken, type));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam("idToken") String idToken) {
        try {
            return ResponseEntity.ok(dashboardService.buildNotifications(idToken));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    // === AMAN: ENDPOINT APIS DELETION BARU UNTUK HAPUS LOG AKTIVITAS ===
    @DeleteMapping("/log/{id}")
    public ResponseEntity<?> deleteLog(@PathVariable("id") Long id, @RequestParam("idToken") String idToken) {
        try {
            dashboardService.deleteActivityLog(id, idToken);
            return ResponseEntity.ok(Map.of("success", true, "message", "Log berhasil dihapus"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }
}