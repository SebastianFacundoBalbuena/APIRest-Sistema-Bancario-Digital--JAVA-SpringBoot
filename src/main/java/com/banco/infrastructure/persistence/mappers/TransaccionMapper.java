package com.banco.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;
import com.banco.infrastructure.persistence.entities.TransaccionEntity;




@Component
public class TransaccionMapper {

    // DE ENTITY A DOMINIO
    
    public Transaccion aDominio(TransaccionEntity entity){

        // OBTENEMOS LOS DATOS JUNTO A SU RESPECTIVO DOMINIO
        TransaccionId transaccionId = new TransaccionId(entity.getTransaccionId());
        TipoTransaccion tipoTransaccion = TipoTransaccion.valueOf(entity.getTipoTransaccion());
        Moneda moneda = Moneda.fromCodigo(entity.getMoneda());
        Dinero monto = Dinero.nuevo(entity.getMonto(), moneda);
        

        CuentaId cuentaOrigen = null;
        if(entity.getCuentaOrigenId() != null){
            cuentaOrigen = CuentaId.newCuentaId(entity.getCuentaOrigenId());
        }

        CuentaId cuentaDestino = null;
        if(entity.getCuentaDestinoId() != null){
            cuentaDestino = CuentaId.newCuentaId(entity.getCuentaDestinoId());
        }

        //CREAMOS LA TRANSACCION 
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            tipoTransaccion, 
            cuentaOrigen, 
            cuentaDestino,
            monto,
            entity.getDescripcion());

        // MODIFICAMOS EL ESTADO A PENDIENTE
 
        return transaccion;
    }


    // DE DOMINIO A ENTITY

    public TransaccionEntity aEntity(Transaccion dominio){

        TransaccionEntity transaccionEntity = new TransaccionEntity();

        // Obtenemos los datos 
        transaccionEntity.setTransaccionId(dominio.getId().toString());
        transaccionEntity.setTipoTransaccion(dominio.getTipo().name());
        transaccionEntity.setMoneda(dominio.getMonto().getMoneda().name());
        transaccionEntity.setMonto(dominio.getMonto().getMonto());
        transaccionEntity.setDescripcion(dominio.getDescripcion());
        transaccionEntity.setFechaDeCreacion(dominio.getFechaCreacion());
        transaccionEntity.setReferencia(dominio.getReferencia());
        transaccionEntity.setEstado(dominio.getEstado());
        
        if(transaccionEntity.getCuentaOrigenId() != null){
            transaccionEntity.setCuentaOrigenId(dominio.getCuentaOrigen().getValor());
        }
        if(transaccionEntity.getCuentaDestinoId() != null){
            transaccionEntity.setCuentaDestinoId(dominio.getCuentaDestino().getValor());
        }
        
        return transaccionEntity;
        
    }
}
