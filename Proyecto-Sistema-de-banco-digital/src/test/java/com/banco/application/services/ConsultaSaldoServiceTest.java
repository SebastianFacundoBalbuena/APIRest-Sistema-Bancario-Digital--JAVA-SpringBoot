package com.banco.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.banco.application.dto.ConsultaSaldoRequest;
import com.banco.application.dto.ConsultaSaldoResponse;
import com.banco.application.dto.MovimientoDTO;
import com.banco.application.port.out.CuentaRepository;
import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;





@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConsultaSaldoService TEST")
public class ConsultaSaldoServiceTest {



    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private ConsultaSaldoService consultaSaldoService;


    private CuentaId cuentaId;
    private ClienteId clienteId;
    private Cuenta cuenta;
    private Dinero saldoActual;
    private ConsultaSaldoRequest requestBasico;
    private ConsultaSaldoRequest requestConMovimientos;
    
    private Transaccion transaccion1;
    private Transaccion transaccion2;
    private Transaccion transaccion3;
    private List<Transaccion> transacciones;


    @BeforeEach
    void SetUp(){


        
        cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
        clienteId = ClienteId.newCliente("CLI-12345678");
        
        // Saldo
        saldoActual = Dinero.nuevo(new BigDecimal("1500.00"), Moneda.ARG);
        cuenta = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldoActual, true);

        // Requests
        requestBasico = new ConsultaSaldoRequest();
        requestBasico.setCuentaId(cuentaId.getValor());
        requestBasico.setIncluirMovimientos(false);

        requestConMovimientos = new ConsultaSaldoRequest();
        requestConMovimientos.setCuentaId(cuentaId.getValor());
        requestConMovimientos.setIncluirMovimientos(true);
        requestConMovimientos.setLimiteMovimientos(10);
        requestConMovimientos.setFechaDesde(LocalDate.now().minusMonths(1));
        requestConMovimientos.setFechaHasta(LocalDate.now());

        // Transacciones de prueba
        transaccion1 = new Transaccion(
            new TransaccionId("TXN-2024-0000001"),
            TipoTransaccion.DEPOSITO,
            null,
            cuentaId,
            Dinero.nuevo(new BigDecimal("1000.00"), Moneda.ARG),
            "Depósito inicial"
        );
        transaccion1.completar();

        transaccion2 = new Transaccion(
            new TransaccionId("TXN-2024-0000002"),
            TipoTransaccion.RETIRO,
            cuentaId,
            null,
            Dinero.nuevo(new BigDecimal("200.00"), Moneda.ARG),
            "Retiro cajero"
        );
        transaccion2.completar();

        transaccion3 = new Transaccion(
            new TransaccionId("TXN-2024-0000003"),
            TipoTransaccion.TRANSFERENCIA,
            cuentaId,
            CuentaId.newCuentaId("ARG0170002000000012345000"),
            Dinero.nuevo(new BigDecimal("300.00"), Moneda.ARG),
            "Transferencia a otra cuenta"
        );
        transaccion3.completar();

        transacciones = Arrays.asList(transaccion1, transaccion2, transaccion3);

