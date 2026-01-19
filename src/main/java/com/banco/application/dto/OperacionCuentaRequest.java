package com.banco.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;



public class OperacionCuentaRequest {
    

    // ATRIBUTOS

    @NotBlank(message = "El id de la cuenta es obligatorio")
    private String cuentaId;

    @NotBlank(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "La moneda es obligatoria")
    private String moneda;

    private String descripcion;

    private String referencia;

    public OperacionCuentaRequest( String cuentaId, BigDecimal monto,
            String descripcion, String referencia) {
        this.cuentaId = cuentaId;
        this.monto = monto;
        this.descripcion = descripcion;
        this.referencia = referencia;
    }

    public String getCuentaId() {
        return cuentaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getReferencia() {
        return referencia;
    }


    
    
}
