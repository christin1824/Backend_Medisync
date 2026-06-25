package com.natania.protel_backend;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    public FirebaseToken verifyIdToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid idToken", e);
        }
    }

    public String extractUid(String idToken) {
        return verifyIdToken(idToken).getUid();
    }

    // === TAMBAHAN: ENDPOINT PENGIRIMAN NOTIFIKASI SOS/FALL UNTUK POP-UP ALARM ===
    // 🚀 Parameter latitude dan longitude ditambahkan di sini
    public void sendEmergencyNotification(String deviceToken, String title, String message, String type, String latitude, String longitude) {
        // Bangun payload FCM
        Message fcmMessage = Message.builder()
            .setToken(deviceToken)
            // 🚀 Kunci diubah agar cocok dengan penangkap data di Flutter (main.dart)
            .putData("alertTitle", title)
            .putData("alertMessage", message)
            .putData("type", type) // Dikirim sebagai "FALL" atau "SOS"
            // 🚀 KOORDINAT DIMASUKKAN KE DALAM PAYLOAD FCM
            .putData("latitude", latitude != null ? latitude : "0.0")
            .putData("longitude", longitude != null ? longitude : "0.0")
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH) // Wajib HIGH biar instan mendarat menembus background
                .build())
            .build();

        try {
            FirebaseMessaging.getInstance().send(fcmMessage);
            System.out.println("FCM Emergency Alert sukses dikirim! Lat: " + latitude + ", Lng: " + longitude);
        } catch (Exception e) {
            System.out.println("Gagal mengirim FCM: " + e.getMessage());
        }
    }
}