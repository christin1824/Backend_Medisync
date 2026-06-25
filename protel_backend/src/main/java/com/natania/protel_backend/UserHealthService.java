package com.natania.protel_backend;

import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserHealthService {

    @Autowired
    private UserHealthRepository healthRepository;

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private ElderProfileRepository elderProfileRepository;

    // === CONFIGURATION INJECTIONS FROM APPLICATION.PROPERTIES ===
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    public UserHealth resolveOrCreateFromIdToken(String idToken) {
        FirebaseToken decoded = firebaseService.verifyIdToken(idToken);
        String uid = decoded.getUid();
        String email = decoded.getEmail();

        Optional<UserHealth> existing = healthRepository.findByFirebaseUid(uid);
        if (existing.isEmpty() && email != null && !email.isBlank()) {
            existing = healthRepository.findByEmail(email);
        }

        UserHealth user = existing.orElseGet(UserHealth::new);
        user.setFirebaseUid(uid);
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            ElderProfile existingProfile = existing.flatMap(value -> elderProfileRepository.findByUserHealth_Id(value.getId())).orElse(null);
            if (existingProfile != null && existingProfile.getFullName() != null && !existingProfile.getFullName().isBlank()) {
                user.setFullName(existingProfile.getFullName());
            }
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (user.getDeviceToken() == null) {
            user.setDeviceToken("");
        }

        UserHealth saved = healthRepository.save(user);
        ensureElderProfile(saved);
        return saved;
    }

    // 🚀 FUNGSI BARU: Khusus untuk mengecek apakah user sudah terdaftar atau belum (Tanpa save otomatis)
    public boolean checkUserExists(String idToken) {
        try {
            FirebaseToken decoded = firebaseService.verifyIdToken(idToken);
            String uid = decoded.getUid();
            String email = decoded.getEmail();

            Optional<UserHealth> existing = healthRepository.findByFirebaseUid(uid);
            if (existing.isEmpty() && email != null && !email.isBlank()) {
                existing = healthRepository.findByEmail(email);
            }

            // Return true kalau ada isinya, return false kalau kosong
            return existing.isPresent();
        } catch (Exception e) {
            return false; // Kalau token kadaluarsa atau rusak, anggap belum terdaftar
        }
    }

    public UserHealth registerOrUpdate(RegisterSyncRequest request) {
        UserHealth user = resolveOrCreateFromIdToken(request.getIdToken());
        if (!StringUtils.hasText(user.getUsername()) && StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername().trim());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setFullName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        UserHealth saved = healthRepository.save(user);
        ensureElderProfile(saved);
        return saved;
    }

    public UserHealth updateProfile(UpdateProfileRequest request) {
        UserHealth user = resolveOrCreateFromIdToken(request.getIdToken());
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setFullName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        UserHealth saved = healthRepository.save(user);
        ensureElderProfile(saved, request.getProfileImageUrl());
        return saved;
    }

    public void updateProfileImage(UserHealth user, String profileImageUrl) {
        ensureElderProfile(user, profileImageUrl);
    }

    public UserHealth registerDeviceToken(Long userId, String deviceToken) {
        UserHealth user = healthRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeviceToken(deviceToken);
        return healthRepository.save(user);
    }

    public UserHealth resolveByUserId(Long userId) {
        return healthRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserHealth resolveForLog(HealthLogRequest request) {
        if (request.getIdToken() != null && !request.getIdToken().isBlank()) {
            return resolveOrCreateFromIdToken(request.getIdToken());
        }

        if (request.getUserId() != null) {
            return resolveByUserId(request.getUserId());
        }

        if (request.getFirebaseUid() != null && !request.getFirebaseUid().isBlank()) {
            return healthRepository.findByFirebaseUid(request.getFirebaseUid())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        throw new RuntimeException("Missing user context");
    }

    public Map<String, Object> toSafeProfile(UserHealth user) {
        Map<String, Object> profile = new HashMap<>();
        String username = resolveUsername(user);
        profile.put("id", user.getId());
        profile.put("firebaseUid", user.getFirebaseUid());
        profile.put("username", username);
        profile.put("user_name", username);
        String fullName = user.getFullName();
        if (fullName == null || fullName.isBlank()) {
            Thread localThread = Thread.currentThread();
            ElderProfile elderProfile = user.getId() == null ? null : elderProfileRepository.findByUserHealth_Id(user.getId()).orElse(null);
            if (elderProfile != null && elderProfile.getFullName() != null && !elderProfile.getFullName().isBlank()) {
                fullName = elderProfile.getFullName();
            }
        }
        profile.put("fullName", fullName);
        profile.put("full_name", fullName);
        String profileImageUrl = null;
        if (user.getId() != null) {
            ElderProfile elderProfile = elderProfileRepository.findByUserHealth_Id(user.getId()).orElse(null);
            if (elderProfile != null) {
                profileImageUrl = elderProfile.getProfileImageUrl();
            }
        }
        profile.put("profileImageUrl", profileImageUrl);
        profile.put("profile_image_url", profileImageUrl);
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("deviceToken", user.getDeviceToken());
        return profile;
    }

    private void ensureElderProfile(UserHealth user) {
        ensureElderProfile(user, null);
    }

    private void ensureElderProfile(UserHealth user, String profileImageUrl) {
        if (user == null || user.getId() == null) {
            return;
        }

        ElderProfile profile = elderProfileRepository.findByUserHealth_Id(user.getId()).orElse(null);
        if (profile == null) {
            profile = new ElderProfile();
            profile.setUserHealth(user);
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            profile.setFullName(user.getFullName());
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            profile.setPhone(user.getPhone());
        } else if (profile.getPhone() == null || profile.getPhone().isBlank()) {
            profile.setPhone(user.getPhone());
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            profile.setProfileImageUrl(profileImageUrl);
        }
        elderProfileRepository.save(profile);
    }

    // === AMAN: LOGIKA BARU UNTUK UPLOAD INSTAN LANGSUNG KE SUPABASE STORAGE BUCKET ===
    public Map<String, Object> updateProfileImageFromUpload(String idToken, MultipartFile image, String publicBaseUrlUnused) throws Exception {
        // 1. Validasi token Firebase untuk mencari user yang sedang login
        UserHealth user = resolveOrCreateFromIdToken(idToken);
        Long userId = user.getId();

        // 2. Siapkan nama file unik agar tidak tabrakan di Supabase
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + extension;

        // 3. Alur Upload ke REST API Supabase Storage
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + supabaseBucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.parseMediaType(image.getContentType()));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(image.getBytes(), headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Tembak POST ke Supabase
            restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            // Jika file sudah ada, coba timpa menggunakan PUT
            restTemplate.exchange(uploadUrl, HttpMethod.PUT, requestEntity, String.class);
        }

        // 4. Buat URL Publik dari Supabase untuk disimpan ke database
        String publicImageUrl = supabaseUrl + "/storage/v1/object/public/" + supabaseBucket + "/" + fileName;

        // 5. Simpan URL ini ke tabel ElderProfile milik user
        ElderProfile elderProfile = elderProfileRepository.findByUserHealth_Id(userId)
                .orElse(new ElderProfile());
        elderProfile.setUserHealth(user);
        elderProfile.setProfileImageUrl(publicImageUrl);
        elderProfileRepository.save(elderProfile);

        // 6. Kembalikan respons sukses ke Flutter
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("profileImageUrl", publicImageUrl);
        return response;
    }

    public String resolveUsername(UserHealth user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return null;
    }

    // 🚀 FUNGSI BARU: Mengecek ketersediaan nomor HP untuk OTP
    public boolean checkPhoneIsRegistered(String phone) {
        // Menggunakan healthRepository karena itu yang di-@Autowired di bagian atas file kamu
        return healthRepository.existsByPhone(phone); 
    }
    
    // 🚀 FUNGSI BARU: Bikin Firebase Custom Token jika OTP WhatsApp Benar
    public String generateCustomTokenForPhone(String phone) {
        UserHealth user = healthRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        try {
            String uid = user.getFirebaseUid();
            // Jaga-jaga jika ada user lama yang belum punya UID Firebase
            if (uid == null || uid.isBlank()) {
                uid = "wa_" + user.getId(); 
            }
            return FirebaseAuth.getInstance().createCustomToken(uid);
        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat Firebase Custom Token", e);
        }
    }

}