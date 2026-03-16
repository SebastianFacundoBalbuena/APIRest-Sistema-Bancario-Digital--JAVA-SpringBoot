package com.banco.infrastructure.persistence.entities;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;






@Entity
@Table(name = "usuarios")
public class UsuarioEntity implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;


    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String rol;

    @Column(name = "cliente_id")
    private String clienteId;


    // Constructor vacío para JPA
    public UsuarioEntity() {
    }




    public UsuarioEntity(UUID id, String username, String password, String email, String rol, String clienteId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.rol = rol;
        this.clienteId = clienteId;
    }



    // Implementación de UserDetails (Spring Security) interfaz

    @Override // devuelve los roles 
    public Collection<? extends GrantedAuthority> getAuthorities(){ // GrantedAuthority = interfaz = contiene getAuthorities

        return List.of(new SimpleGrantedAuthority("ROLE_" + rol)); // SimpleGrantedAuthority = asigna el return de getAuthorities
    }


    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    //La cuenta no expiró?
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    // la cuenta  no esta bloqueada?
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    //la credencial no expiro?
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    // esta activa?
    @Override
    public boolean isEnabled() {
        return true;
    }



    // GETTERS Y SETTERS

    public UUID getId() { return id;}
    public void setId(UUID id) {this.id = id;}


    
    public void setUsername(String username) {this.username = username;}


    
    public void setPassword(String password) {this.password = password; }


    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}


    public String getRol() {return rol;}
    public void setRol(String rol) {this.rol = rol;}


    public String getClienteId() {return clienteId;}
    public void setClienteId(String clienteId) {this.clienteId = clienteId;}


    


    
       

}
