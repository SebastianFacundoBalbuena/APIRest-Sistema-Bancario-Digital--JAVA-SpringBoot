package com.banco.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.banco.application.port.out.CuentaRepository;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.infrastructure.persistence.entities.CuentaEntity;
import com.banco.infrastructure.persistence.mappers.CuentaMapper;

import jakarta.transaction.Transactional;
import java.util.List;







@Repository
@Transactional
public class CuentaRepositoryJpa implements CuentaRepository {
    
    // INTERNFAZ para adquirir metodo CRUD JPA
     interface CuentaJpaRepository extends JpaRepository<CuentaEntity, UUID> {
    
        // Esta interfaz hereda de JPA, por lo cual podemos usar sus palabras clave
        // COMO existsBy - findBy etc + nombre del atributo
        // JPA ya conoce estas palabras y las detecta automaticamente sabiendo que queremos
        Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta);


        boolean existsByNumeroCuenta(String numeroCuenta);

        List<CuentaEntity> findByClienteId(String clienteId);
    }

    
    // INYECCION DE DEPENDENCIA
    private final CuentaJpaRepository cuentaJpaRepository;
    private final CuentaMapper cuentaMapper;

    public CuentaRepositoryJpa(CuentaJpaRepository cuentaJpaRepository, CuentaMapper cuentaMapper) {
        this.cuentaJpaRepository = cuentaJpaRepository;
        this.cuentaMapper = cuentaMapper;
    }


    // METODOS A IMPLEMENTAR

    @Override
    public Optional<Cuenta> buscarPorId(CuentaId cuentaId){

        System.out.println("CuentaId recibido: " + cuentaId);
        System.out.println("Valor del CuentaId: " + cuentaId.getValor());
        // Convertimos el Value Object a string para buscar en BD
        String numeroCuenta = cuentaId.getValor();

        // Buscamos en la BD usando Spring Data JPA
        Optional<CuentaEntity> entityOpt = cuentaJpaRepository.findByNumeroCuenta(numeroCuenta);

        System.out.println("¿Encontrado en BD? " + entityOpt.isPresent());
        // retornamos la cuenta convertida a DOMINIO

        Optional<Cuenta> cuentaa = entityOpt.map(entity -> cuentaMapper.aDominio(entity));

        return cuentaa;
    }


    @Override
    public void guardar(Cuenta cuenta){
        
        // buscar o Convertimos a Entity

        CuentaEntity entityExistente = cuentaJpaRepository.findByNumeroCuenta(cuenta.getCuentaId().getValor()).orElse(null);
        CuentaEntity entity = cuentaMapper.aEntity(cuenta, entityExistente);
        

        if(entity != null){ 
            cuentaJpaRepository.save(entity);
            System.out.println(" Cuenta guardada en BD: " + cuenta.getCuentaId());
        }

        
    }

    

    @Override
    public void actualizar(Cuenta cuenta){
        guardar(cuenta);
    }

    @Override
    public List<Cuenta> buscarPorCliente(ClienteId clienteId){

        List<CuentaEntity> cuentasEntity = cuentaJpaRepository.findByClienteId(clienteId.getValor());

        return cuentasEntity.stream().map(entity -> cuentaMapper.aDominio(entity))
        .collect(Collectors.toList());
    }

    @Override
    public boolean existeCuentaConNumero(String numeroCuenta) {
        return cuentaJpaRepository.existsByNumeroCuenta(numeroCuenta);
    }


}
