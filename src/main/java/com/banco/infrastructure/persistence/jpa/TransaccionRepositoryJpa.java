package com.banco.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.infrastructure.persistence.entities.CuentaEntity;
import com.banco.infrastructure.persistence.entities.TransaccionEntity;
import com.banco.infrastructure.persistence.mappers.TransaccionMapper;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;






@Repository
@Transactional
public class TransaccionRepositoryJpa implements TransaccionRepository {
    
    // INTERFAZ CONTRATO
    interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, UUID> {
    
        Optional<TransaccionEntity> findByTransaccionId(String transaccionId);

        //Busca transacciones donde la cuenta sea origen O destino
        List<TransaccionEntity> findByCuentaOrigenIdOrCuentaDestinoId(String cuentaOrigen, String cuentaDestino);

        List<TransaccionEntity> findByNumeroCuentas(String numeroCuenta);
        
        List<TransaccionEntity> findByReferenciaContainingIgnoreCase(String referencia);

        List<TransaccionEntity> findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween(
            String cuentaOrigenId, String cuentaDestinoId,
            LocalDateTime desde, LocalDateTime hasta);

            
    }

    private final TransaccionJpaRepository transaccionJpaRepository;
    private final TransaccionMapper transaccionMapper;

    public TransaccionRepositoryJpa(TransaccionJpaRepository transaccionJpaRepository,
            TransaccionMapper transaccionMapper) {
        this.transaccionJpaRepository = transaccionJpaRepository;
        this.transaccionMapper = transaccionMapper;
    }

    // METODOS 
    @Override
    public Optional<Transaccion> buscarPorId(TransaccionId transaccionId){

        String idTran = transaccionId.getValor();

        return transaccionJpaRepository.findByTransaccionId(idTran)
        .map(id -> transaccionMapper.aDominio(id));
    }

    @Override
    public void guardar(Transaccion transaccion){

        TransaccionEntity transaccionExistente = transaccionJpaRepository
        .findByTransaccionId(transaccion.getId().getValor()).orElse(null);
        TransaccionEntity entity = transaccionMapper.aEntity(transaccion, transaccionExistente);

        if(entity != null){
        transaccionJpaRepository.save(entity);
        System.out.println(" Transacción guardada exitosamente");
        }
    }

    @Override
    public List<Transaccion> buscarPorCuenta(Cuenta cuenta, LocalDateTime desde, LocalDateTime hasta) {
        String cuentaIdString = cuenta.getCuentaId().getValor();
        System.out.println(" Buscando transacciones de cuenta " + cuentaIdString + 
                         " entre " + desde + " y " + hasta);
        
        // Buscar donde la cuenta es origen O destino, dentro del rango de fechas
        List<TransaccionEntity> entities = transaccionJpaRepository
                .findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween(
                    cuentaIdString, cuentaIdString, desde, hasta);
        
        return entities.stream()
                .map(transaccionMapper::aDominio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaccion> buscarCuentas(CuentaId cuentaId){

        System.out.println("CuentaId recibido: " + cuentaId);
        System.out.println("Valor del CuentaId: " + cuentaId.getValor());
        // Convertimos el Value Object a string para buscar en BD
        String numeroCuenta = cuentaId.getValor();

        // Buscamos en la BD usando Spring Data JPA
        List<TransaccionEntity> entityOpt = transaccionJpaRepository.findByNumeroCuentas(numeroCuenta);

        System.out.println("¿Encontrado en BD? " + entityOpt.isEmpty());
        // retornamos la cuenta convertida a DOMINIO

        return entityOpt.stream()
        .map(transaccionMapper::aDominio)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

        
    }

    @Override
    public List<Transaccion> buscarPorReferencia(String referencia) {
        System.out.println(" Buscando transacciones por referencia: " + referencia);
        
        List<TransaccionEntity> entities = transaccionJpaRepository
                .findByReferenciaContainingIgnoreCase(referencia);
        
        return entities.stream()
                .map(transaccionMapper::aDominio)
                .collect(Collectors.toList());
    }
}

