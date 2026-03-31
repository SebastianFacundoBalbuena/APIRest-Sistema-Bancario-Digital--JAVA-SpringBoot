package com.banco.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultaSaldoResponse {

    // ATRIBUTOS

    // INFORMACIÓN BÁSICA
    private String cuentaId;
    private String clienteId;
    private String tipoCuenta;
    private String moneda;
    private LocalDateTime fechaConsulta;

    // SALDOS
    private BigDecimal saldoActual;
    private BigDecimal saldoDisponible;
    private BigDecimal limiteSobregiro;

    // RESUMEN PERIODO
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;

    //  MOVIMIENTOS
    private List<MovimientoDTO> movimientos;
    private boolean tieneMasMovimientos;

    // ESTADO Y RESTRICCIONES
    private String estadoCuenta;
    private List<String> restricciones;

    // MENSAJE INFORMATIVO
    private String mensaje;



    //  Constructor básico
    public ConsultaSaldoResponse(String cuentaId, String clienteId, String tipoCuenta,
            String moneda, BigDecimal saldoActual, String mensaje) {
        this.cuentaId = cuentaId;
        this.clienteId = clienteId;
        this.tipoCuenta = tipoCuenta;
        this.moneda = moneda;
        this.saldoActual = saldoActual;
        this.saldoDisponible = saldoActual;
        this.fechaConsulta = LocalDateTime.now();
        this.movimientos = new ArrayList<>();
        this.restricciones = new ArrayList<>();
        this.mensaje = mensaje;
    }



    // METODOS
    public void agregarMovimiento(MovimientoDTO movimiento) {
        if (this.movimientos == null) {
            this.movimientos = new ArrayList<>();
        }
        this.movimientos.add(movimiento);
    }

    public void agregarRestriccion(String restriccion) {
        if (this.restricciones == null) {
            this.restricciones = new ArrayList<>();
        }
        this.restricciones.add(restriccion);
    }



    // Getters y Setters
    public String getCuentaId() { return cuentaId; }
    public void setCuentaId(String cuentaId) { this.cuentaId = cuentaId; }
    
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    
    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    
    public LocalDateTime getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDateTime fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    
    public BigDecimal getSaldoActual() { return saldoActual; }
    public void setSaldoActual(BigDecimal saldoActual) { this.saldoActual = saldoActual; }
    
    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; }
    
    public BigDecimal getLimiteSobregiro() { return limiteSobregiro; }
    public void setLimiteSobregiro(BigDecimal limiteSobregiro) { this.limiteSobregiro = limiteSobregiro; }
    
    public BigDecimal getTotalIngresos() { return totalIngresos; }
    public void setTotalIngresos(BigDecimal totalIngresos) { this.totalIngresos = totalIngresos; }
    
    public BigDecimal getTotalEgresos() { return totalEgresos; }
    public void setTotalEgresos(BigDecimal totalEgresos) { this.totalEgresos = totalEgresos; }
    
    public List<MovimientoDTO> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoDTO> movimientos) { this.movimientos = movimientos; }
    
    public boolean isTieneMasMovimientos() { return tieneMasMovimientos; }
    public void setTieneMasMovimientos(boolean tieneMasMovimientos) { this.tieneMasMovimientos = tieneMasMovimientos; }
    
    public String getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(String estadoCuenta) { this.estadoCuenta = estadoCuenta; }
    
    public List<String> getRestricciones() { return restricciones; }
    public void setRestricciones(List<String> restricciones) { this.restricciones = restricciones; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
