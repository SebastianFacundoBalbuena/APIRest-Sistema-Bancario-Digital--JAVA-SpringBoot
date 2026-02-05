package com.banco.domain.model.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;

class TransaccionTest {



    // propiedades para todos los test
    private TransaccionId transaccionId;
    private CuentaId cuentaOrigenId;
    private CuentaId cuentaDestinoId;
    private Dinero montoEur;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    

    @BeforeEach  // PREPARA los datos que TODOS los tests necesitan - Se ejecuta antes de cada TEST.
    void setUp() {
        transaccionId = new TransaccionId("TXN-2024-0000001");
        cuentaOrigenId = CuentaId.newCuentaId("ARG0170000000000000000000");
        cuentaDestinoId = CuentaId.newCuentaId("ARG0170000000000000000001");
        montoEur = new Dinero(new BigDecimal("100.00"), Moneda.EUR);
        descripcion = "Test transaction";
        fechaCreacion = LocalDateTime.now();
    }




    // TESTS DE CONSTRUCCIÓN Y TIPOS

    @Test
    @DisplayName("Crear TRANSFERENCIA válida - Debe crearse en estado PENDIENTE")
    void crearTransferenciaValida_DebeCrearseEstadoPendiente() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId,
            montoEur,
            descripcion
        );
        
        assertThat(transaccion.getTipo()).isEqualTo(TipoTransaccion.TRANSFERENCIA);
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.PENDIENTE);
        assertThat(transaccion.getCuentaOrigen()).isEqualTo(cuentaOrigenId);
        assertThat(transaccion.getCuentaDestino()).isEqualTo(cuentaDestinoId);
        assertThat(transaccion.getMonto()).isEqualTo(montoEur);
        assertThat(transaccion.getDescripcion()).isEqualTo(descripcion);
        assertThat(transaccion.getReferencia()).isNotBlank();
        assertThat(transaccion.getFechaCreacion()).isNotNull();
    }


    @Test
    @DisplayName("Crear DEPOSITO válido - Solo cuenta destino (origen null)")
    void crearDepositoValido_SoloCuentaDestino() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.DEPOSITO,
            null, // No tiene cuenta origen
            cuentaDestinoId,
            montoEur,
            descripcion
        );
        
        assertThat(transaccion.getTipo()).isEqualTo(TipoTransaccion.DEPOSITO);
        assertThat(transaccion.getCuentaOrigen()).isNull();
        assertThat(transaccion.getCuentaDestino()).isEqualTo(cuentaDestinoId);
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.PENDIENTE);
    }


    @Test
    @DisplayName("Crear RETIRO válido - Solo cuenta origen (destino null)")
    void crearRetiroValido_SoloCuentaOrigen() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.RETIRO,
            cuentaOrigenId,
            null, // No tiene cuenta destino
            montoEur,
            descripcion
        );
        
        assertThat(transaccion.getTipo()).isEqualTo(TipoTransaccion.RETIRO);
        assertThat(transaccion.getCuentaOrigen()).isEqualTo(cuentaOrigenId);
        assertThat(transaccion.getCuentaDestino()).isNull();
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.PENDIENTE);
    }



    @Test
    @DisplayName("Crear TRANSFERENCIA sin cuenta origen - Debe lanzar excepción")
    void crearTransferenciaSinCuentaOrigen_DebeLanzarExcepcion() {
        assertThatThrownBy(() -> 
            new Transaccion(
                transaccionId, 
                TipoTransaccion.TRANSFERENCIA,
                null, 
                cuentaDestinoId,
                montoEur,
                descripcion
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Transferencia debe tener cuenta de origen");
    }


    @Test
    @DisplayName("Crear TRANSFERENCIA a misma cuenta - Debe lanzar excepción")
    void crearTransferenciaMismaCuenta_DebeLanzarExcepcion() {
        assertThatThrownBy(() -> 
            new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaOrigenId, // ERROR: Misma cuenta
            montoEur,
            descripcion
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("misma cuenta");
    }


    @Test
    @DisplayName("Crear DEPOSITO con cuenta origen - Debe lanzar excepción")
    void crearDepositoConCuentaOrigen_DebeLanzarExcepcion() {
        assertThatThrownBy(() -> 
            new Transaccion(
                transaccionId, 
                TipoTransaccion.DEPOSITO,
                cuentaOrigenId, // ERROR: Deposito NO debe tener origen
                cuentaDestinoId,
                montoEur,
                descripcion
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Depósito no debe tener cuenta de origen");
    }



    @Test
    @DisplayName("Crear RETIRO con cuenta destino - Debe lanzar excepción")
    void crearRetiroConCuentaDestino_DebeLanzarExcepcion() {
        assertThatThrownBy(() -> 
            new Transaccion(
            transaccionId, 
            TipoTransaccion.RETIRO,
            cuentaOrigenId,
            cuentaDestinoId, // ERROR: Retiro NO debe tener destino
            montoEur,
            descripcion
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Retiro no debe tener cuenta de destino");
    }



    @Test
    @DisplayName("Crear con monto cero o negativo - Debe lanzar excepción")
    void crearConMontoCeroONegativo_DebeLanzarExcepcion() {
        Dinero montoCero = new Dinero(BigDecimal.ZERO, Moneda.EUR);

        
        assertThatThrownBy(() -> 
            new Transaccion(
            transaccionId, 
            TipoTransaccion.DEPOSITO,
            null,
            cuentaDestinoId,
            montoCero,
            descripcion
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El monto debe ser positivo");
        
        assertThatThrownBy(() -> 
            new Transaccion(
            transaccionId, 
            TipoTransaccion.DEPOSITO,
            null,
            cuentaDestinoId,
            new Dinero(new BigDecimal("-10.00"), Moneda.EUR),
            descripcion
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El monto no puede ser negativo ");
    }



    // TESTS DE CICLO DE VIDA (ESTADOS)


    @Nested
    @DisplayName("Ciclo de vida - transiciones de estado")
    class CicloVidaTest{



        @Test
        @DisplayName("completar() transacción PENDIENTE - Debe cambiar a COMPLETADA")
        void completar_TransaccionPendiente_DebeCambiarACompletada() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA, 
            cuentaOrigenId, 
            cuentaDestinoId, 
            montoEur, 
            descripcion);
            

            assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.PENDIENTE);

            transaccion.completar();
            
            assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
        }



        @Test
        @DisplayName("completar() transacción ya COMPLETADA - Debe lanzar excepción")
        void completar_TransaccionYaCompletada_DebeLanzarExcepcion() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA, 
            cuentaOrigenId, 
            cuentaDestinoId, 
            montoEur, 
            descripcion);

            transaccion.completar(); // Primera vez OK
            
            assertThatThrownBy(() -> transaccion.completar())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Solo transacciones pendientes pueden completarse");
        }



        @Test
        @DisplayName("rechazar() transacción PENDIENTE - Debe cambiar a RECHAZADA")
        void rechazar_TransaccionPendiente_DebeCambiarARechazada() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA, 
            cuentaOrigenId, 
            cuentaDestinoId, 
            montoEur, 
            descripcion);
            
            transaccion.rechazar("Fondos insuficientes");
            
            assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.RECHAZADA);
        }



        @Test
        @DisplayName("revertir() transacción COMPLETADA - Debe cambiar a REVERTIDA")
        void revertir_TransaccionCompletada_DebeCambiarARevertida() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA, 
            cuentaOrigenId, 
            cuentaDestinoId, 
            montoEur, 
            descripcion);

            transaccion.completar(); // Primero completar
            
            transaccion.revertir();
            
            assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.REVERTIDA);
        }



        @Test
        @DisplayName("revertir() transacción PENDIENTE - Debe lanzar excepción")
        void revertir_TransaccionPendiente_DebeLanzarExcepcion() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA, 
            cuentaOrigenId, 
            cuentaDestinoId, 
            montoEur, 
            descripcion);
            
            assertThatThrownBy(() -> transaccion.revertir())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Solo transacciones COMPLETADAS pueden revertirse");
        }



        @Test
        @DisplayName("revertir() transacción ya REVERTIDA - Debe lanzar excepción")
        void revertir_TransaccionYaRevertida_DebeLanzarExcepcion() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA, 
            cuentaOrigenId, 
            cuentaDestinoId, 
            montoEur, 
            descripcion);
            
            transaccion.completar();
            transaccion.revertir(); // Primera vez OK
            
            assertThatThrownBy(() -> transaccion.revertir())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo transacciones COMPLETADAS pueden revertirse");
        }
        


    }




    // TESTS DE MÉTODOS DE CONSULTA


    @Nested
    @DisplayName("METODOS DE CONSULTA")
    class MetodoConsultaTest{



        @Test
        @DisplayName("esEntradaPara() cuenta destino TRANSFERENCIA - Debe ser true")
        void esEntradaPara_CuentaDestinoTransferencia_DebeSerTrue() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId,
            montoEur,
            descripcion
            );
            
            assertThat(transaccion.esEntradaPara(cuentaDestinoId)).isTrue();
            assertThat(transaccion.esEntradaPara(cuentaOrigenId)).isFalse();
        }



        @Test
        @DisplayName("esEntradaPara() cuenta destino DEPOSITO - Debe ser true")
        void esEntradaPara_CuentaDestinoDeposito_DebeSerTrue() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.DEPOSITO,
            null,
            cuentaDestinoId,
            montoEur,
            descripcion
            );
            
            assertThat(transaccion.esEntradaPara(cuentaDestinoId)).isTrue();
        }



        @Test
        @DisplayName("esSalidaPara() cuenta origen RETIRO - Debe ser true")
        void esSalidaPara_CuentaOrigenRetiro_DebeSerTrue() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.RETIRO,
            cuentaOrigenId,
            null,
            montoEur,
            descripcion
            );
            
            assertThat(transaccion.esSalidaPara(cuentaOrigenId)).isTrue();
            assertThat(transaccion.esSalidaPara(cuentaDestinoId)).isFalse();
        }



        @Test
        @DisplayName("esSalidaPara() cuenta origen TRANSFERENCIA - Debe ser true")
        void esSalidaPara_CuentaOrigenTransferencia_DebeSerTrue() {
            Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId,
            montoEur,
            descripcion
            );
            
            assertThat(transaccion.esSalidaPara(cuentaOrigenId)).isTrue();
            assertThat(transaccion.esSalidaPara(cuentaDestinoId)).isFalse();
        }



        @Test
        @DisplayName("esReversible() transacción COMPLETADA no antigua - Debe ser true")
        void esReversible_TransaccionCompletadaNoAntigua_DebeSerTrue() {
            // Crear transacción con fecha reciente (no antigua)
            Transaccion transaccion = new Transaccion(
                transaccionId, 
                TipoTransaccion.TRANSFERENCIA,
                cuentaOrigenId,
                cuentaDestinoId,
                montoEur,
                descripcion
            );
            
            transaccion.completar();
            
            assertThat(transaccion.esReversible()).isTrue();
        }



        @Test
        @DisplayName("esReversible() transacción COMISION - Debe ser false (no reversible)")
        void esReversible_TransaccionComision_DebeSerFalse() {
            Transaccion transaccion = new Transaccion(
            new TransaccionId("TXN-2024-0000002"),
            TipoTransaccion.COMISION,
            cuentaOrigenId,
            null,
            new Dinero(new BigDecimal("5.00"), Moneda.EUR),
            "Comisión mensual"
            );
            
            transaccion.completar();
            
            assertThat(transaccion.esReversible()).isFalse();
        }



        @Test
        @DisplayName("esReversible() transacción PENDIENTE - Debe ser false")
        void esReversible_TransaccionPendiente_DebeSerFalse() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId, 
            montoEur,
            descripcion
        );
            
            assertThat(transaccion.esReversible()).isFalse();
        }



        @Test
        @DisplayName("esReversible() transacción RECHAZADA - Debe ser false")
        void esReversible_TransaccionRechazada_DebeSerFalse() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId, 
            montoEur,
            descripcion
        );

            transaccion.rechazar("Fondos insuficientes");
            
            assertThat(transaccion.esReversible()).isFalse();
        }


    }




    // TESTS DE MÉTODOS UTILITARIOS



    @Test
    @DisplayName("getResumen() - Debe incluir tipo, monto, estado y fecha")
    void getResumen_DebeIncluirInformacionBasica() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId, 
            montoEur,
            descripcion
        );

        transaccion.completar();
        
        String resumen = transaccion.getResumen();
        
        assertThat(resumen).contains("TRANSFERENCIA");
        assertThat(resumen).contains("100.00");
        assertThat(resumen).contains("COMPLETADA");
        assertThat(resumen).contains(LocalDateTime.now().toLocalDate().toString());
    }


    @Test
    @DisplayName("generarDetalleExtracto() - Formato para extractos")
    void generarDetalleExtracto_FormatoParaExtractos() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId, 
            montoEur,
            descripcion
        );
        
        String detalle = transaccion.generarDetalleExtracto();
        
        assertThat(detalle).contains("TRANSFERENCIA");
        assertThat(detalle).contains("100.00");
        assertThat(detalle).contains("PENDIENTE");
        assertThat(detalle).contains("Test transaction");
    }



    @Test
    @DisplayName("getReferencia() - Debe generarse automáticamente y no estar vacía")
    void getReferencia_DebeGenerarseAutomaticamente() {
        Transaccion transaccion = new Transaccion(
            transaccionId, 
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId, 
            montoEur,
            descripcion
        );
        
        String referencia = transaccion.getReferencia();
        
        assertThat(referencia).isNotBlank();
        assertThat(referencia).startsWith("REF-");
        assertThat(referencia).contains(transaccionId.toString());
    }



    // TESTS DE CONSTRUCTOR COMPLETO


    @Test
    @DisplayName("Constructor completo - Con todos los parámetros")
    void constructorCompleto_ConTodosParametros() {
        LocalDateTime fechaEspecifica = LocalDateTime.of(
            2024, 
            1, 
            15, 
            10, 
            30);
        String referenciaEspecifica = "REF-123456789";
        
        Transaccion transaccion = new Transaccion(
            transaccionId,
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId,
            fechaEspecifica,
            EstadoTransaccion.COMPLETADA,
            referenciaEspecifica,
            montoEur,
            descripcion
        );
        
        assertThat(transaccion.getFechaCreacion()).isEqualTo(fechaEspecifica);
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
        assertThat(transaccion.getReferencia()).isEqualTo(referenciaEspecifica);
    }



    



}
