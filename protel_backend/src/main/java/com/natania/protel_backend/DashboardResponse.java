package com.natania.protel_backend;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class DashboardResponse {
    private Map<String, Object> data;

    public DashboardResponse(Map<String, Object> data) {
        this.data = data;
    }
}