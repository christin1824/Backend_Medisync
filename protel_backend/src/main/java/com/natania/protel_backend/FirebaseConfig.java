package com.natania.protel_backend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount;
            
            // 1. Coba ambil dari Environment Variable (untuk di Server / Railway)
            String firebaseJson = System.getenv("FIREBASE_CONFIG_JSON");

            if (firebaseJson != null && !firebaseJson.isEmpty()) {
                // Jika env variable ada isinya, ubah string JSON menjadi InputStream
                serviceAccount = new ByteArrayInputStream(firebaseJson.getBytes(StandardCharsets.UTF_8));
                System.out.println("Firebase: Membaca konfigurasi dari Environment Variable.");
            } else {
                // 2. Jika env kosong (untuk di lokal/laptop), baca dari file resources asli
                serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("firebase-service-account.json");
                System.out.println("Firebase: Membaca konfigurasi dari file lokal (resources).");
            }

            if (serviceAccount == null) {
                throw new IOException("Kredensial Firebase tidak ditemukan di Environment Variable maupun folder resources!");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase berhasil diinisialisasi!");
            }
        } catch (IOException e) {
            System.err.println("Gagal menginisialisasi Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}