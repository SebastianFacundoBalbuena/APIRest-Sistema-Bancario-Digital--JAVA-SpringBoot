package com.banco.domain.model.valueobjects;

import java.util.Objects;

// TRANSACCION_ID - Identificador único de transacción
//  Formato: TXN-2024-0000001 (TXN-AÑO-SECUENCIA)

public class TransaccionId {

    private final String valor;

    public TransaccionId(String valor){

        String valorValidado = Objects.requireNonNull(valor,"El valor no puede ser nulo");

        //Validamos formato
        if(!valor.matches("^TXN-\\d{4}-\\d{7}$")) throw new IllegalArgumentException(
            "Formato de Id de transaccion incorrecto: " +  valor);

            this.valor = valorValidado;
    }


    public String getValor() {
        return valor;
    }

    //  TOSTRING 
    @Override
    public String toString() {
        return valor; // Devuelve directamente el valor para facilidad de uso
    }




    //TIPO_TRANSACCION
    public enum TipoTransaccion{
        DEPOSITO,  //Ingreso de dinero
        RETIRO,    // Extracción de dinero 
        TRANSFERENCIA, //Envío entre cuentas
        PAGO_SERVICIO, //Pago de servicios
        COMISION, //Cobro de comisión
        INTERES, //Pago de intereses
        REVERSO //Reverso de una transacción anterior
    }

    //ESTADO_TRANSACCION
    public enum EstadoTransaccion{
        PENDIENTE,
        COMPLETADA,
        RECHAZADA,
        REVERTIDA
    }




}
