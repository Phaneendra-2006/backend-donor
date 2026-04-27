package com.foodwaste.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    private String token;
    
    // Explicit getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
