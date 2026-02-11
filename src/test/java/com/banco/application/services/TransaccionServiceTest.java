package com.banco.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.banco.application.dto.MovimientoDTO;
import com.banco.application.dto.TransferenciaRequest;
import com.banco.application.dto.TransferenciaResponse;
import com.banco.application.port.out.CuentaRepository;
import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Permite mocks configurados pero no usados
@DisplayName("TransaccionServices - Test unitarios")
class TransaccionServiceTest {
    


    //MOCKS
    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    // SERVICIO A TESTEAR (con mocks inyectados (@InjectMocks))
    @InjectMocks
    private TransaccionService transaccionService;



    // propiedades de prubea que usaran los test
    private CuentaId cuentaOrigenId;
    private CuentaId cuentaDestinoId;
    private ClienteId clienteId;
    private Dinero dinero;
    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;
    private TransferenciaRequest transferenciaRequest;


    @BeforeEach // Se ejecuta en cada test - Datos de prueba para los test
    void setUp(){

        // Nota: No todos los tests usarán estos mocks

        cuentaOrigenId = CuentaId.newCuentaId("ARG0170001000000012345000");
        cuentaDestinoId = CuentaId.newCuentaId("ARG0170002000000098765000");
        clienteId = ClienteId.newCliente("CLI-12345678");
        dinero = Dinero.nuevo(new BigDecimal("1000.00"), Moneda.ARG);

        // cuentas de prueba
        cuentaOrigen = new Cuenta(cuentaOrigenId, clienteId, Moneda.ARG, dinero, true);
        cuentaDestino = new Cuenta(cuentaDestinoId, clienteId, Moneda.ARG, 
        Dinero.nuevo(new BigDecimal("500.00"), Moneda.ARG), true);

        //REQUEST DE TRANSFERENCIA
        transferenciaRequest = new TransferenciaRequest(
            cuentaOrigenId.getValor(),
            cuentaDestinoId.getValor(),
            new BigDecimal("1000.00"),
            "ARG",
            "Test transferencia"
        );


        // Mocks configurados - cuando(when) y entonces(then)
        when(cuentaRepository.buscarPorId(cuentaOrigenId)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.buscarPorId(cuentaDestinoId)).thenReturn(Optional.of(cuentaDestino));

    }




    @Nested
    @DisplayName("Validaciones basicas")
    class ValidacionesBasicasTest{