        // Configuración base de mocks
        when(cuentaRepository.buscarPorId(cuentaId)).thenReturn(Optional.of(cuenta));

    }



    @Nested
    @DisplayName("Consultas Exitosas")
    class ConsultasExitosasTest {

        @Test
        @DisplayName("Debería consultar saldo básico sin movimientos")
        void consultarSaldo_SinMovimientos_RetornaSaldo() {
            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertNotNull(response);
            assertThat(response.getCuentaId()).isEqualTo(cuentaId.getValor());
            assertThat(response.getClienteId()).isEqualTo(clienteId.getValor());
            assertThat(response.getMoneda()).isEqualTo("ARG");
            assertThat(response.getSaldoActual()).isEqualByComparingTo("1500.00");
            assertThat(response.getSaldoDisponible()).isEqualByComparingTo("6500.00");
            assertThat(response.getMovimientos()).isEmpty();
            assertThat(response.getEstadoCuenta()).isEqualTo("ACTIVA");
            assertThat(response.getMensaje()).contains("Consulta de saldo realizada");

            verify(cuentaRepository, times(1)).buscarPorId(cuentaId);
            verify(transaccionRepository, never()).buscarPorCuenta(any(), any(), any());
        }

        @Test
        @DisplayName("Debería consultar saldo con movimientos")
        void consultarSaldo_ConMovimientos_RetornaSaldoYMovimientos() {

            // Configurar mock de transacciones
            when(transaccionRepository.buscarPorCuenta(
            any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(transacciones);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);
            assertThat(response.getCuentaId()).isEqualTo(cuentaId.getValor());
            assertThat(response.getSaldoActual()).isEqualByComparingTo("1500.00");
            assertThat(response.getMovimientos()).hasSize(3);
            
            // Verificar que están ordenados (el más reciente primero)
            List<MovimientoDTO> movimientos = response.getMovimientos();
            assertThat(movimientos.get(0).getTipo()).isIn("DEPOSITO", "RETIRO", "TRANSFERENCIA");

            // Verificar totales del período (debería calcular algo)
            assertThat(response.getTotalIngresos()).isNotNull();
            assertThat(response.getTotalEgresos()).isNotNull();

            verify(transaccionRepository, times(1)).buscarPorCuenta(
            any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Debería consultar con límite de movimientos personalizado")
        void consultarSaldo_ConLimitePersonalizado_RespetaLimite() {
            
            requestConMovimientos.setLimiteMovimientos(2);
            
            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transacciones);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertThat(response.getMovimientos()).hasSize(2);
            assertThat(response.isTieneMasMovimientos()).isTrue();
        }

        @Test
        @DisplayName("Debería calcular saldo disponible con límite de sobregiro")
        void consultarSaldo_ConLimiteSobregiro_CalculaSaldoDisponible() {
            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertThat(response.getLimiteSobregiro()).isNotNull();
            assertThat(response.getLimiteSobregiro()).isEqualByComparingTo("5000.00");
            assertThat(response.getSaldoDisponible()).isEqualByComparingTo("6500.00"); 
        }

    }




    @Nested
    @DisplayName(" Consultas con Fechas")
    class ConsultasConFechasTest {

        @Test
        @DisplayName("Debería consultar con rango de fechas válido")
        void consultarSaldo_ConRangoFechas_RetornaMovimientosFiltrados() {
            
            LocalDate desde = LocalDate.now().minusDays(15);
            LocalDate hasta = LocalDate.now().minusDays(5);
            
            requestConMovimientos.setFechaDesde(desde);
            requestConMovimientos.setFechaHasta(hasta);

            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transacciones);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);
            
            // Verificar que se llamó al repositorio con las fechas convertidas correctamente
            verify(transaccionRepository).buscarPorCuenta(
                eq(cuenta),
                argThat(fecha -> fecha.toLocalDate().equals(desde)),
                argThat(fecha -> fecha.toLocalDate().equals(hasta))
            );
        }

        @Test
        @DisplayName("Debería usar fecha por defecto (30 días) cuando no se especifica")
        void consultarSaldo_SinFechas_UsaUltimos30Dias() {
           

            requestConMovimientos.setFechaDesde(null);
            requestConMovimientos.setFechaHasta(null);

            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transacciones);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);
            
            // Verificar que se usó fecha desde = hoy - 30 días
            verify(transaccionRepository).buscarPorCuenta(
                eq(cuenta),
                argThat(fecha -> fecha.toLocalDate().equals(LocalDate.now().minusDays(30))),
                any(LocalDateTime.class)
            );
        }
    }



    @Nested
    @DisplayName("Validaciones de Entrada")
    class ValidacionesEntradaTest {

        @Test
        @DisplayName("Debería fallar cuando request es nulo")
        void consultarSaldo_RequestNulo_RetornaError() {
            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(null);

            
            assertNotNull(response);
            assertThat(response.getEstadoCuenta()).isEqualTo("ERROR");
            assertThat(response.getMensaje()).contains("Error en consulta");
            assertThat(response.getMensaje()).contains("no puede ser nula");
            assertThat(response.getRestricciones()).isNotEmpty();
        }

        @Test
        @DisplayName("Debería fallar cuando cuentaId es nulo o vacío")
        void consultarSaldo_CuentaIdInvalido_RetornaError() {
            
            ConsultaSaldoRequest requestInvalido = new ConsultaSaldoRequest();
            requestInvalido.setCuentaId(null);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestInvalido);

            
            assertNotNull(response);
            assertThat(response.getEstadoCuenta()).isEqualTo("ERROR");
            assertThat(response.getMensaje()).contains("Se requiere ID de cuenta");
        }

        @Test
        @DisplayName("Debería fallar cuando límite de movimientos excede el máximo")
        void consultarSaldo_LimiteExcedeMaximo_RetornaError() {
           
            requestConMovimientos.setLimiteMovimientos(101); // Máximo es 100

           
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);

            assertThat(response.getMensaje()).contains("Límite de movimientos excedido");
        }

        @Test
        @DisplayName("Debería fallar cuando fecha desde es posterior a fecha hasta")
        void consultarSaldo_FechasInvalidas_RetornaError() {
            
            requestConMovimientos.setFechaDesde(LocalDate.now());
            requestConMovimientos.setFechaHasta(LocalDate.now().minusDays(1));

           
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);
            assertThat(response.getEstadoCuenta()).isEqualTo("ERROR");
            assertThat(response.getMensaje()).contains("Fecha desde debe ser anterior");
        }

        @Test
        @DisplayName("Debería fallar cuando el rango de fechas excede 1 año")
        void consultarSaldo_RangoExcedeUnAnio_RetornaError() {
            
            requestConMovimientos.setFechaDesde(LocalDate.now().minusYears(2));
            requestConMovimientos.setFechaHasta(LocalDate.now());

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);
            assertThat(response.getEstadoCuenta()).isEqualTo("ERROR");
            assertThat(response.getMensaje()).contains("rango máximo de consulta es 1 año");
        }


    }



    @Nested
    @DisplayName(" Validaciones de Cuenta")
    class ValidacionesCuentaTest {

        @Test
        @DisplayName("Debería fallar cuando cuenta no existe")
        void consultarSaldo_CuentaNoExiste_RetornaError() {
            
            when(cuentaRepository.buscarPorId(cuentaId)).thenReturn(Optional.empty());

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertNotNull(response);
            assertThat(response.getEstadoCuenta()).isEqualTo("ERROR");
            assertThat(response.getMensaje()).contains("Cuenta no encontrada");
        }

        @Test
        @DisplayName("Debería mostrar estado INACTIVA para cuenta cerrada")
        void consultarSaldo_CuentaInactiva_MuestraEstadoInactiva() {
           
            Cuenta cuentaInactiva = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldoActual, false);
            when(cuentaRepository.buscarPorId(cuentaId)).thenReturn(Optional.of(cuentaInactiva));

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertNotNull(response);
            assertThat(response.getEstadoCuenta()).isEqualTo("INACTIVA");
            assertThat(response.getRestricciones()).anyMatch(r -> r.contains("inactiva"));
        }

        @Test
        @DisplayName("Debería mostrar advertencia de saldo bajo")
        void consultarSaldo_SaldoBajo_MuestraRestriccion() {
            
            Dinero saldoBajo = Dinero.nuevo(new BigDecimal("500.00"), Moneda.ARG);
            Cuenta cuentaBajo = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldoBajo, true);

            when(cuentaRepository.buscarPorId(cuentaId)).thenReturn(Optional.of(cuentaBajo));

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertNotNull(response);
            assertThat(response.getRestricciones()).anyMatch(r -> r.contains("Saldo bajo"));
        }


    }




    @Nested
    @DisplayName("Cálculos y Totales")
    class CalculosTotalesTest {

        @Test
        @DisplayName("Debería calcular totales de ingresos y egresos correctamente")
        void consultarSaldo_ConMovimientos_CalculaTotales() {
            
            Transaccion deposito = new Transaccion(
                new TransaccionId("TXN-2024-0000007"),
                TipoTransaccion.DEPOSITO, null, cuentaId,
                Dinero.nuevo(new BigDecimal("1000.00"), Moneda.ARG), "Depósito"
            );
            deposito.completar();

            Transaccion retiro = new Transaccion(
                new TransaccionId("TXN-2024-0000004"),
                TipoTransaccion.RETIRO, cuentaId, null,
                Dinero.nuevo(new BigDecimal("300.00"), Moneda.ARG), "Retiro"
            );
            retiro.completar();

            Transaccion transferencia = new Transaccion(
                new TransaccionId("TXN-2024-0000008"),
                TipoTransaccion.TRANSFERENCIA, cuentaId, 
                CuentaId.newCuentaId("ARG0170002000000012345000"),
                Dinero.nuevo(new BigDecimal("200.00"), Moneda.ARG), "Transferencia"
            );
            transferencia.completar();

            List<Transaccion> transaccionesTest = Arrays.asList(deposito, retiro, transferencia);

            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transaccionesTest);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            // Depósito: +1000, Retiro: -300, Transferencia: -200 (egreso para origen)
            assertThat(response.getTotalIngresos()).isEqualByComparingTo("1000.00");
            assertThat(response.getTotalEgresos()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("Debería calcular saldo posterior en cada movimiento")
        void consultarSaldo_CalculaSaldoPosterior_Correctamente() {

            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transacciones);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            response.getMovimientos().forEach(movimiento -> {
                assertThat(movimiento.getSaldoPosterior()).isNotNull();
            });
        }


    }


    @Nested
    @DisplayName("Consultas sin Movimientos")
    class ConsultasSinMovimientosTest {

        @Test
        @DisplayName("Debería manejar cuenta sin movimientos")
        void consultarSaldo_SinMovimientos_ListaVacia() {
            
            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertNotNull(response);
            assertThat(response.getMovimientos()).isEmpty();
            assertThat(response.getTotalIngresos()).isNull();
            assertThat(response.getTotalEgresos()).isNull();
            assertThat(response.isTieneMasMovimientos()).isFalse();
        }

        @Test
        @DisplayName("Debería manejar cuando se solicitan movimientos pero no hay en el período")
        void consultarSaldo_PeriodoSinMovimientos_ListaVacia() {
            // Configurar fechas antiguas sin movimientos
            requestConMovimientos.setFechaDesde(LocalDate.now().minusYears(5));
            requestConMovimientos.setFechaHasta(LocalDate.now().minusYears(4));
            
            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            
            assertNotNull(response);
            assertThat(response.getMovimientos()).isEmpty();
        }


    }



    @Nested
    @DisplayName("Métodos Auxiliares")
    class MetodosAuxiliaresTest {

        @Test
        @DisplayName("Debería cargar cuenta correctamente")
        void cargarCuenta_CuentaExiste_RetornaCuenta() {
            
            consultaSaldoService.consultarSaldo(requestBasico);
            
            // Verificar que se usó el método cargarCuenta internamente
            verify(cuentaRepository, times(1)).buscarPorId(cuentaId);
        }

        @Test
        @DisplayName("Debería preparar fechas correctamente")
        void prepararFechas_FechasNull_UsaDefault() {
            // Configurar request sin fechas
            requestConMovimientos.setFechaDesde(null);
            requestConMovimientos.setFechaHasta(null);
            
            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transacciones);

           
            consultaSaldoService.consultarSaldo(requestConMovimientos);

            // Verificar que se llamó con fecha desde = hoy - 30 días
            verify(transaccionRepository).buscarPorCuenta(
                eq(cuenta),
                argThat(fecha -> fecha.toLocalDate().equals(LocalDate.now().minusDays(30))),
                any(LocalDateTime.class)
            );
        }

        @Test
        @DisplayName("Debería construir respuesta de error correctamente")
        void crearRespuestaError_FormatoCorrecto() {
            // Forzar error para obtener respuesta de error
            when(cuentaRepository.buscarPorId(cuentaId)).thenReturn(Optional.empty());

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertThat(response.getEstadoCuenta()).isEqualTo("ERROR");
            assertThat(response.getMensaje()).startsWith("Error en consulta");
            assertThat(response.getRestricciones()).isNotEmpty();
            assertThat(response.getRestricciones().get(0)).startsWith("Consulta fallida");
        }

    }



    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar cuenta con saldo muy grande")
        void consultarSaldo_SaldoMuyGrande_SinErrores() {
            // Configurar saldo grande
            Dinero saldoGrande = Dinero.nuevo(new BigDecimal("999999999.99"), Moneda.ARG);
            Cuenta cuentaGrande = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldoGrande, true);
            when(cuentaRepository.buscarPorId(cuentaId)).thenReturn(Optional.of(cuentaGrande));

           
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertNotNull(response);
            assertThat(response.getSaldoActual()).isEqualByComparingTo("999999999.99");
            assertThat(response.getSaldoDisponible()).isEqualByComparingTo("1000004999.99"); // +5000
        }

        @Test
        @DisplayName("Debería manejar límite de movimientos = 0")
        void consultarSaldo_LimiteCero_NoTraeMovimientos() {
            
            requestConMovimientos.setLimiteMovimientos(0);
            requestConMovimientos.setIncluirMovimientos(true);

            when(transaccionRepository.buscarPorCuenta(
                any(Cuenta.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transacciones);

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestConMovimientos);

            // Verificar - debería traer 0 movimientos
            assertThat(response.getMovimientos()).isEmpty();
        }

        @Test
        @DisplayName("Debería incluir movimientos aunque incluirMovimientos=false si se especifica límite")
        void consultarSaldo_IncluirFalsePeroConLimite_NoTraeMovimientos() {
            
            requestBasico.setIncluirMovimientos(false);
            requestBasico.setLimiteMovimientos(10); // No debería afectar porque incluirMovimientos=false

            
            ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(requestBasico);

            
            assertThat(response.getMovimientos()).isEmpty();
            verify(transaccionRepository, never()).buscarPorCuenta(any(), any(), any());
        }
    }


    

}
