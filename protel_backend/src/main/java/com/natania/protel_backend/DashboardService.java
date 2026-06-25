package com.natania.protel_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    // === SUNTIKKAN FIREBASE SERVICE AGAR BACKEND BISA KIRIM SINYAL FCM ===
    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private UserHealthService userHealthService;

    @Autowired
    private UserHealthRepository userHealthRepository; 

    @Autowired
    private StepsLogRepository stepsLogRepository;

    @Autowired
    private HeartRateLogRepository heartRateLogRepository;

    @Autowired
    private OxygenLogRepository oxygenLogRepository;

    @Autowired
    private DangerEventRepository dangerEventRepository;

    @Autowired
    private RealtimeLocationRepository realtimeLocationRepository;

    @Autowired
    private LocationHistoryRepository locationHistoryRepository;

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ElderProfileRepository elderProfileRepository;

    public DashboardResponse buildDashboard(String idToken) {
        UserHealth user = userHealthService.resolveOrCreateFromIdToken(idToken);
        Long userId = user.getId();

        ElderProfile elderProfile = elderProfileRepository.findByUserHealth_Id(userId).orElse(null);
        StepsLog latestSteps = stepsLogRepository.findFirstByUserHealth_IdOrderByRecordedAtDesc(userId).orElse(null);
        HeartRateLog latestHeartRate = heartRateLogRepository.findFirstByUserHealth_IdOrderByRecordedAtDesc(userId).orElse(null);
        OxygenLog latestOxygen = oxygenLogRepository.findFirstByUserHealth_IdOrderByRecordedAtDesc(userId).orElse(null);
        DangerEvent latestDanger = dangerEventRepository.findFirstByUserHealth_IdOrderByCreatedAtDesc(userId).orElse(null);
        RealtimeLocation realtimeLocation = realtimeLocationRepository.findFirstByUserHealth_IdOrderByUpdatedAtDesc(userId).orElse(null);
        List<LocationHistory> locationHistory = locationHistoryRepository.findByUserHealth_IdOrderByRecordedAtDesc(userId);
        List<EmergencyContact> emergencyContacts = emergencyContactRepository.findByUserHealth_Id(userId);
        List<MedicalHistory> medicalHistory = medicalHistoryRepository.findByUserHealth_Id(userId);
        List<AuditLog> auditLogs = auditLogRepository.findByUserHealth_IdOrderByCreatedAtDesc(userId);

        int totalStepsToday = stepsLogRepository.getTotalStepsToday(userId);
        int targetSteps = 150;
        String username = userHealthService.resolveUsername(user);
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = elderProfile != null && elderProfile.getFullName() != null && !elderProfile.getFullName().isBlank()
                ? elderProfile.getFullName()
            : null;
        }

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("user", userHealthService.toSafeProfile(user));
        snapshot.put("elderProfile", elderProfile);
        snapshot.put("username", username);
        snapshot.put("user_name", username);
        snapshot.put("fullName", fullName);
        snapshot.put("full_name", fullName);
        String profileImageUrl = elderProfile != null ? elderProfile.getProfileImageUrl() : null;
        snapshot.put("profileImageUrl", profileImageUrl);
        snapshot.put("profile_image_url", profileImageUrl);
        snapshot.put("displayName", username);
        snapshot.put("latestSteps", latestSteps);
        snapshot.put("latestHeartRate", latestHeartRate);
        snapshot.put("latestOxygen", latestOxygen);
        snapshot.put("latestDanger", latestDanger);
        snapshot.put("realtimeLocation", realtimeLocation);
        snapshot.put("locationHistory", locationHistory);
        snapshot.put("emergencyContacts", emergencyContacts);
        snapshot.put("medicalHistory", medicalHistory);
        snapshot.put("auditLogs", auditLogs);
        snapshot.put("totalStepsToday", totalStepsToday);
        snapshot.put("targetSteps", targetSteps);
        snapshot.put("percentage", targetSteps == 0 ? 0 : (double) totalStepsToday / targetSteps);
        snapshot.put("remaining", Math.max(0, targetSteps - totalStepsToday));
        return new DashboardResponse(snapshot);
    }

    public List<Map<String, Object>> buildMetricHistory(String idToken, String type) {
        UserHealth user = userHealthService.resolveOrCreateFromIdToken(idToken);
        Long userId = user.getId();
        String normalizedType = type == null ? "" : type.trim().toUpperCase();

        List<Map<String, Object>> history = new ArrayList<>();
        switch (normalizedType) {
            case "DANGER": {
                for (DangerEvent log : dangerEventRepository.findByUserHealth_IdOrderByCreatedAtDesc(userId)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", log.getId());
                    item.put("title", log.getTitle());
                    item.put("message", log.getMessage());
                    item.put("status", log.getStatus());
                    item.put("createdAt", log.getCreatedAt());
                    history.add(item);
                }
                break;
            }
            case "HEART_RATE": {
                for (HeartRateLog log : heartRateLogRepository.findByUserHealth_IdOrderByRecordedAtDesc(userId)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", log.getId());
                    item.put("value", log.getHeartRateBpm());
                    item.put("createdAt", log.getRecordedAt());
                    item.put("status", log.getHeartRateBpm() >= 60 && log.getHeartRateBpm() <= 100 ? "Normal" : "Tidak Normal");
                    history.add(item);
                }
                break;
            }
            case "OXYGEN": {
                for (OxygenLog log : oxygenLogRepository.findByUserHealth_IdOrderByRecordedAtDesc(userId)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", log.getId());
                    item.put("value", log.getOxygenSaturation());
                    item.put("createdAt", log.getRecordedAt());
                    item.put("status", log.getOxygenStatus() != null ? log.getOxygenStatus() : (log.getOxygenSaturation() >= 95 ? "Normal" : "Tidak Normal"));
                    history.add(item);
                }
                break;
            }
            case "STEPS": {
                for (StepsLog log : stepsLogRepository.findByUserHealth_IdOrderByRecordedAtDesc(userId)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", log.getId());
                    item.put("value", log.getTotalSteps());
                    item.put("createdAt", log.getRecordedAt());
                    item.put("status", "Langkah");
                    history.add(item);
                }
                break;
            }
            case "ACTIVITY": {
                for (AuditLog log : auditLogRepository.findByUserHealth_IdOrderByCreatedAtDesc(userId)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", log.getId());
                    item.put("status", log.getAction() != null ? log.getAction() : "Aktivitas");
                    item.put("createdAt", log.getCreatedAt());
                    history.add(item);
                }
                break;
            }
            default: {
                throw new RuntimeException("Unsupported metric type: " + type);
            }
        }

        return history;
    }

    public List<Map<String, Object>> buildNotifications(String idToken) {
        UserHealth user = userHealthService.resolveOrCreateFromIdToken(idToken);
        Long userId = user.getId();

        List<Map<String, Object>> notifications = new ArrayList<>();
        for (DangerEvent dangerEvent : dangerEventRepository.findByUserHealth_IdOrderByCreatedAtDesc(userId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", dangerEvent.getId());
            item.put("title", dangerEvent.getTitle());
            item.put("message", dangerEvent.getMessage());
            item.put("status", dangerEvent.getStatus());
            item.put("eventType", dangerEvent.getEventType());
            item.put("latitude", dangerEvent.getLatitude());
            item.put("longitude", dangerEvent.getLongitude());
            item.put("createdAt", dangerEvent.getCreatedAt());
            notifications.add(item);
        }
        return notifications;
    }

    // 🚀 METHOD YANG SUDAH DIPERBARUI DENGAN JALUR VIP NODE-RED DAN PENANGANAN ACTIVITY
    public Map<String, Object> recordLog(HealthLogRequest request) {
        UserHealth user;

        // 🚀 JALUR VIP: Jika kiriman dari Node-RED (token khusus)
        if ("DUMMY_NODE_RED".equals(request.getIdToken())) {
            // Ambil UserHealth default (ID 42L). Pastikan ID ini ada di database-mu!
            user = userHealthRepository.findById(42L)
                    .orElseThrow(() -> new RuntimeException("User Default untuk Node-RED dengan ID 42 tidak ditemukan di database"));
        } else {
            // Jalur Normal: Verifikasi Firebase ID Token
            user = userHealthService.resolveForLog(request);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userHealthId", user.getId());
        response.put("type", request.getType());

        String type = request.getType() == null ? "" : request.getType().trim().toUpperCase();
        
        // 🔥 PROTEKSI DOUBLE: Jika dari request tipenya kosong tapi ada kiriman nama ruangan dari controller, paksa jadi PRESENCE 🔥
        if (type.isBlank() && request.getStatus() != null && !request.getStatus().isBlank()) {
            type = "PRESENCE";
            response.put("type", "PRESENCE");
        }

        Long objectId = null;
        String objectType = null;
        
        switch (type) {
            case "STEPS": {
                StepsLog log = new StepsLog();
                log.setUserHealth(user);
                log.setTotalSteps(request.getValue());
                stepsLogRepository.save(log);
                response.put("savedAs", "steps_logs");
                response.put("recordId", log.getId());
                objectId = log.getId();
                objectType = "steps_logs";
                break;
            }
            case "HEART_RATE": {
                HeartRateLog log = new HeartRateLog();
                log.setUserHealth(user);
                log.setHeartRateBpm(request.getValue());
                heartRateLogRepository.save(log);
                response.put("savedAs", "heart_rate_logs");
                response.put("recordId", log.getId());
                objectId = log.getId();
                objectType = "heart_rate_logs";

                if (request.getValue() > 100 || request.getValue() < 60) {
                    response.putAll(saveDangerEvent(user, request, "HIGH_HR", "GANGGUAN JANTUNG", "Detak jantung kritis: " + request.getValue() + " BPM."));
                    
                    String deviceToken = user.getDeviceToken();
                    if (deviceToken != null && !deviceToken.isBlank()) {
                        // 🚀 TAMBAHKAN LATITUDE DAN LONGITUDE
                        firebaseService.sendEmergencyNotification(
                            deviceToken, 
                            "DETAK JANTUNG KRITIS!", 
                            "Detak jantung lansia tidak normal: " + request.getValue() + " BPM.", 
                            "HIGH_HR",
                            String.valueOf(request.getLatitude()),
                            String.valueOf(request.getLongitude())
                        );
                    }
                }
                break;
            }
            case "OXYGEN": {
                OxygenLog log = new OxygenLog();
                log.setUserHealth(user);
                log.setOxygenSaturation(request.getValue());
                log.setOxygenStatus(request.getStatus());
                oxygenLogRepository.save(log);
                response.put("savedAs", "oxygen_logs");
                response.put("recordId", log.getId());
                objectId = log.getId();
                objectType = "oxygen_logs";

                if (request.getValue() < 85) {
                    response.putAll(saveDangerEvent(user, request, "LOW_SPO2", "OKSIGEN KRITIS", "Saturasi oksigen sangat rendah: " + request.getValue() + "%."));
                    
                    String deviceToken = user.getDeviceToken();
                    if (deviceToken != null && !deviceToken.isBlank()) {
                        // 🚀 TAMBAHKAN LATITUDE DAN LONGITUDE
                        firebaseService.sendEmergencyNotification(
                            deviceToken, 
                            "OKSIGEN KRITIS!", 
                            "Saturasi oksigen lansia drop di angka: " + request.getValue() + "%.", 
                            "LOW_SPO2",
                            String.valueOf(request.getLatitude()),
                            String.valueOf(request.getLongitude())
                        );
                    }
                }
                break;
            }
            
            // 🔥 JALUR PRESENCE: AMBIL MURNI TEXT STATUS DARI CONTROLLER BYPASS VALIDASI VALUE 🔥
            case "PRESENCE": {
                // Gunakan findFirst...OrderByBy... agar jika ada data banyak, Java hanya mengambil 1 data paling update!
                RealtimeLocation realtimeLocation = realtimeLocationRepository
                    .findFirstByUserHealth_IdOrderByUpdatedAtDesc(user.getId())
                    .orElse(null);
                
                // Jika user ini bener-bener belum punya baris data, baru buat objek baru
                if (realtimeLocation == null) {
                    realtimeLocation = new RealtimeLocation();
                    realtimeLocation.setUserHealth(user);
                }
                
                // 🚀 LANGSUNG SET: Ambil apa pun teks yang dibawa dari Controller (TW-601 / Luar Ruangan)
                String lokasiFix = (request.getStatus() != null && !request.getStatus().isBlank()) 
                    ? request.getStatus().trim() 
                    : "Luar Ruangan";
                    
                realtimeLocation.setIndoorLocation(lokasiFix);
                
                if (request.getLatitude() != 0.0) realtimeLocation.setLatitude(request.getLatitude());
                if (request.getLongitude() != 0.0) realtimeLocation.setLongitude(request.getLongitude());
                
                // Paksa update timestamp agar Flutter tahu ada data masuk baru
                realtimeLocation.setUpdatedAt(java.time.LocalDateTime.now()); 
                
                realtimeLocationRepository.save(realtimeLocation);

                if (request.getLatitude() != 0.0 || request.getLongitude() != 0.0) {
                    LocationHistory locationHistory = new LocationHistory();
                    locationHistory.setUserHealth(user);
                    locationHistory.setLatitude(request.getLatitude());
                    locationHistory.setLongitude(request.getLongitude());
                    locationHistoryRepository.save(locationHistory);
                    response.put("locationSaved", true);
                }

                response.put("savedAs", "realtime_locations");
                response.put("indoorLocationSaved", true);
                
                // 🚀 POTONG JALUR LANGSUNG: Jaminan anti-bocor ke laci default di bawah
                return response; 
            }

            // 🚀 JALUR KHUSUS OUTDOOR: Menyimpan data GPS tanpa memicu Danger Event!
            case "LOCATION_TRACKING": {
                // 1. Simpan lokasi terkini ke RealtimeLocation biar data koordinat terupdate
                RealtimeLocation realtimeLocation = realtimeLocationRepository
                    .findFirstByUserHealth_IdOrderByUpdatedAtDesc(user.getId())
                    .orElse(null);
                
                if (realtimeLocation == null) {
                    realtimeLocation = new RealtimeLocation();
                    realtimeLocation.setUserHealth(user);
                }
                
                // Set status murni ke "Luar Ruangan" karena ini tracking GPS luar rumah
                realtimeLocation.setIndoorLocation("Luar Ruangan");
                if (request.getLatitude() != 0.0) realtimeLocation.setLatitude(request.getLatitude());
                if (request.getLongitude() != 0.0) realtimeLocation.setLongitude(request.getLongitude());
                realtimeLocation.setUpdatedAt(java.time.LocalDateTime.now());
                realtimeLocationRepository.save(realtimeLocation);

                // 2. Simpan ke riwayat tabel location_history untuk titik pergerakan peta
                if (request.getLatitude() != 0.0 || request.getLongitude() != 0.0) {
                    LocationHistory locationHistory = new LocationHistory();
                    locationHistory.setUserHealth(user);
                    locationHistory.setLatitude(request.getLatitude());
                    locationHistory.setLongitude(request.getLongitude());
                    
                    // Masukkan teks tempat (misal: BLSDM Komdigi Surabaya) ke kolom location_name database!
                    if (request.getStatus() != null && !request.getStatus().isBlank()) {
                        locationHistory.setLocationName(request.getStatus().trim());
                    }
                    
                    locationHistoryRepository.save(locationHistory);
                    response.put("locationSaved", true);
                }

                response.put("savedAs", "location_history");
                response.put("status", "success");
                
                // Potong jalur biar gak merosot ke default (Danger Event)
                return response; 
            }
            
            // 🚀 FIX: JALUR KHUSUS ACTIVITY YANG BISA DETEKSI JATUH PINTAR 🚀
            case "ACTIVITY": {
                String aksi = request.getStatus() != null ? request.getStatus().toUpperCase() : "";
                
                // 1. Cek apakah statusnya mengandung kata JATUH atau FALL
                if (aksi.contains("JATUH") || aksi.contains("FALL")) {
                    Map<String, Object> danger = saveDangerEvent(user, request, "FALL", "LANSIA JATUH", "Peringatan! Lansia terdeteksi jatuh.");
                    response.putAll(danger);
                    
                    Object dangerEventId = danger.get("dangerEventId");
                    if (dangerEventId instanceof Long) {
                        objectId = (Long) dangerEventId;
                        objectType = "danger_events";
                    }

                    String deviceToken = user.getDeviceToken();
                    if (deviceToken != null && !deviceToken.isBlank()) {
                        // 🚀 TAMBAHKAN LATITUDE DAN LONGITUDE
                        firebaseService.sendEmergencyNotification(
                            deviceToken, 
                            "LANSIA JATUH!", 
                            "Terdeteksi jatuh. Segera cek lokasi!", 
                            "FALL",
                            String.valueOf(request.getLatitude()),
                            String.valueOf(request.getLongitude())
                        );
                    }
                } 
                // 2. Jika bukan jatuh (misal: DUDUK, JALAN), catat diam-diam sebagai log biasa
                else {
                    response.put("savedAs", "audit_log");
                    response.put("message", "Aktivitas tercatat: " + request.getStatus());
                }
                break;
            }
            
            case "FALL":
            case "SOS":
            default: {
                String dangerType = type.isBlank() ? "EVENT" : type;
                Map<String, Object> danger = saveDangerEvent(user, request, dangerType, "LANSIA JATUH", "Terdeteksi jatuh. Segera cek lokasi!");
                response.putAll(danger);
                Object dangerEventId = danger.get("dangerEventId");
                if (dangerEventId instanceof Long) {
                    objectId = (Long) dangerEventId;
                    objectType = "danger_events";
                }

                String deviceToken = user.getDeviceToken();
                if (deviceToken != null && !deviceToken.isBlank()) {
                    // 🚀 TAMBAHKAN LATITUDE DAN LONGITUDE
                    firebaseService.sendEmergencyNotification(
                        deviceToken, 
                        dangerType.equals("SOS") ? "TOMBOL SOS DITEKAN!" : "LANSIA JATUH!", 
                        dangerType.equals("SOS") ? "Lansia memerlukan bantuan darurat SOS!" : "Terdeteksi jatuh. Segera cek lokasi!", 
                        dangerType,
                        String.valueOf(request.getLatitude()),
                        String.valueOf(request.getLongitude())
                    );
                }
                break;
            }
        }

        // 🔥 FIX MUTAKHIR JALUR DOUBLE: Tambahkan syarat !type.equals("PRESENCE") agar tidak menimpa data yang sudah disimpan di atas! 🔥
        if (!type.equals("PRESENCE") && (request.getLatitude() != 0.0 || request.getLongitude() != 0.0)) {
            RealtimeLocation realtimeLocation = new RealtimeLocation();
            realtimeLocation.setUserHealth(user);
            realtimeLocation.setLatitude(request.getLatitude());
            realtimeLocation.setLongitude(request.getLongitude());
            realtimeLocationRepository.save(realtimeLocation);

            LocationHistory locationHistory = new LocationHistory();
            locationHistory.setUserHealth(user);
            locationHistory.setLatitude(request.getLatitude());
            locationHistory.setLongitude(request.getLongitude());
            locationHistoryRepository.save(locationHistory);

            response.put("locationSaved", true);
        }

        // Cetak AuditLog aktivitas harian reguler
        AuditLog auditLog = new AuditLog();
        auditLog.setUserHealth(user);

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            auditLog.setAction(request.getStatus().trim());
        } else {
            auditLog.setAction("LOG_CREATED");
        }

        auditLog.setObjectType(objectType);
        auditLog.setObjectId(objectId);

        String jsonValid = "{\"type\": \"" + request.getType() + "\", \"value\": " + request.getValue() + "}";
        auditLog.setDetails(jsonValid);

        auditLogRepository.save(auditLog);

        return response;
    }

    private Map<String, Object> saveDangerEvent(UserHealth user, HealthLogRequest request, String dangerType, String title, String message) {
        DangerEvent dangerEvent = new DangerEvent();
        dangerEvent.setUserHealth(user);
        dangerEvent.setEventType(dangerType);
        dangerEvent.setStatus(request.getStatus());
        dangerEvent.setLatitude(request.getLatitude());
        dangerEvent.setLongitude(request.getLongitude());
        dangerEvent.setTitle(title);
        dangerEvent.setMessage(message);
        dangerEventRepository.save(dangerEvent);

        Map<String, Object> response = new HashMap<>();
        response.put("savedAs", "danger_events");
        response.put("dangerEventId", dangerEvent.getId());
        response.put("alertTitle", title);
        response.put("alertMessage", message);
        return response;
    }

    public Map<String, Object> addEmergencyContact(EmergencyContactRequest request) {
        Map<String, Object> response = new HashMap<>();

        UserHealth user = userHealthRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User lansia dengan ID " + request.getUserId() + " tidak ditemukan"));

        EmergencyContact contact = new EmergencyContact();
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setRelation(request.getRelation());
        contact.setUserHealth(user);

        emergencyContactRepository.save(contact);

        response.put("status", "success");
        response.put("message", "Kontak darurat berhasil disimpan!");
        response.put("contactId", contact.getId());
        response.put("userHealthId", user.getId());

        return response;
    }

    public void deleteActivityLog(Long logId, String idToken) {
        userHealthService.resolveOrCreateFromIdToken(idToken);
        if (auditLogRepository.existsById(logId)) {
            auditLogRepository.deleteById(logId);
        } else {
            throw new RuntimeException("Log record tidak ditemukan di database");
        }
    }

    // === FIX SINKRONISASI: MENYIMPAN REKAM MEDIS SECARA BENAR DAN AKURAT ===
    public Map<String, Object> saveMedicalHistory(HealthLogRequest request) {
        UserHealth user = userHealthRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User lansia dengan ID " + request.getUserId() + " tidak ditemukan"));

        MedicalHistory history = new MedicalHistory();
        history.setUserHealth(user);
        
        history.setAge(request.getAge());
        history.setBloodType(request.getBloodType());
        history.setBaselineBpm(request.getBaselineBpm());
        history.setBaselineSpo2(request.getBaselineSpo2());
        
        // 🚀 FIX MUTAKHIR: Ditata membaca langsung dari properti request.getDescription() murni!
        history.setDescription(request.getDescription()); 
        
        history.setAllergies(request.getAllergies());

        medicalHistoryRepository.save(history);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Data Kesehatan Lansia Berhasil Diperbarui!");
        response.put("medicalHistoryId", history.getId());
        
        return response;
    }
}