        @Test
        @DisplayName("Debería lanzar excepción cuando request es nulo")
        void ejecutarTransferencia_RequestNulo_LanzaExcepcion() {

            assertThatThrownBy(()-> transaccionService.ejecutarTransferencia(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La solicitud no puede ser nula");
        }



        @Test
        @DisplayName("Deberia lanzar exception cuando monto es cero")
        void ejecutarTransferencia_debeLanzarExceptionSiMontoEsCero(){

            TransferenciaRequest request = new TransferenciaRequest(
            "ARG0170001000000012345000",
            "ARG0170002000000098765000",
            BigDecimal.ZERO, // Monto cero
            "ARG",
            "Test"
            );


            assertThatThrownBy(()-> transaccionService.ejecutarTransferencia(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El monto debe ser positivo");

        }



        @Test
        @DisplayName("Deberia lanzar exception cuando cuentas son iguales")
        void ejecutarTranbsferencia_CuentasIguales_LanzaException(){


            TransferenciaRequest request = new TransferenciaRequest(
            "ARG0170001000000012345000",
            "ARG0170001000000012345000",
            BigDecimal.ZERO, // Monto cero
            "ARG",
            "Test"
            );

            assertThatThrownBy(()-> transaccionService.ejecutarTransferencia(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No se puede transferir a la misma cuenta");

        }

    }



    @Nested
    @DisplayName("Transferencia exitosa")
    class TransferenciaExitosaTest{




        @Test
        @DisplayName("deberia transferir exitosamente entre cuentas validas")
        void ejecutarTransferencia_CuentasValidas_deberiaFuncionar(){


            TransferenciaResponse response = transaccionService.ejecutarTransferencia(transferenciaRequest);

            assertNotNull(response);
            assertThat(response.getEstado()).isEqualTo("COMPLETADA");
            assertThat(response.getMonto()).isEqualTo(new BigDecimal("1000.00"));
            assertThat(response.getCuentaOrigenId()).isEqualTo(cuentaOrigenId.getValor());
            assertThat(response.getCuentaDestinoId()).isEqualTo(cuentaDestinoId.getValor());
            assertThat(response.getMensaje()).isEqualTo("Transaccion realizada exitosamente");

            // verificamos las llamadas a cuentaRepository (times(1) esperamos x llamada)
            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);
            verify(cuentaRepository, times(1)).buscarPorId(cuentaDestinoId);
            verify(cuentaRepository, times(1)).actualizar(cuentaOrigen);
            verify(cuentaRepository, times(1)).actualizar(cuentaDestino);
            verify(transaccionRepository, times(1)).guardar(any(Transaccion.class));



        }



        @Test
        @DisplayName("Deberia manejar transferencia con fondo justo")
        void ejecutarTransferencia_ConFondoJusto_deberiaFuncionar(){


            TransferenciaResponse response = transaccionService.ejecutarTransferencia(transferenciaRequest);

            assertNotNull(response);
            assertThat(response.getEstado()).isEqualTo("COMPLETADA");
            assertThat(response.getMonto()).isEqualTo("1000.00");

        }


    }



    @Nested
    @DisplayName("Test de errores")
    class ErroresTest{



        @Test
        @DisplayName("Transferencia con errores")
        void cuentaOrigenNoExiste_DeberiaFallar(){

            // cuando busque cuenta origen - devolve vacio
            when(cuentaRepository.buscarPorId(cuentaOrigenId)).thenReturn(Optional.empty());

            assertThatThrownBy(()-> transaccionService.ejecutarTransferencia(transferenciaRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cuenta de origen no encontrada");

            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);
            verify(cuentaRepository, never()).buscarPorId(cuentaDestinoId);
 

        }



        @Test
        @DisplayName("Transferencia con errores")
        void cuentaDestinoNoExiste_DeberiaFallar(){

            // cuando busque cuenta origen - devolve vacio
            when(cuentaRepository.buscarPorId(cuentaDestinoId)).thenReturn(Optional.empty());

            assertThatThrownBy(()-> transaccionService.ejecutarTransferencia(transferenciaRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cuenta de destino no encontrada");

            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);
            verify(cuentaRepository, times(1)).buscarPorId(cuentaDestinoId);
 

        }



        @Test
        @DisplayName("Fondos son insuficientes - deberia fallar")
        void ejecutarTransferencia_FondosInsuficientes_DeberiaFallar(){


            TransferenciaRequest requestGrande = new TransferenciaRequest(
            cuentaOrigenId.getValor(),
            cuentaDestinoId.getValor(),
            new BigDecimal("1500.00"), // Más que el saldo
            "ARG",
            "Transferencia grande");


            TransferenciaResponse response = transaccionService.ejecutarTransferencia(requestGrande);

            assertNotNull(response);
            assertThat(response.getEstado()).isEqualTo("RECHAZADA");
            assertThat(response.getMensaje()).contains("Transferencia fallida Saldo insuficiente.");


        }


        @Test
        @DisplayName("Cuenta origen esta inactiva- deberia fallar")
        void cuentaOrigenEstaInactiva_deberiaFallar(){

            Cuenta cuentaOrigenInactiva = new Cuenta(cuentaOrigenId, clienteId, Moneda.ARG, dinero, false);

            when(cuentaRepository.buscarPorId(cuentaOrigenId)).thenReturn(Optional.of(cuentaOrigenInactiva));

            TransferenciaResponse response = transaccionService.ejecutarTransferencia(transferenciaRequest);

            assertThat(response.getMensaje()).contains("esta inactiva y no puede operar");

        }




        @Test
        @DisplayName("Ejecutar Transferencia con moneda incompatible- debe fallar")
        void ejecutarTransferenciaMonedaIncompatible_debeFallar(){

            Cuenta cuentaOrigenUSD = new Cuenta(cuentaOrigenId, clienteId, Moneda.USD, 
            Dinero.nuevo(new BigDecimal("1000.00"), Moneda.USD), true);

            when(cuentaRepository.buscarPorId(cuentaOrigenId)).thenReturn(Optional.of(cuentaOrigenUSD));


            TransferenciaRequest requestMonedaDiferente = new TransferenciaRequest(
            cuentaOrigenId.getValor(),
            cuentaDestinoId.getValor(), // Esta cuenta es ARG
            new BigDecimal("1000.00"),
            "USD", // Moneda diferente
            "Test moneda diferente");


            TransferenciaResponse response = transaccionService.ejecutarTransferencia(requestMonedaDiferente);

            assertThat(response.getMensaje()).contains("Transferencia fallida. No se puede operar entre monedas diferentes");


        }



    }





    @Nested
    @DisplayName(" Operaciones de Depósito")
    class OperacionesDepositoTest {
        
        @Test
        @DisplayName("Debería depositar exitosamente en cuenta válida")
        void depositar_CuentaValidaMontoPositivo_DepositoExitoso() {
            
            
            
            Transaccion transaccion = transaccionService.depositar(
            cuentaOrigenId.getValor(),
            new BigDecimal("500.00"),
            "ARG",
            "Depósito test");
            
            
            assertNotNull(transaccion);
            assertThat(transaccion.getTipo()).isEqualTo(TipoTransaccion.DEPOSITO);
            
            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);
            verify(cuentaRepository, times(1)).actualizar(cuentaOrigen);
            verify(transaccionRepository, times(1)).guardar(any(Transaccion.class));
        }
        
        @Test
        @DisplayName("Debería fallar al depositar en cuenta inexistente")
        void depositar_CuentaNoExiste_LanzaExcepcion() {
            
            when(cuentaRepository.buscarPorId(cuentaOrigenId))
                .thenReturn(Optional.empty());
            
            
            assertThatThrownBy(()-> transaccionService.depositar(
            cuentaOrigenId.getValor(),
            new BigDecimal("500.00"),
            "ARG",
            "Depósito test")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error de deposito: Cuenta no encontrada");


        }
        
        @Test
        @DisplayName("Debería fallar al depositar monto negativo")
        void depositar_MontoNegativo_LanzaExcepcion() {

            assertThatThrownBy(()-> transaccionService.depositar(
            cuentaOrigenId.getValor(),
            new BigDecimal("-500.00"),
            "ARG",
            "Depósito test")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Error de deposito: El monto no puede ser negativo");

        }
    }




    @Nested
    @DisplayName("Operaciones de retiro")
    class OperacionesRetiroTest{




        @Test
        @DisplayName("Retirar de cuenta con fondos - deberia funcionar")
        void retiroCuentaConFondos_debeFuncionar(){


            Transaccion transaccion= transaccionService.retirar(
                cuentaOrigenId.getValor(), 
                new BigDecimal("500.00"), 
                "ARG", 
                "retiro test");


            assertNotNull(transaccion);
            assertThat(transaccion.getTipo()).isEqualTo(TipoTransaccion.RETIRO);
            assertThat(transaccion.getDescripcion()).contains("retiro test");
            assertThat(cuentaOrigen.getSaldo().getMontoConEscalaMoneda()).isEqualTo("500.00");

            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);
            verify(cuentaRepository,times(1)).actualizar(cuentaOrigen);
            verify(transaccionRepository, times(1)).guardar(transaccion);


        }



        @Test
        @DisplayName("Retirar de cuenta sin fondos - debe fallar")
        void retiroCuentaSinFondos_debeFallar(){


            assertThatThrownBy(()-> transaccionService.retirar(
                cuentaOrigenId.getValor(), 
                new BigDecimal("1500.00"), 
                "ARG", 
                "retiro fallido"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo insuficiente"); 


            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);



        }




        @Test
        @DisplayName("Retirar de cuenta inactiva - debe fallar")
        void retiroCuentaInactiva_debeFallar(){

            Cuenta cuentaInactiva = new Cuenta(cuentaOrigenId, clienteId, Moneda.ARG, dinero, false);

            when(cuentaRepository.buscarPorId(cuentaOrigenId)).thenReturn(Optional.of(cuentaInactiva));

            assertThatThrownBy(()-> transaccionService.retirar(
                cuentaOrigenId.getValor(), 
                new BigDecimal("500.00"), 
                "ARG", 
                "retiro fallido"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactiva"); 


            verify(cuentaRepository, times(1)).buscarPorId(cuentaOrigenId);



        }


    }




    @Nested
    @DisplayName("Operaciones de reversion")
    class OperacionesReversionTest{




        @Test
        @DisplayName("Transaccion reversible - deberia revertir exitosamente")
        void revertirTransaccion_DeberiaFuncionar(){



            TransaccionId transaccionId = new TransaccionId("TXN-2024-0000001");
            Transaccion transaccionOriginal = new Transaccion(
                transaccionId, 
                TipoTransaccion.TRANSFERENCIA, 
                cuentaOrigenId, 
                cuentaDestinoId, 
                new Dinero(new BigDecimal("100.00"), Moneda.ARG), 
                "transferencia a revertir");

                transaccionOriginal.completar();

                when(transaccionRepository.buscarPorId(any(TransaccionId.class))).thenReturn(Optional.of(transaccionOriginal));

                Transaccion response = transaccionService.revertir(transaccionId.getValor());


                assertNotNull(response);
                assertThat(response.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
                assertThat(response.getTipo()).isEqualTo(TipoTransaccion.REVERSO);


                verify(transaccionRepository, times(1)).buscarPorId(any(TransaccionId.class));
                verify(cuentaRepository, times(2)).buscarPorId(any(CuentaId.class));
                verify(transaccionRepository, times(2)).guardar(any(Transaccion.class));

        }





        @Test
        @DisplayName("Transaccion reversible no encontrada - deberia fallar")
        void revertirTransaccionNoEncontrada_DeberiaFallar(){



            TransaccionId transaccionId = new TransaccionId("TXN-2024-0000001");


                when(transaccionRepository.buscarPorId(any(TransaccionId.class))).thenReturn(Optional.empty());

                assertThatThrownBy(()-> transaccionService.revertir(transaccionId.getValor()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrada"); 


                verify(transaccionRepository, times(1)).buscarPorId(any(TransaccionId.class));
                verify(cuentaRepository, never()).buscarPorId(any(CuentaId.class));
                verify(transaccionRepository, never()).guardar(any(Transaccion.class));

        }



        @Test
        @DisplayName("Transaccion no reversible  - deberia fallar")
        void revertirTransaccionNoReversible_DeberiaFallar(){



            TransaccionId transaccionId = new TransaccionId("TXN-2024-0000001");
            Transaccion transaccionNoReversible = new Transaccion(
                transaccionId, 
                TipoTransaccion.TRANSFERENCIA, 
                cuentaOrigenId, 
                cuentaDestinoId, 
                new Dinero(new BigDecimal("100.00"), Moneda.ARG), 
                "transferencia a revertir");


                when(transaccionRepository.buscarPorId(any(TransaccionId.class))).thenReturn(Optional.of(transaccionNoReversible));

                assertThatThrownBy(()-> transaccionService.revertir(transaccionId.getValor()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no reversible"); 


                verify(transaccionRepository, times(1)).buscarPorId(any(TransaccionId.class));
                verify(cuentaRepository, never()).buscarPorId(any(CuentaId.class));
                verify(transaccionRepository, never()).guardar(any(Transaccion.class));

        }


    }




    @Nested
    @DisplayName("Test de consulta")
    class ConsultaTest{



        
        @Test
        @DisplayName("Deberia consultar movimientos de cuenta existente")
        void consultaDeMovimientoCuentas_DeriaSalirBien(){



            Transaccion transaccion1 = new Transaccion(
                new TransaccionId("TXN-2024-0000001"),
                TipoTransaccion.DEPOSITO,
                null,
                cuentaOrigenId,
                Dinero.nuevo(new BigDecimal("1000.00"), Moneda.ARG),
                "Depósito inicial"
            );
            transaccion1.completar();

            Transaccion transaccion2 = new Transaccion(
                new TransaccionId("TXN-2024-0000002"),
                TipoTransaccion.RETIRO,
                cuentaOrigenId,
                null,
                Dinero.nuevo(new BigDecimal("200.00"), Moneda.ARG),
                "Retiro cajero"
            );
            transaccion2.completar();

             List<Transaccion> transacciones = Arrays.asList(transaccion1, transaccion2);

             when(transaccionRepository.buscarCuentas(cuentaOrigenId)).thenReturn(transacciones);

            List<MovimientoDTO> listTransaccion = transaccionService.consultarMovimiento(cuentaOrigenId.getValor());

            

            assertNotNull(listTransaccion);
            assertEquals(2, listTransaccion.size());


            verify(transaccionRepository, times(1)).buscarCuentas(cuentaOrigenId);

        }


        @Test
        @DisplayName(" Debería lanzar excepción al consultar cuenta inválida")
        void consultarMovimientos_CuentaInvalida_LanzaExcepcion() {
            
            assertThatThrownBy(()-> transaccionService.consultarMovimiento("cuenta-invalida"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Hubo un error al intentar consultar movimientos");
            

        }



        @Test
        @DisplayName("Debería devolver lista vacía cuando no hay movimientos")
        void consultarMovimientos_CuentaSinMovimientos_ListaVacia() {

            when(transaccionRepository.buscarCuentas(cuentaOrigenId))
                .thenReturn(Arrays.asList());
            
            
            List<MovimientoDTO> movimientos = transaccionService.consultarMovimiento(cuentaOrigenId.getValor());
            
   
            assertNotNull(movimientos);
            assertTrue(movimientos.isEmpty());
        }



    }

}
