package com.banco.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaResponse {

    // ATRIBUTOS
    private String transaccionId;
    private String estado;
    private BigDecimal monto;
    private String moneda;
    private LocalDateTime fecha;
    private String cuentaOrigenId;
    private String cuentaDestinoId;
    private String mensaje;

    //CONSTRUCTOR
    public TransferenciaResponse(String transaccionId, String estado, BigDecimal monto, String moneda,
            LocalDateTime fecha, String cuentaOrigenId, String cuentaDestinoId, String mensaje) {
        this.transaccionId = transaccionId;
        this.estado = estado;
        this.monto = monto;
        this.moneda = moneda;
        this.fecha = fecha;
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.mensaje = mensaje;
    }



    public String getTransaccionId() {return transaccionId; }
    public void setTransaccionId(String transaccionId) { this.transaccionId = transaccionId;}

    public String getEstado() {  return estado; }
    public void setEstado(String estado) {this.estado = estado; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMoneda() {return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public LocalDateTime getFecha() {return fecha;}
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getCuentaOrigenId() { return cuentaOrigenId;}
    public void setCuentaOrigenId(String cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId;}

    public String getCuentaDestinoId() {return cuentaDestinoId; }
    public void setCuentaDestinoId(String cuentaDestinoId) {this.cuentaDestinoId = cuentaDestinoId;}

    public String getMensaje() { return mensaje;}
    public void setMensaje(String mensaje) {this.mensaje = mensaje; }


    
    

}
