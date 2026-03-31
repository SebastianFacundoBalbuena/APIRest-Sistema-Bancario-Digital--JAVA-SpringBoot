package com.banco.application.dto;

import java.math.BigDecimal;

public class AperturaCuentaRequest {

    // ATRIBUTOS
    private String clienteId;
    private String tipoCuenta;
    private String moneda;
    private BigDecimal saldoInicial;
    private String sucursal;



    // CONSTRUCTOR
      //Vacio - necesario  
    public AperturaCuentaRequest() {}


    public AperturaCuentaRequest(String clienteId, String tipoCuenta, String moneda, BigDecimal saldoInicial,
                String sucursal) {
            this.clienteId = clienteId;
            this.tipoCuenta = tipoCuenta;
            this.moneda = moneda;
            this.saldoInicial = saldoInicial;
            this.sucursal = sucursal;
        }



    // GETTERS Y SETTERS
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) {this.clienteId = clienteId;}


    public String getTipoCuenta() {  return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta;}


    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) {this.moneda = moneda; }


    public BigDecimal getSaldoInicial() {return saldoInicial;}
    public void setSaldoInicial(BigDecimal saldoInicial) {this.saldoInicial = saldoInicial;}


    public String getSucursal() {return sucursal; }
    public void setSucursal(String sucursal) { this.sucursal = sucursal; }


       

        
        
}
