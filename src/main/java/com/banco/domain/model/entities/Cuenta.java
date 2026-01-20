package com.banco.domain.model.entities;

import java.util.Objects;

import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;

public class Cuenta {

    // ATRIBUTOS PRIVADOS
    private final CuentaId cuentaId;
    private final ClienteId clienteId;
    private  Dinero saldo;
    private final Moneda moneda; 
    private boolean activa;

    // CONSTRUCTOR 
    public Cuenta(CuentaId cuentaId, ClienteId clienteId, Moneda moneda){

        // VALIDACIONES DE SEGURIDAD - Nunca aceptamos valores nulos
        this.cuentaId = Objects.requireNonNull(cuentaId, "El id de cuenta no puede ser nulo");
        this.clienteId = Objects.requireNonNull(clienteId,"El id de cliente no puede ser nulo");
        this.moneda = Objects.requireNonNull(moneda,"la moneda no puede ser nula");

        // 💰 INICIALIZACIÓN POR DEFECTO
        this.saldo = Dinero.nuevoCero(moneda); // // Todas las cuentas empiezan en CERO
        this.activa = true;

        System.out.println("Cuenta creada: " + cuentaId + "para cliente: " + clienteId);
    }

    public Cuenta(CuentaId cuentaId, ClienteId clienteId, Moneda moneda, Dinero saldo, boolean activa){

        // VALIDACIONES DE SEGURIDAD - Nunca aceptamos valores nulos
        this.cuentaId = Objects.requireNonNull(cuentaId, "El id de cuenta no puede ser nulo");

        this.clienteId = Objects.requireNonNull(clienteId,"El id de cliente no puede ser nulo");

        this.moneda = Objects.requireNonNull(moneda,"la moneda no puede ser nula");

        this.activa = Objects.requireNonNull(activa,"se requiere un valor para -activa-");

        if (!saldo.getMoneda().equals(moneda)) {
        throw new IllegalArgumentException(
        "El saldo debe ser en " + moneda + ". Se recibió: " + saldo.getMoneda());
        }
        else {

            this.saldo = saldo;
        }


    }



    // MÉTODOS DE OPERACIÓNES BÁSICAS

    //DEPOSITAR DINERO
    public void depositar(Dinero monto){
        //VALIDACIONES EN CADENA
        validarPuedeOperar();  // Cuenta debe estar activa
        validarMontoPositivo(monto);  // Monto debe ser positivo

        if (!monto.getMoneda().equals(this.moneda)) {
        throw new IllegalArgumentException(
            "No se puede depositar " + monto.getMoneda() + 
            " en cuenta de " + this.moneda);
    }

        Dinero saldoAnterior = this.saldo;
        this.saldo = this.saldo.sumar(monto);

        System.out.println(" Depósito exitoso: " + saldoAnterior + " → " + this.saldo);
    }

    // RETIRAR DINERO
    public void retirar(Dinero monto){

        validarPuedeOperar();
        validarMontoPositivo(monto);
        verificarSaldoSufuciente(monto);

        Dinero saldoAnterior = this.saldo;
        this.saldo = this.saldo.restar(monto);

        System.out.println("Retiro exitoso: " + saldoAnterior + " → " + this.saldo);
    }

    // TRANSFERENCIAS
    public void transferir(Dinero monto, Cuenta cuentaDestino){

         validarPuedeOperar();
         cuentaDestino.validarPuedeOperar();
         validarMontoPositivo(monto);
         verificarSaldoSufuciente(monto);
         validarCuentaDestino(cuentaDestino);
         validarMonedaCompatible(cuentaDestino);

         Dinero saldoAnterior = this.saldo;
         Dinero saldoDestinatario = cuentaDestino.saldo;

         this.saldo = this.saldo.restar(monto);
         cuentaDestino.saldo = cuentaDestino.saldo.sumar(monto);

         System.out.println("✅ Transferencia exitosa:");
         System.out.println("   Origen: " + saldoAnterior + " → " + this.saldo);
         System.out.println("   Destino: " + saldoDestinatario + " → " + cuentaDestino.saldo);
    }



    // GETTERS

    public CuentaId getCuentaId(){return cuentaId;}
    public ClienteId getClienteId(){return clienteId;}
    public Moneda getMoneda(){return moneda;}
    public Dinero getSaldo(){return saldo;}
    public boolean getActiva(){ return activa;}

    //Solo para casos especiales (administración, fraudes, etc.)
    public void desactivar(){this.activa = false;}
    public void activar(){this.activa = true;}





    // 🛡️ VALIDACIONES DE LA CUENTA
    public void validarPuedeOperar(){
        if(!activa) throw new IllegalStateException("La cuenta " + cuentaId + 
        "esta inactiva y no puede operar");
    }

    public void validarMonedaCompatible(Cuenta otraCuenta){
        if(!moneda.equals(otraCuenta.moneda)) throw new IllegalStateException("No se puede operar entre monedas diferentes");
    }

    public void validarMontoPositivo(Dinero monto){
        if(monto.esMenorOIgualQue(Dinero.nuevoCero(monto.getMoneda()))) throw new IllegalArgumentException(
            "El monto debe ser positivo. Se recibio: " + monto);
    }

    public void verificarSaldoSufuciente(Dinero monto){
        if(!saldo.esMayorOIgualQue(monto)) throw new IllegalStateException(
            "Saldo insuficiente. Se intento: " + monto + ", saldo actual: " + saldo);
    }

    public void validarCuentaDestino(Cuenta cuentaDestino){
        if(cuentaDestino == null) throw new IllegalStateException("La cuenta no puede ser nula");
        if(this.equals(cuentaDestino)) throw new IllegalArgumentException("No se puede transferir a la misma cuenta");
    }


}
