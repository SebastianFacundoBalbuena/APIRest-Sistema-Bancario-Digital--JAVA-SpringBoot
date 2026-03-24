package com.banco.application.services;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.banco.application.dto.auth.AuthResponse;
import com.banco.application.dto.auth.LoginRequest;
import com.banco.application.dto.auth.RegisterRequest;
import com.banco.infrastructure.persistence.entities.UsuarioEntity;
import com.banco.infrastructure.persistence.jpa.Interface.UsuarioJpaRepository;
import com.banco.infrastructure.security.jwt.JwtUtils;






@Service
public class AuthService {
    


    private final AuthenticationManager authenticationManager;
    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    public AuthService(AuthenticationManager authenticationManager, UsuarioJpaRepository usuarioRepository,
        PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }


    // Login
    public AuthResponse login(LoginRequest request){

        //autenticar usuario
        Authentication authentication = authenticationManager
        .authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(), 
                request.getPassword()));

        //guardar autenticacion
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //obtener detalles del usuario
        // nota: (UserDetails) =  especificación explícita del tipo de dato que devuelve
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();


        //generar token
        String token = jwtUtils.generarToken(userDetails);

        //obtener datos adicionales
        UsuarioEntity usuarioEntity = usuarioRepository.findByUsername(request.getUsername())
        .orElseThrow(()-> new RuntimeException("Usuario no encontrado"));

        return new AuthResponse(
            token, 
            usuarioEntity.getUsername(), 
            usuarioEntity.getEmail(), 
            usuarioEntity.getRol());

    }


    //registrar
    public String register(RegisterRequest request){

        //validar email y username unico
        if(usuarioRepository.existsByEmail(request.getEmail())) throw new IllegalArgumentException("El email ya existe");

        if(usuarioRepository.existsByUsername(request.getUsername())) throw new IllegalArgumentException("El Usuario ya existe");


        //crear usuario
        UsuarioEntity usuarioEntity = new UsuarioEntity(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()), 
            request.getEmail(), 
            request.getRol(), 
            request.getClienteId());

        
            //guardar
            usuarioRepository.save(usuarioEntity);

            return "Usuario registrado exitosamente";

    }



}
