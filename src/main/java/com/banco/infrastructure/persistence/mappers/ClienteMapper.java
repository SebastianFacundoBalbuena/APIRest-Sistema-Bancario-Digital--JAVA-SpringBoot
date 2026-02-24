package com.banco.infrastructure.persistence.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.infrastructure.persistence.entities.ClienteEntity;



@Component
public class ClienteMapper {
    
    //ClienteEntoty a DOMINIO
    public Cliente aDominio(ClienteEntity entity){
        
        ClienteId clienteId = ClienteId.newCliente(entity.getClienteId());

        //Cada string se convierte en un Value Object CuentaId
        List<CuentaId> cuentaIds = entity.getCuentasIds().stream().map(
            entitys -> CuentaId.newCuentaId(entitys)).collect(Collectors.toList());

        // CREAMOS EL CLIENTE
        Cliente cliente = new Cliente(clienteId, entity.getNombre(), entity.getEmail());

        //  Si en BD está inactivo, llamamos al método desactivar()
        if (!entity.isActiva()) {
            cliente.desactivar();
        }

        //AGREGAR CUENTAS A CLIENTE
        // Por cada cuentaId (iteracion) de la lista "cuentasIds" hace esto...
        for (CuentaId cuentaId : cuentaIds) {
            
            try {
                cliente.agregarCuenta(cuentaId);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Ocurrio un error al intentar mapear las cuentasIdS: " + e.getMessage());
            }
        }

        System.out.println("Cliente reconstruido desde BD: " + clienteId);
        return cliente;


    }


    // DE DOMINIO A ENTITY
    public ClienteEntity aEntity(Cliente dominio,ClienteEntity entityExistente){

        if(entityExistente == null){
            entityExistente = new ClienteEntity();
        }
        

        entityExistente.setClienteId(dominio.getClienteId().getValor());

        // Cada iteracion retorna su valor a string y luego lo almacenamos
        List<String> cuentasIdsString = dominio.getCuentas()
        .stream().map(dom -> dom.getValor()).collect(Collectors.toList());

        entityExistente.setCuentasIds(cuentasIdsString);
        entityExistente.setActiva(dominio.getActiva());
        entityExistente.setEmail(dominio.getEmail());
        entityExistente.setNombre(dominio.getNombre());
        entityExistente.setMaxCuentasPermitidas(dominio.getMaxCuentas());

        return entityExistente;
    }
}
