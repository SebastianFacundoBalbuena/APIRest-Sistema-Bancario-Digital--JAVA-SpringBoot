package com.banco.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CuentaRequest {
    
    //ATRIBUTOS

    @NotBlank(message = "El id del cliente es obligatorio")
    private String clienteId;

    @NotBlank(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El saldo debe ser positivo")
    private BigDecimal saldoInicial;

    // Pattern: anotación de validación que verifica si un String coincide con una expresión regular (regex).
    @NotBlank(message = "La moneda es obligatoria")
    @Pattern(regexp = "^(EUR|USD|ARG)$", message = "Moneda no valida. Use : EUR, USD o ARG")
    private String moneda;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Pattern(regexp = "^(CORRIENTE|AHORROS)$", message = "Tipo de cuenta no válido. Use: CORRIENTE o AHORROS")
    private String tipoCuenta;

    private String descripcion;



    public CuentaRequest() {}



    public CuentaRequest( String clienteId,BigDecimal saldoInicial, String moneda,  String tipoCuenta, String descripcion) {

        this.clienteId = clienteId;
        this.saldoInicial = saldoInicial;
        this.moneda = moneda;
        this.tipoCuenta = tipoCuenta;
        this.descripcion = descripcion;
    }



    public String getClienteId() { return clienteId;}
    public void setClienteId(String clienteId) {this.clienteId = clienteId; }

    public BigDecimal getSaldoInicial() {return saldoInicial; }
    public void setSaldoInicial(BigDecimal saldoInicial) {this.saldoInicial = saldoInicial;}

    public String getMoneda() {return moneda; }
    public void setMoneda(String moneda) {this.moneda = moneda; }

    public String getTipoCuenta() { return tipoCuenta;}
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) { this.descripcion = descripcion;}


    

    


    
    
}
