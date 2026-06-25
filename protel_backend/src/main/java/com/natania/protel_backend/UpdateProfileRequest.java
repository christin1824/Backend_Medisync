package com.natania.protel_backend;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String idToken;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
}
