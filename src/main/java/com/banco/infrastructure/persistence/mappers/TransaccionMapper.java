package com.banco.infrastructure.persistence.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
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
        EstadoTransaccion estado = EstadoTransaccion.valueOf(entity.getEstado().name());
        LocalDateTime fechaDeCreacion = entity.getFechaDeCreacion();
        String referencia = entity.getReferencia();
        

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
            fechaDeCreacion,
            estado,
            referencia,
            monto,
            entity.getDescripcion());

        // MODIFICAMOS EL ESTADO A PENDIENTE
 
        return transaccion;
    }


    // DE DOMINIO A ENTITY

    public TransaccionEntity aEntity(Transaccion dominio, TransaccionEntity transaccionExistente){

        if(transaccionExistente == null){

            transaccionExistente = new TransaccionEntity();
        }

        // Obtenemos los datos 
        transaccionExistente.setTransaccionId(dominio.getId().toString());
        transaccionExistente.setTipoTransaccion(dominio.getTipo().name());
        transaccionExistente.setMoneda(dominio.getMonto().getMoneda().name());
        transaccionExistente.setMonto(dominio.getMonto().getMonto());
        transaccionExistente.setDescripcion(dominio.getDescripcion());
        transaccionExistente.setFechaDeCreacion(dominio.getFechaCreacion());
        transaccionExistente.setReferencia(dominio.getReferencia());
        transaccionExistente.setEstado(dominio.getEstado());
        
        if(transaccionExistente.getCuentaOrigenId() == null){
            transaccionExistente.setCuentaOrigenId(dominio.getCuentaOrigen().getValor());
        }
        if(transaccionExistente.getCuentaDestinoId() == null){
            transaccionExistente.setCuentaDestinoId(dominio.getCuentaDestino().getValor());
        }
        
        return transaccionExistente;
        
    }
}
