package com.banco.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoDTO {

    // ATRIBUTOS

    private String id;
    private String tipo; // 📝 DEPOSITO, RETIRO, TRANSFERENCIA, etc.
    private LocalDateTime fecha; // ⏰ Fecha y hora exacta
    private BigDecimal monto; // 💰 Cantidad
    private String descripcion; // 📄 Descripción legible
    private String referencia; // 🔗 Referencia o número de transacción
    private String cuentaContraparte; // 👤 Cuenta origen/destino (si aplica)
    private BigDecimal saldoPosterior;// 💵 Saldo después de esta transacciónFFK


    // CONSTRUCTOR
    public MovimientoDTO(String id, String tipo, LocalDateTime fecha, BigDecimal monto, String descripcion,
            String referencia, String cuentaContraparte, BigDecimal saldoPosterior) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.monto = monto;
        this.descripcion = descripcion;
        this.referencia = referencia;
        this.cuentaContraparte = cuentaContraparte;
        this.saldoPosterior = saldoPosterior;
    }


    public String getId() {return id; }

    public String getTipo() { return tipo; }

    public LocalDateTime getFecha() { return fecha; }

    public BigDecimal getMonto() { return monto; }

    public String getDescripcion() { return descripcion; }

    public String getReferencia() { return referencia; }

    public String getCuentaContraparte() { return cuentaContraparte;}

    public BigDecimal getSaldoPosterior() { return saldoPosterior; }


    
}
