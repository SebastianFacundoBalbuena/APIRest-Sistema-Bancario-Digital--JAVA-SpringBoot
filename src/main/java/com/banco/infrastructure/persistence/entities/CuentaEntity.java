package com.banco.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entidad JPA para la tabla CUENTAS

@Entity
@Table(name = "cuentas")
public class CuentaEntity {

    // ATRIBUTOS CON ANOTACIONES JPA

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_cuenta", unique = true, nullable = false, length = 30)
    private String numeroCuenta;

    @Column(name = "cliente_id", nullable = false, length = 50)
    private String clienteId;


    @Column(name = "saldo", nullable = false, scale = 2, precision = 15)
    private BigDecimal saldo;


    @Column(name = "moneda", length = 3, nullable = false)
    private String moneda;

    @Column(name = "activa", nullable = false)
    private boolean activa;


    // Constructor vacio - Necesario
    public CuentaEntity(){}



    // GETTERS Y SETTERS

    public UUID getId() {return id; }
    public void setId(UUID id) {this.id = id; }


    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }


    public String getClienteId() { return clienteId;}
    public void setClienteId(String clienteId) { this.clienteId = clienteId;  }



    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) {  this.saldo = saldo; }



    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }


    public boolean getActiva() { return activa;  }
    public void setActiva(boolean activa) { this.activa = activa;  }

    

}
