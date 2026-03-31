package com.banco.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteRequest {
    

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    // Constructores
    public ClienteRequest() {}
    
    public ClienteRequest(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
