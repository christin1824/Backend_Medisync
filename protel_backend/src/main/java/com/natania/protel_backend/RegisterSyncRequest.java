package com.natania.protel_backend;

import lombok.Data;

@Data
public class RegisterSyncRequest {
    private String idToken;
    private String username;
    private String name;
    private String email;
    private String phone;
}
