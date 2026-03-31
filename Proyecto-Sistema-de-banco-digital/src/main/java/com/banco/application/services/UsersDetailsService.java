package com.banco.application.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.banco.infrastructure.persistence.entities.UsuarioEntity;
import com.banco.infrastructure.persistence.jpa.Interface.UsuarioJpaRepository;



@Service
public class UsersDetailsService implements UserDetailsService{
    
    private UsuarioJpaRepository usuarioJpaRepository;


    public UsersDetailsService(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UsuarioEntity usuarioEntity = usuarioJpaRepository.findByUsername(username)
        .orElseThrow(()-> new UsernameNotFoundException(
        "Usuario no encontrado " + username));

        return usuarioEntity;

    }



    
}
