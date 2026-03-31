package com.banco.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ClienteResponse {
    
    //ATRIBUTOS
    private String clienteId;
    private String nombre;
    private String email;
    private boolean activo;
    private int cantidadCuentas;
    private int maxCuentasPermitidas;
    private List<String> cuentaIds; // Solo IDs de cuentas
    private LocalDateTime fechaCreacion; // Si tienes en el dominio
    private LocalDateTime fechaUltimaActualizacion; // Si tienes


    // CONSTRUCTOR
    public ClienteResponse(String clienteId, String nombre, String email, boolean activo, int cantidadCuentas,
            int maxCuentasPermitidas, List<String> cuentaIds, LocalDateTime fechaCreacion,
            LocalDateTime fechaUltimaActualizacion) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.email = email;
        this.activo = activo;
        this.cantidadCuentas = cantidadCuentas;
        this.maxCuentasPermitidas = maxCuentasPermitidas;
        this.cuentaIds = cuentaIds;
        this.fechaCreacion = fechaCreacion;
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }


    public String getClienteId() {   return clienteId; }

    public String getNombre() { return nombre;  }

    public String getEmail() { return email;  }

    public boolean isActivo() {   return activo; }

    public int getCantidadCuentas() {return cantidadCuentas;  }

    public int getMaxCuentasPermitidas() { return maxCuentasPermitidas; }

    public List<String> getCuentaIds() { return cuentaIds; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion;}

    public LocalDateTime getFechaUltimaActualizacion() {   return fechaUltimaActualizacion; }


 
    
}
