package com.banco.domain.model.entities;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;

class CuentaTest {


    // propiedades que usaran los test
    private CuentaId cuentaId;
    private ClienteId clienteId;
    private Moneda moneda;
    private Dinero dinero;
    
    @BeforeEach // PREPARA los datos que TODOS los tests necesitan - Se ejecuta antes de cada TEST. 
    void setUp(){

        cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
        clienteId = ClienteId.newCliente("CLI-12345678");
        moneda = Moneda.EUR;
        dinero = Dinero.nuevo(new BigDecimal(100.00), moneda);
    }



    @Test
    @DisplayName("Constructor - crea cuenta con saldo cero por defecto")
    void constructor_CreaCuentaConSaldoCeroPorDefecto(){

        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda);

        assertThat(cuenta.getCuentaId()).isEqualTo(cuentaId);
        assertThat(cuenta.getClienteId()).isEqualTo(clienteId);
        assertThat(cuenta.getMoneda()).isEqualTo(moneda);
        assertThat(cuenta.getActiva()).isTrue();
        assertThat(cuenta.getSaldo().esCero()).isTrue();
    }



    @Test
    @DisplayName("Constructor - crea cuenta con saldo especifico")
    void constructor_CreaCuentConSaldoEspecifico(){

        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);

        assertThat(cuenta.getActiva()).isTrue();
        assertThat(cuenta.getSaldo()).isEqualTo(dinero);
    }


    @Test
    @DisplayName("constructor - crea con saldo moneda diferente - debe lanzar exception")
    void constructor_creaConSaldoMonedaDiferente_DebeLanzarException(){

        Dinero dineroUsd = new Dinero(new BigDecimal("100.00"), Moneda.USD);

        assertThatThrownBy(()-> new Cuenta(cuentaId, clienteId, moneda, dineroUsd, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El saldo debe ser en " + moneda);
    }


    @Test
    @DisplayName("depositar() - monto positivo misma moneda - debe aunmentar saldo")
    void depositarMontoPositivoMinsmaMoneda_debeAumentarSaldo(){

        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        Dinero aumentar = new Dinero(new BigDecimal("50.00"), moneda);

        cuenta.depositar(aumentar);

        assertThat(cuenta.getSaldo().getMontoConEscalaMoneda()).isEqualTo("150.00");
        

    }


    @Test
    @DisplayName("depositar() - moneda diferente,  debe lanzar exception")
    void depositarConMonedaDiferente_DebeLanzarException(){

        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        Dinero aumentar = new Dinero(new BigDecimal("50.00"), Moneda.USD);

        assertThatThrownBy(()-> cuenta.depositar(aumentar))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No se puede depositar " + aumentar.getMoneda() + " en cuenta de " + moneda);
    }



    @Test
    @DisplayName("depositar() - en cuenta inactiva, debe lanzar exception")
    void depositarEnCuentaInactiva_DebeLanzarException(){

        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, false);
        Dinero aumentar = new Dinero(new BigDecimal("50.00"), moneda);

        assertThatThrownBy(()-> cuenta.depositar(aumentar))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("La cuenta " + cuentaId + " esta inactiva y no puede operar");

    }



    @Test
    @DisplayName("retirar() con saldo suficiente - Debe disminuir saldo")
    void retirar_ConSaldoSuficiente_DebeDisminuirSaldo() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        Dinero montoRetiro = new Dinero(new BigDecimal("30.00"), moneda);
        
        cuenta.retirar(montoRetiro);
        
        Dinero saldoEsperado = dinero.restar(montoRetiro); // 100 - 30 = 70
        assertThat(cuenta.getSaldo()).isEqualTo(saldoEsperado);
    }



    @Test
    @DisplayName("retirar() saldo insuficiente - Debe lanzar excepción")
    void retirar_SaldoInsuficiente_DebeLanzarExcepcion() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        Dinero montoGrande = new Dinero(new BigDecimal("200.00"), moneda);
        
        assertThatThrownBy(() -> cuenta.retirar(montoGrande))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Saldo insuficiente");
    }



    @Test
    @DisplayName("transferir() entre cuentas misma moneda - Debe funcionar")
    void transferir_EntreCuentasMismaMoneda_DebeFuncionar() {
        // cuenta origen con saldo
        Cuenta cuentaOrigen = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        
        // cuenta destino (diferente ID, mismo cliente y moneda)
        CuentaId cuentaDestinoId = CuentaId.newCuentaId("ARG0170000000000000000001");
        Cuenta cuentaDestino = new Cuenta(cuentaDestinoId, clienteId, moneda);
        
        Dinero montoTransferencia = new Dinero(new BigDecimal("40.00"), moneda);
        
        cuentaOrigen.transferir(montoTransferencia, cuentaDestino);
        
        // Verificar saldos
        Dinero saldoOrigenEsperado = dinero.restar(montoTransferencia); // 100 - 40 = 60
        Dinero saldoDestinoEsperado = Dinero.nuevoCero(moneda).sumar(montoTransferencia); // 0 + 40 = 40
        
        assertThat(cuentaOrigen.getSaldo()).isEqualTo(saldoOrigenEsperado);
        assertThat(cuentaDestino.getSaldo()).isEqualTo(saldoDestinoEsperado);
    }


    @Test
    @DisplayName("transferir() a misma cuenta - Debe lanzar excepción")
    void transferir_AMismaCuenta_DebeLanzarExcepcion() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        Dinero monto = new Dinero(new BigDecimal("10.00"), moneda);
        
        assertThatThrownBy(() -> cuenta.transferir(monto, cuenta))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("misma cuenta");
    }


    @Test
    @DisplayName("cerrar() cuenta con saldo cero - Debe desactivar")
    void cerrar_CuentaConSaldoCero_DebeDesactivar() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda); // Saldo cero por defecto
        
        cuenta.cerrar();
        
        assertThat(cuenta.getActiva()).isFalse();
        assertThat(cuenta.tieneSaldoCero()).isTrue();
    }


    @Test
    @DisplayName("cerrar() cuenta con saldo - Debe lanzar excepción")
    void cerrar_CuentaConSaldo_DebeLanzarExcepcion() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        
        assertThatThrownBy(() -> cuenta.cerrar())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No se puede cerrar cuenta con saldo");
    }


    @Test
    @DisplayName("activarCuenta() cuenta desactivada - Debe activar")
    void activarCuenta_CuentaDesactivada_DebeActivar() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, false);
        
        cuenta.activarCuenta();
        
        assertThat(cuenta.getActiva()).isTrue();
    }



    @Test
    @DisplayName("tieneSaldoCero() cuenta con saldo - Debe devolver false")
    void tieneSaldoCero_CuentaConSaldo_DebeDevolverFalse() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        
        assertThat(cuenta.tieneSaldoCero()).isFalse();
    }


    @Test
    @DisplayName("estaActiva() cuenta activa - Debe devolver true")
    void estaActiva_CuentaActiva_DebeDevolverTrue() {
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, dinero, true);
        
        assertThat(cuenta.estaActiva()).isTrue();
    }


}
