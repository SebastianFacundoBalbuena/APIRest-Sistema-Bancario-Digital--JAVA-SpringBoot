package com.banco.domain.model.entities;

import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cliente {

    // ATRIBUTOS
    private final ClienteId clienteId;
    private String nombre;
    private String email;
    private final List<CuentaId> cuentas;
    private boolean activa;
    private final int maxCuentasPermitidas;

    // CONSTRUCTOR PRINCIPAL
    public Cliente(ClienteId clienteId, String nombre, String email){
        
        // NO NULOS
        this.clienteId = Objects.requireNonNull(clienteId,"El id no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre,"El nombre no puede ser nulo");
        this.email = validarEmail(email);

        // INICIALIZACION
        this.cuentas = new ArrayList<>();
        this.activa = true;
        this.maxCuentasPermitidas = 5;

        System.out.println("✅ Cliente creado: " + nombre + " (" + email + ")");

    }

        public Cliente(ClienteId clienteId, String nombre, String email, boolean activa, List<CuentaId> cuentas){
        
        // NO NULOS
        this.clienteId = Objects.requireNonNull(clienteId,"El id no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre,"El nombre no puede ser nulo");
        this.email = validarEmail(email);
        this.activa = Objects.requireNonNull(activa, "Se requiere un valor");
        this.cuentas = cuentas;
        this.maxCuentasPermitidas = 5;

    }


    // GETTERS
    
    public ClienteId getClienteId(){return clienteId;}
    public String getNombre(){return nombre;}
    public String getEmail(){return email;}
    public boolean getActiva(){return activa;}
    public int getMaxCuentas(){return maxCuentasPermitidas;}
    public List<CuentaId> getCuentas(){return new ArrayList<>(cuentas);}

    public boolean verificarCuenta(CuentaId cuentaId){return cuentas.contains(cuentaId);}
    public void desactivar(){this.activa = false;}
    public void activar(){this.activa = true;}


    public void setNombre(String nombre){this.nombre = nombre;}
    public void setEmail(String email){this.email = email;}



    // METODOS CON COMPORTAMIENTO

    public void agregarCuenta(CuentaId cuentaId){
    // VALIDACIONES EN CADENA
    validarClienteActivo();
    validarLimiteCuenta();
    validarCuentaExistente(cuentaId);

    cuentas.add(cuentaId);

    System.out.println("Cuenta " + cuentaId + " agregada." + " Total: " + cuentas.size());

    }

    public void eliminarCuenta(CuentaId cuentaId){

        if(!cuentas.remove(cuentaId)) throw new IllegalArgumentException("La cuenta no se ecuentra asociada a este cliente");

        cuentas.remove(cuentaId);
    }

        public void validarTransferenciaEntrePropiasCuentas(CuentaId cuentaOrigenId, CuentaId cuentaDestinoId) {
        validarClienteActivo();
        
        if (!cuentas.contains(cuentaOrigenId)) throw new IllegalArgumentException(
                "❌ La cuenta origen " + cuentaOrigenId + " no pertenece al cliente"
            );
        
        
        if (!cuentas.contains(cuentaDestinoId)) throw new IllegalArgumentException(
                "❌ La cuenta destino " + cuentaDestinoId + " no pertenece al cliente"
            );

        
        if (cuentaOrigenId.equals(cuentaDestinoId)) throw new IllegalArgumentException(
            "❌ No se puede transferir a la misma cuenta");
        
        
        System.out.println("✅ Validación OK: transferencia entre cuentas propias permitida");
    }









    // VALIDACIONES
    private String validarEmail(String email){
        if(email == null || email.trim().isEmpty()) throw new IllegalArgumentException("El email no puede estar vacio");
        if(!email.contains("@")) throw new IllegalArgumentException("El email debe contener @");

        // devuelve el email sin espacios sobrantes trim()
        return email.trim();
    }

    private void validarClienteActivo(){

        if(!activa){ throw new IllegalArgumentException("El cliente esta inactivo y no puede operar");}       
    }

    private void validarLimiteCuenta(){
        
        // size() devuelve la cantidad de elementos del array
        if(cuentas.size() >= maxCuentasPermitidas) throw new IllegalArgumentException(
            "Limite de cuentas excedido. Maximo 5, actual: " + cuentas.size());
    }

    private void validarCuentaExistente(CuentaId cuentaId){
        if(cuentas.contains(cuentaId)) throw new IllegalArgumentException(
            "La cuenta " + cuentaId + " ya se encuentra asociada a este cliente " );
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
