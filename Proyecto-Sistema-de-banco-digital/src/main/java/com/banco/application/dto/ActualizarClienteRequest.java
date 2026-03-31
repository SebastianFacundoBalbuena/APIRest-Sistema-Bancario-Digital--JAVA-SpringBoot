package com.banco.application.dto;

import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;




public class ActualizarClienteRequest {
    
    @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
    private String nombre;
    
    @Email(message = "El formato del email no es válido")
    private String email;
    
    private Boolean activo; // Boolean para poder distinguir null de false
    


    // Getters que devuelven Optional para manejar nulls y Setters 
    public Optional<String> getNombre() {  return Optional.ofNullable(nombre);  }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public Optional<String> getEmail() {  return Optional.ofNullable(email);  }
    public void setEmail(String email) { this.email = email; }
    
    public Optional<Boolean> getActivo() {  return Optional.ofNullable(activo);  }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    
    
    
}
