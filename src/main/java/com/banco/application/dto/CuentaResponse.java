package com.banco.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;




public class CuentaResponse {
    
    //ATRIBUTOS

    private String cuentaId;

    private String clienteId;

    private String numeroCuenta;

    private BigDecimal saldo;

    private String moneda;

    private String tipoCuenta;

    private boolean activa;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaUltActualizacion;

    private String mensaje;

    public CuentaResponse(String cuentaId, String clienteId, String numeroCuenta, BigDecimal saldo, String moneda,
            String tipoCuenta, boolean activa, LocalDateTime fechaCreacion, LocalDateTime fechaUltActualizacion,
            String mensaje) {
        this.cuentaId = cuentaId;
        this.clienteId = clienteId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.moneda = moneda;
        this.tipoCuenta = tipoCuenta;
        this.activa = activa;
        this.fechaCreacion = fechaCreacion;
        this.fechaUltActualizacion = fechaUltActualizacion;
        this.mensaje = mensaje;
    }

    
    // GETTERS
    public String getCuentaId() { return cuentaId; }

    public String getClienteId() { return clienteId; }

    public String getNumeroCuenta() { return numeroCuenta; }

    public BigDecimal getSaldo() {  return saldo; }

    public String getMoneda() { return moneda; }

    public String getTipoCuenta() { return tipoCuenta; }

    public boolean isActiva() { return activa; }

    public LocalDateTime getFechaCreacion() {return fechaCreacion;}

    public LocalDateTime getFechaUltActualizacion() { return fechaUltActualizacion; }

    public String getMensaje() {return mensaje; }


    // SETTERS
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion; }

    public void setMensaje(String mensaje) {this.mensaje = mensaje;}


    
    
    
}
