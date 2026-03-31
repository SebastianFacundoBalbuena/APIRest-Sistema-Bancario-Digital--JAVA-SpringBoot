package com.banco.infrastructure.persistence.mappers;


import org.springframework.stereotype.Component;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.infrastructure.persistence.entities.CuentaEntity;




@Component
public class CuentaMapper {

    // Convierte CuentaEntity (BD) a Cuenta (dominio)

    public Cuenta aDominio(CuentaEntity entity){

        // // Reconstruimos los Value Objects del dominio
        CuentaId cuentaId =  CuentaId.newCuentaId(entity.getNumeroCuenta());
        ClienteId clienteId = ClienteId.newCliente(entity.getClienteId());
        Moneda moneda =  Moneda.fromCodigo(entity.getMoneda());
        Dinero saldo = Dinero.nuevo(entity.getSaldo(), moneda);
        boolean activa = entity.getActiva();
        
        
        // Creamos la entidad de dominio con todos sus valores
        

        return new Cuenta(cuentaId, clienteId, moneda, saldo, activa);

    }

    public CuentaEntity aEntity(Cuenta cuenta, CuentaEntity cuentaExistente){

        if(cuentaExistente == null){
            cuentaExistente = new CuentaEntity();
        }


        
        cuentaExistente.setNumeroCuenta(cuenta.getCuentaId().getValor());
        cuentaExistente.setClienteId(cuenta.getClienteId().getValor());
        cuentaExistente.setMoneda(cuenta.getMoneda().name());
        cuentaExistente.setSaldo(cuenta.getSaldo().getMonto());
        cuentaExistente.setActiva(cuenta.getActiva());
        return cuentaExistente;
    }
}
