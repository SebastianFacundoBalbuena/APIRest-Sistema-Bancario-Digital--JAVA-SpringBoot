package com.banco.application.dto;

import java.math.BigDecimal;

// DTO: SOLICITUD DE TRANSFERENCIA
public class TransferenciaRequest {

    //ATRIBUTOS
    private String cuentaOrigen;
    private String cuentaDestino;
    private BigDecimal monto;
    private String moneda;
    private String descripcion;

    // Contructor
    public TransferenciaRequest(String cuentaOrigen, String cuentaDestino, BigDecimal monto, String moneda,
            String descripcion) {
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.moneda = moneda;
        this.descripcion = descripcion;
    }

    public String getCuentaOrigen() { return cuentaOrigen;}
    public void setCuentaOrigen(String cuentaOrigen) { this.cuentaOrigen = cuentaOrigen;}

    public String getCuentaDestino() {return cuentaDestino;}
    public void setCuentaDestino(String cuentaDestino) {this.cuentaDestino = cuentaDestino;}

    public BigDecimal getMonto() {return monto;}
    public void setMonto(BigDecimal monto) { this.monto = monto;}

    public String getMoneda() { return moneda;}
    public void setMoneda(String moneda) {this.moneda = moneda; }

    public String getDescripcion() {return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    


}
