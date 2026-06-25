package com.natania.protel_backend;

import lombok.Data;

@Data
public class RegisterDeviceTokenRequest {
    private Long userId;
    private String deviceToken;
}
