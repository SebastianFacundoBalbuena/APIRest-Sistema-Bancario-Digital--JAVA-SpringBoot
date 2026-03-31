package com.banco.infrastructure.persistence.jpa.Interface;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.infrastructure.persistence.entities.ClienteEntity;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID> {
    // Esta interfaz hereda de JPA, por lo cual podemos usar sus palabras clave
    // COMO existsBy - findBy etc + nombre del atributo
    // JPA ya conoce estas palabras y las detecta automaticamente sabiendo que queremos

    Optional<ClienteEntity> findByClienteId(String clienteId);

    boolean existsByEmail(String email);
        
    }
