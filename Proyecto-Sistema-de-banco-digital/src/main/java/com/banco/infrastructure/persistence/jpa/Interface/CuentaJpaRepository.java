package com.banco.infrastructure.persistence.jpa.Interface;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.infrastructure.persistence.entities.CuentaEntity;





    // INTERNFAZ para adquirir metodo CRUD JPA
public interface CuentaJpaRepository extends JpaRepository<CuentaEntity, UUID> {
    
        // Esta interfaz hereda de JPA, por lo cual podemos usar sus palabras clave
        // COMO existsBy - findBy etc + nombre del atributo
        // JPA ya conoce estas palabras y las detecta automaticamente sabiendo que queremos
        Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta);


        boolean existsByNumeroCuenta(String numeroCuenta);

        List<CuentaEntity> findByClienteId(String clienteId);
}
