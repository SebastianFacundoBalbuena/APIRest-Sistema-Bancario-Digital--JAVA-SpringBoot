package com.banco.infrastructure.persistence.jpa.Interface;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banco.infrastructure.persistence.entities.UsuarioEntity;


@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID>{
    
    Optional<UsuarioEntity> findByUsername(String username);
    
    Optional<UsuarioEntity> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
