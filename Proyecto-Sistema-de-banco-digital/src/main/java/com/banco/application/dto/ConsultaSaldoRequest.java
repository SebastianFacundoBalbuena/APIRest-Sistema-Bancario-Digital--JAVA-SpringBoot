package com.banco.application.dto;

import java.time.LocalDate;

public class ConsultaSaldoRequest {

    // ATRIBUTOS
    private String cuentaId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private boolean incluirMovimientos;
    private int limiteMovimientos;

    public ConsultaSaldoRequest(){

        this.incluirMovimientos = true;
        this.limiteMovimientos = 10;
    }

    public ConsultaSaldoRequest(String cuentaId, LocalDate fechaDesde, LocalDate fechaHasta, boolean incluirMovimientos,
            int limiteMovimientos) {
        this.cuentaId = cuentaId;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.incluirMovimientos = incluirMovimientos;
        this.limiteMovimientos = limiteMovimientos;
    }


    // GETTERS Y SETTERS

    public String getCuentaId() { return cuentaId; }
    public void setCuentaId(String cuentaId) { this.cuentaId = cuentaId; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) {this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) {this.fechaHasta = fechaHasta;}

    public boolean isIncluirMovimientos() { return incluirMovimientos;}
    public void setIncluirMovimientos(boolean incluirMovimientos) { this.incluirMovimientos = incluirMovimientos; }

    public int getLimiteMovimientos() { return limiteMovimientos; }
    public void setLimiteMovimientos(int limiteMovimientos) {this.limiteMovimientos = limiteMovimientos ;  } 


    
}
