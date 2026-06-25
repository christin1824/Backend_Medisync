package com.natania.protel_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class WhatsAppOtpService {

    @Autowired
    private OtpTransactionRepository otpRepository;

    // ⚠️ GANTI DENGAN TOKEN FONNTE YANG BARU SAJA KAMU COPY
    private final String FONNTE_TOKEN = "8L3WAMXib1bDAAJMdTQz";

    // Fungsi 1: Membuat dan Mengirim OTP
    public void requestOtp(String phone) {
        // Generate 6 digit angka acak
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Simpan ke database PostgreSQL
        OtpTransaction otp = new OtpTransaction();
        otp.setPhone(phone);
        otp.setOtpCode(otpCode);
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // OTP hanya berlaku 5 menit
        otpRepository.save(otp);

        // Kirim pesannya via Fonnte
        sendFonnteMessage(phone, otpCode);
    }

    // Fungsi 2: Menembak API Fonnte
    private void sendFonnteMessage(String phone, String otpCode) {
        String fonnteUrl = "https://api.fonnte.com/send";
        String message = "Halo! Kode OTP Medisync kamu adalah: *" + otpCode + "*. \n\nBerlaku selama 5 menit. Jangan berikan kode ini ke siapapun ya!";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", FONNTE_TOKEN);

        Map<String, String> body = new HashMap<>();
        body.put("target", phone);
        body.put("message", message);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.postForEntity(fonnteUrl, request, String.class);
            System.out.println("✅ OTP berhasil ditembakkan ke WhatsApp: " + phone);
        } catch (Exception e) {
            System.out.println("❌ Gagal mengirim WA: " + e.getMessage());
        }
    }

    // Fungsi 3: Memvalidasi inputan User
    public boolean verifyOtp(String phone, String otpInput) {
        return otpRepository.findTopByPhoneOrderByExpiredAtDesc(phone)
                .map(otp -> {
                    // Cek apakah sudah kadaluarsa atau sudah dipakai
                    if (otp.isUsed() || otp.getExpiredAt().isBefore(LocalDateTime.now())) {
                        return false; 
                    }
                    // Cek apakah ketikan user sama persis dengan di database
                    if (otp.getOtpCode().equals(otpInput)) {
                        otp.setUsed(true); // Langsung hanguskan OTP-nya biar gak dipakai 2x
                        otpRepository.save(otp);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }
}