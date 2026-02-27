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
        List<TransaccionEntity> findByCuentaOrigenIdOrCuentaDestinoId(String cuentaOrigen, String cuentaDestino);

        List<TransaccionEntity> findByCuentaOrigenId(String numeroCuenta);
        
        List<TransaccionEntity> findByReferenciaContainingIgnoreCase(String referencia);

    @Query("SELECT t FROM TransaccionEntity t WHERE " +
           "(t.cuentaOrigenId = :cuentaId OR t.cuentaDestinoId = :cuentaId) " +
           "AND t.fechaDeCreacion BETWEEN :desde AND :hasta")
    List<TransaccionEntity> findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween(
        @Param("cuentaId") String cuentaId,
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta);

            
}
