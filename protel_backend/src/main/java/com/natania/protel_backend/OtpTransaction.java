package com.natania.protel_backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_transactions")
@Data
public class OtpTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    
    @Column(name = "otp_code")
    private String otpCode;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @Column(name = "is_used")
    private boolean isUsed = false;
}