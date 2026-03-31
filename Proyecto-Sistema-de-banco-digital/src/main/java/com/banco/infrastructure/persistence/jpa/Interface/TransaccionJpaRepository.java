package com.banco.infrastructure.persistence.jpa.Interface;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.infrastructure.persistence.entities.TransaccionEntity;

// INTERFAZ CONTRATO
public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, UUID> {
    
        Optional<TransaccionEntity> findByTransaccionId(String transaccionId);

        //Busca transacciones donde la cuenta sea origen O destino
        // codigo SQL automatico por spring gracias a el nombre del metodo
        List<TransaccionEntity> findByCuentaOrigenIdOrCuentaDestinoId(String cuentaOrigen, String cuentaDestino);

        List<TransaccionEntity> findByCuentaOrigenId(String numeroCuenta);
        
        List<TransaccionEntity> findByReferenciaContainingIgnoreCase(String referencia);


        // @Query =  Permite escribir consultas SQL personalizadas reemplazando la del metodo por defecto
        // siempre va por encima del metodo a remplazar su consulta.
        // :nombreParametro = Vincula automáticamente el valor del parámetro
        // @Param("nombre") = @Param("nombre"): Conecta el parámetro del método con el :nombreParametro

        @Query("SELECT t FROM TransaccionEntity t WHERE " +
           "(t.cuentaOrigenId = :cuentaId OR t.cuentaDestinoId = :cuentaId) " + 
           "AND t.fechaDeCreacion BETWEEN :desde AND :hasta")
        List<TransaccionEntity> buscarPorCuentaYFechas(
        @Param("cuentaId") String cuentaId,  // @Param conecta con :cuentaId
        @Param("desde") LocalDateTime desde, // @Param conecta con :desde
        @Param("hasta") LocalDateTime hasta  // @Param conecta con :hasta
        );

            
}
