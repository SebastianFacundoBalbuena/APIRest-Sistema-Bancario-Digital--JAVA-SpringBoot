package com.banco.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AperturaCuentaResponse {

    // ATRIBUTOS
    private String cuentaId;
    private String clienteId;
    private String tipoCuenta;
    private String moneda;
    private BigDecimal saldoInicial;
    private LocalDateTime fechaApertura;
    private String mensaje;


    // CONSTRUCTOR
    public AperturaCuentaResponse(String cuentaId, String clienteId, String tipoCuenta, String moneda,
            BigDecimal saldoInicial, LocalDateTime fechaApertura, String mensaje) {
        this.cuentaId = cuentaId;
        this.clienteId = clienteId;
        this.tipoCuenta = tipoCuenta;
        this.moneda = moneda;
        this.saldoInicial = saldoInicial;
        this.fechaApertura = fechaApertura;
        this.mensaje = mensaje;
    }


    // GETTERS
    public String getCuentaId() { return cuentaId;}

    public String getClienteId() {return clienteId;}

    public String getTipoCuenta() {return tipoCuenta;}

    public String getMoneda() {return moneda;}

    public BigDecimal getSaldoInicial() { return saldoInicial; }

    public LocalDateTime getFechaApertura() { return fechaApertura;}

    public String getMensaje() { return mensaje;}



    

    

}
