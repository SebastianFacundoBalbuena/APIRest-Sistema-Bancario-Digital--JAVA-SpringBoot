package com.banco.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    private String username;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;
    
    private String rol = "USER"; // Por defecto
    
    private String clienteId; 
    
    // Constructores
    public RegisterRequest() {}

    public RegisterRequest(
        @NotBlank(message = "El usuario es obligatorio") 
        @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres") String username,
        @NotBlank(message = "La contraseña es obligatoria") 
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
        @NotBlank(message = "El email es obligatorio") 
        @Email(message = "El email no es válido") String email,
        String rol, String clienteId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.rol = rol;
        this.clienteId = clienteId;
    }



    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
}
