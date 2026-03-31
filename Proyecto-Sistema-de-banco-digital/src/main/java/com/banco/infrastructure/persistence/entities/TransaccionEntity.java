package com.banco.infrastructure.persistence.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;




@Entity
@Table(name = "Transacciones")
public class TransaccionEntity {

    //Atributos

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaccion_id", unique = true, nullable = false, length = 60)
    private String transaccionId;

    // ENUM TIPO TRANSACCIÓN - Guardado como texto en BD
    @Column(name = "tipo_transaccion", nullable = false, length = 20)
    private String tipoTransaccion;

    @Column(name = "cuenta_origen_id", length = 50)
    private String cuentaOrigenId;

    @Column(name = "cuenta_destino_id", length = 50)
    private String cuentaDestinoId;

    // Precision: 15 dígitos total, 2 decimales (ej: 9999999999999.99)
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "fecha_de_creacion", nullable = false)
    private LocalDateTime fechaDeCreacion;

    // ENUM ESTADO - Guardado como texto, puede cambiar
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTransaccion estado;

    @Column(name = "referencia", length = 100)
    private String referencia;

    // CONTRUCTOR VACIO 
    public TransaccionEntity(){}
    // CONTRUCTOR PARA PRUEBAS
    public TransaccionEntity(UUID id, String transaccionId, String tipoTransaccion, String cuentaOrigenId,
            String cuentaDestinoId, BigDecimal monto, String moneda, String descripcion, LocalDateTime fechaDeCreacion,
            EstadoTransaccion estado, String referencia) {
        this.id = id;
        this.transaccionId = transaccionId;
        this.tipoTransaccion = tipoTransaccion;
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.monto = monto;
        this.moneda = moneda;
        this.descripcion = descripcion;
        this.fechaDeCreacion = fechaDeCreacion;
        this.estado = estado;
        this.referencia = referencia;
    }


    // GETTERS Y SETTERS

    public UUID getId() { return id;}
    public void setId(UUID id) {this.id = id; }


    public String getTransaccionId() { return transaccionId;  }
    public void setTransaccionId(String transaccionId) {this.transaccionId = transaccionId;  }


    public String getTipoTransaccion() {return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }


    public String getCuentaOrigenId() {return cuentaOrigenId;   }
    public void setCuentaOrigenId(String cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }


    public String getCuentaDestinoId() { return cuentaDestinoId;  }
    public void setCuentaDestinoId(String cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }

    
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto;}


    public String getMoneda() {  return moneda;   }
    public void setMoneda(String moneda) { this.moneda = moneda; }


    public String getDescripcion() {return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }


    public LocalDateTime getFechaDeCreacion() {return fechaDeCreacion; }
    public void setFechaDeCreacion(LocalDateTime fechaDeCreacion) {this.fechaDeCreacion = fechaDeCreacion;}


    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }


    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia;}

 

    
}
