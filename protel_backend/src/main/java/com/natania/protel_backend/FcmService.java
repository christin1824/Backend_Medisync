package com.natania.protel_backend;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FcmService {

    public void sendEmergencyNotification(
        String token,
        String title,
        String message,
        String type,
        String value,
        double lat,
        double lon,
        LocalDateTime timestamp) {

    String safeTimestamp = timestamp != null ? timestamp.toString() : LocalDateTime.now().toString();

        Message fcmMessage = Message.builder()
        .setToken(token)
        .putData("type", type)
        .putData("value", value)
        .putData("latitude", String.valueOf(lat))
        .putData("longitude", String.valueOf(lon))
        .putData("timestamp", safeTimestamp)
        .putData("alertTitle", title)
        .putData("alertMessage", message)
        .setAndroidConfig(AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setTtl(60 * 1000L)
            .build())
        .setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder()
                .setAlert(ApsAlert.builder()
                    .setTitle(title)
                    .setBody(message)
                    .build())
                .setSound("default")
                .setContentAvailable(true)
                .build())
            .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(fcmMessage);
            System.out.println("Berhasil mengirim notifikasi: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Gagal mengirim notifikasi: " + e.getMessage());
        }
    }
}