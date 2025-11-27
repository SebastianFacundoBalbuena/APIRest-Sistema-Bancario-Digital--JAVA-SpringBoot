package com.banco.domain.model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

import com.banco.domain.model.valueobjects.*;
import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;


public class Transaccion {

    //🔐 ATRIBUTOS INMUTABLES
    private final TransaccionId transaccionId;
    private final TipoTransaccion tipoTransaccion;
    private final CuentaId cuentaOrigen;
    private final CuentaId cuentaDestino;
    private final Dinero monto;
    private final String descripcion;
    private final LocalDateTime fechaDeCreacion;
    private EstadoTransaccion estado;
    private final String referencia;



    // CONSTRUCTOR PRINCIPAL

    public Transaccion(TransaccionId transaccionId, TipoTransaccion tipoTransaccion, CuentaId cuentaOrigen,
        CuentaId cuentaDestino, Dinero monto, String descripcion){

            // VALIDACIONES DE INTEGRIDAD
            this.transaccionId = Objects.requireNonNull(transaccionId,"No se permite valor nulo");
            this.tipoTransaccion = Objects.requireNonNull(tipoTransaccion,"No se permite valor nulo");
            this.monto = Objects.requireNonNull(monto,"No se permite nulo");
            this.descripcion = Objects.requireNonNull(descripcion,"No se permite nulo");

            this.cuentaOrigen = cuentaOrigen;
            this.cuentaDestino = cuentaDestino;

            this.fechaDeCreacion = LocalDateTime.now();
            this.estado = EstadoTransaccion.PENDIENTE;
            this.referencia = generarReferencia();

            validarConsistencia();
            
}




    // GETTERS
    public TransaccionId getId() { return transaccionId; }
    public TipoTransaccion getTipo() { return tipoTransaccion; }
    public CuentaId getCuentaOrigen() { return cuentaOrigen; }
    public CuentaId getCuentaDestino() { return cuentaDestino; }
    public Dinero getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaDeCreacion; }
    public EstadoTransaccion getEstado() { return estado; }
    public String getReferencia() { return referencia; }

    //OBTENER RESUMEN PARA REPORTES
    public String getResumen() {
        return String.format("%s - %s - %s - %s", 
            tipoTransaccion, monto, estado, fechaDeCreacion.toLocalDate());
    }

    //ES TRANSACCIÓN DE ENTRADA?
    public boolean esEntradaPara(CuentaId cuentaId) {
        return cuentaId.equals(cuentaDestino) && 
               (tipoTransaccion == TipoTransaccion.DEPOSITO || tipoTransaccion == TipoTransaccion.TRANSFERENCIA);
    }

    // DE SALIDA
    public boolean esSalidaPara(CuentaId cuentaId) {
        return cuentaId.equals(cuentaOrigen) && 
               (tipoTransaccion == TipoTransaccion.RETIRO || tipoTransaccion == TipoTransaccion.TRANSFERENCIA);
    }

    //DETALLE PARA EXTRACTO
        public String generarDetalleExtracto() {
        return String.format(
            "%-12s %-15s %10s %-8s %s",
            fechaDeCreacion.toLocalDate(),
            tipoTransaccion,
            monto,
            estado,
            descripcion
        );
    }






    // METODOS CICLO DE VIDA
    public void completar(){
        if(this.estado != EstadoTransaccion.PENDIENTE) throw new IllegalArgumentException(
            "Solo transacciones pendientes pueden completarse. Estado : " +  this.estado);
        
            this.estado = EstadoTransaccion.COMPLETADA;

    }

    public void rechazar(String motivo){
        if(this.estado != EstadoTransaccion.PENDIENTE) throw new IllegalArgumentException(
            "Solo transacciones pendientes pueden rechazarse. Estado : " +  this.estado);
        
            this.estado = EstadoTransaccion.RECHAZADA;
            System.out.println("Transacción " + transaccionId + " RECHAZADA: " + motivo);
            
    }

    public void revertir() {
        if (this.estado != EstadoTransaccion.COMPLETADA) {
            throw new IllegalStateException(
                "Solo transacciones COMPLETADAS pueden revertirse. Estado actual: " + estado
            );
        }
        
        this.estado = EstadoTransaccion.REVERTIDA;
        System.out.println("Transacción " + transaccionId + " REVERTIDA");
    }

    private boolean esDemasiadoAntigua() {
        return fechaDeCreacion.isBefore(LocalDateTime.now().minusDays(30));
    }

    public boolean esReversible() {
        return this.estado == EstadoTransaccion.COMPLETADA &&
               this.tipoTransaccion != TipoTransaccion.COMISION && // Ejemplo de regla
               !esDemasiadoAntigua(); // Otra regla de negocio
    }






    // 🛡️ VALIDAR CONSISTENCIA DE LA TRANSACCIÓN
        private void validarConsistencia() {
        // 💰 MONTO DEBE SER POSITIVO
        if (monto.esMenorOIgualQue(Dinero.nuevoCero(monto.getMoneda()))) {
            throw new IllegalArgumentException(" El monto debe ser positivo: " + monto);
        }

        // 🎯 VALIDACIONES ESPECÍFICAS POR TIPO
        switch (tipoTransaccion) {
            case TRANSFERENCIA:
                validarTransferencia();
                break;
            case DEPOSITO:
                validarDeposito();
                break;
            case RETIRO:
                validarRetiro();
                break;
            case REVERSO:
                validarReverso();
                break;
            default :

                break;
        }

    }

        private void validarTransferencia() {
        if (cuentaOrigen == null) {
            throw new IllegalArgumentException(" Transferencia debe tener cuenta de origen");
        }
        if (cuentaDestino == null) {
            throw new IllegalArgumentException(" Transferencia debe tener cuenta de destino");
        }
        if (cuentaOrigen.equals(cuentaDestino)) {
            throw new IllegalArgumentException(" No se puede transferir a la misma cuenta");
        }
    }

        private void validarDeposito() {
        if (cuentaOrigen != null) {
            throw new IllegalArgumentException(" Depósito no debe tener cuenta de origen");
        }
        if (cuentaDestino == null) {
            throw new IllegalArgumentException(" Depósito debe tener cuenta de destino");
        }
    }

        private void validarRetiro() {
        if (cuentaOrigen == null) {
            throw new IllegalArgumentException(" Retiro debe tener cuenta de origen");
        }
        if (cuentaDestino != null) {
            throw new IllegalArgumentException(" Retiro no debe tener cuenta de destino");
        }
    }

        private void validarReverso() {
        // En una implementación completa, validaríamos que la referencia
        // apunte a una transacción existente y reversible
        if (referencia == null || referencia.trim().isEmpty()) {
            throw new IllegalArgumentException(" Reverso debe tener referencia a transacción original");
        }
    }

        private String generarReferencia() {
        return String.format("REF-%s-%d", 
            transaccionId.toString(), 
            System.currentTimeMillis() // 🕐 Para hacerla única
        );
    }
}


