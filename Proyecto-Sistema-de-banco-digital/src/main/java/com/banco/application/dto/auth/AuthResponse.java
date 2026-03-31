package com.banco.application.dto.auth;




public class AuthResponse {


    private String token;
    private String tipo = "Bearer";
    private String username;
    private String email;
    private String rol;
    
    public AuthResponse(String token, String username, String email, String rol) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.rol = rol;
    }
    
    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
}
