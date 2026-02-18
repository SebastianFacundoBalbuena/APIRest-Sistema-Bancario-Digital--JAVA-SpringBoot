package com.banco.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.banco.application.dto.AperturaCuentaRequest;
import com.banco.application.dto.AperturaCuentaResponse;
import com.banco.application.port.out.ClienteRepository;
import com.banco.application.port.out.CuentaRepository;
import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;





@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // Permite mocks no usados
@DisplayName("Test de AperturaCuentaService")
class AperturaCuentaServiceTest {


    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;
    

    @InjectMocks
    private AperturaCuentaService aperturaCuentaService;


    // Propiedades de prueba

    private ClienteId clienteId;
    private Cliente cliente;
    private AperturaCuentaRequest requestValido;
    private AperturaCuentaRequest requestConSaldoInicial;
    private AperturaCuentaRequest requestSinSaldo;



    @BeforeEach  // se ejecuta antes de cada test - inicar datos de prueba
    void SetUp(){


        clienteId = ClienteId.newCliente("CLI-12345678");
        cliente = new Cliente(clienteId, "Juan Pérez", "juan@email.com");


        // Request con saldo inicial (mayor al mínimo de $100)
        requestConSaldoInicial = new AperturaCuentaRequest(
            clienteId.getValor(),
            "CORRIENTE",
            "ARG",
            new BigDecimal("1000.00"),
            "001");


        // Request sin saldo inicial
        requestSinSaldo = new AperturaCuentaRequest(
            clienteId.getValor(),
            "AHORRO",
            "USD",
            null,
            "002");


        // Request válido estándar
        requestValido = requestConSaldoInicial;


        //CONFIGURACION MOCK
        when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(cliente);


    }


    @Nested
    @DisplayName(" TEST de apertura exitosa")
    class AperturaExitosaTest{



        @Test
        @DisplayName("Deberia abrir cuenta existosamente con saldo inicial")
        void abrirCuentaConSaldoInicial_debeFuncionar(){


            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestConSaldoInicial);


            assertNotNull(response);
            assertThat(response.getClienteId().toString()).isEqualTo(clienteId.toString());
            assertThat(response.getTipoCuenta()).isEqualTo("CORRIENTE");
            assertThat(response.getMoneda()).isEqualTo("ARG");
            assertThat(response.getMensaje()).contains("creada exitosamente");
            assertThat(response.getSaldoInicial()).isEqualTo("1000.00");


            verify(clienteRepository,times(1)).buscarPorId(clienteId.getValor());
            verify(clienteRepository, times(1)).actualizar(cliente);
            verify(cuentaRepository, times(1)).guardar(any(Cuenta.class));
            verify(transaccionRepository,times(1)).guardar(any(Transaccion.class));


        }



        @Test
        @DisplayName("Deberia abrir cuenta existosamente sin saldo inicial")
        void abrirCuentaSinSaldoInicial_debeFuncionar(){


            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestSinSaldo);


            assertNotNull(response);
            assertThat(response.getClienteId().toString()).isEqualTo(clienteId.toString());
            assertThat(response.getTipoCuenta()).isEqualTo("AHORRO");
            assertThat(response.getMoneda()).isEqualTo("USD");
            assertThat(response.getMensaje()).contains("creada exitosamente");
            assertThat(response.getSaldoInicial()).isNull();            


            verify(clienteRepository,times(1)).buscarPorId(clienteId.getValor());
            verify(clienteRepository, times(1)).actualizar(cliente);
            verify(cuentaRepository, times(1)).guardar(any(Cuenta.class));
            verify(transaccionRepository, never()).guardar(any(Transaccion.class));


        }




        @Test
        @DisplayName("Debería generar número de cuenta con formato válido")
        void ejecutarAperturaCuenta_RequestValido_FormatoCuentaValido() {

            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            
            String cuentaId = response.getCuentaId();
            
            assertNotNull(cuentaId);
            assertThat(response.getCuentaId()).startsWith("ARG"); // País
            assertEquals(25, cuentaId.length()); // ARG + 22 dígitos
            
            // Verificar dígito verificador (debería pasar)
            // assertDoesNotThrow = Ejecuta y asegúrate de que NO lance ninguna excepción.
            assertDoesNotThrow(() -> CuentaId.newCuentaId(cuentaId));
        }



        @Test
        @DisplayName("Debería crear cuenta del tipo correcto según moneda")
        void ejecutarAperturaCuenta_TiposCuenta_CuentaConTipoCorrecto() {

            
            // Test para cada moneda
            AperturaCuentaRequest requestPesos = new AperturaCuentaRequest(
                clienteId.getValor(), "CORRIENTE", "ARG", new BigDecimal("500.00"), "001"
            );
            
            AperturaCuentaRequest requestDolares = new AperturaCuentaRequest(
                clienteId.getValor(), "AHORRO", "USD", new BigDecimal("500.00"), "001"
            );
            
            AperturaCuentaRequest requestEuros = new AperturaCuentaRequest(
                clienteId.getValor(), "AHORRO", "EUR", new BigDecimal("500.00"), "001"
            );
            
            // When & Then
            AperturaCuentaResponse responsePesos = aperturaCuentaService.ejecutarAperturaCuenta(requestPesos);
            
            AperturaCuentaResponse responseDolares = aperturaCuentaService.ejecutarAperturaCuenta(requestDolares);

            AperturaCuentaResponse responseEuros = aperturaCuentaService.ejecutarAperturaCuenta(requestEuros);


            assertNotNull(responsePesos.getCuentaId());
            assertNotNull(responseDolares.getCuentaId());
            assertNotNull(responseEuros.getCuentaId());
        }



    }



    @Nested
    @DisplayName("Validaciones de Entrada")
    class ValidacionesEntradaTest {
        
        @Test
        @DisplayName(" Debería fallar cuando request es nulo")
        void ejecutarAperturaCuenta_RequestNulo_RespuestaError() {
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(null);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains("La solicitud no puede ser nula");
            
            verify(clienteRepository, never()).buscarPorId(anyString());
        }
        
        @Test
        @DisplayName("Debería fallar cuando clienteId es nulo")
        void ejecutarAperturaCuenta_ClienteIdNulo_RespuestaError() {
            
            AperturaCuentaRequest requestInvalido = new AperturaCuentaRequest(
                null, "CORRIENTE", "ARG", new BigDecimal("1000.00"), "001"
            );
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestInvalido);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains("Se necesita id del cliente");
            
            verify(clienteRepository, never()).buscarPorId(any(String.class));
        }
        
        @Test
        @DisplayName(" Debería fallar cuando moneda es nula")
        void ejecutarAperturaCuenta_MonedaNula_RespuestaError() {
            // Given
            AperturaCuentaRequest requestInvalido = new AperturaCuentaRequest(
                clienteId.getValor(), "CORRIENTE", null, new BigDecimal("1000.00"), "001"
            );
            
            // When
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestInvalido);
            
            // Then
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains(" Se requiere moneda");
            
            verify(clienteRepository, never()).buscarPorId(any(String.class));
        }
        
        @Test
        @DisplayName(" Debería fallar cuando tipoCuenta es nulo")
        void ejecutarAperturaCuenta_TipoCuentaNulo_RespuestaError() {
            
            AperturaCuentaRequest requestInvalido = new AperturaCuentaRequest(
                clienteId.getValor(), null, "ARG", new BigDecimal("1000.00"), "001"
            );
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestInvalido);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains(" Se requiere tipo de cuenta");
            
            verify(clienteRepository, never()).buscarPorId(anyString());
        }
        
        @Test
        @DisplayName("Debería fallar cuando saldo inicial es negativo")
        void ejecutarAperturaCuenta_SaldoInicialNegativo_RespuestaError() {
            // Given
            AperturaCuentaRequest requestInvalido = new AperturaCuentaRequest(
                clienteId.getValor(),
                "CORRIENTE",
                "ARG",
                new BigDecimal("-500.00"), // Saldo negativo
                "001"
            );
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestInvalido);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains(" El saldo inicial no puede ser menor a 0");
            
            verify(clienteRepository, never()).buscarPorId(anyString());
        }


    }



    @Nested
    @DisplayName("Validaciones de Cliente")
    class ValidacionesClienteTest {
        
        @Test
        @DisplayName(" Debería fallar cuando cliente no existe")
        void ejecutarAperturaCuenta_ClienteNoExiste_RespuestaError() {
            
            when(clienteRepository.buscarPorId(clienteId.getValor()))
            .thenReturn(null);
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains("is null");
            
            verify(clienteRepository, times(1)).buscarPorId(clienteId.getValor());
            verify(clienteRepository, never()).actualizar(any(Cliente.class));
            verify(cuentaRepository, never()).guardar(any(Cuenta.class));
        }
        
        @Test
        @DisplayName("Debería fallar cuando cliente está inactivo")
        void ejecutarAperturaCuenta_ClienteInactivo_RespuestaError() {
            
            Cliente clienteInactivo = new Cliente(
                clienteId, 
                "Juan Pérez", 
                "juan@email.com", 
                false, 
                cliente.getCuentas()
            );
            
            when(clienteRepository.buscarPorId(clienteId.getValor()))
            .thenReturn(clienteInactivo);
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains("inactivo");
            
            verify(clienteRepository, times(1)).buscarPorId(clienteId.getValor());
            verify(clienteRepository, never()).actualizar(any(Cliente.class));
            verify(cuentaRepository, never()).guardar(any(Cuenta.class));
        }
        
        @Test
        @DisplayName("Debería fallar cuando cliente ya tiene 5 cuentas")
        void ejecutarAperturaCuenta_ClienteConMaxCuentas_RespuestaError() {
            
            CuentaId cuenta1 = CuentaId.newCuentaId("ARG0170001000000012345000");
            CuentaId cuenta2 = CuentaId.newCuentaId("ARG0170001000000012345010");
            CuentaId cuenta3 = CuentaId.newCuentaId("ARG0170001000000012345020");
            CuentaId cuenta4 = CuentaId.newCuentaId("ARG0170001000000012345030");
            CuentaId cuenta5 = CuentaId.newCuentaId("ARG0170001000000012345040");
            
            Cliente clienteCon5Cuentas = new Cliente(
                clienteId,
                "Juan Pérez",
                "juan@email.com",
                true,
                List.of(cuenta1, cuenta2, cuenta3, cuenta4, cuenta5)
            );
            
            when(clienteRepository.buscarPorId(clienteId.getValor()))
            .thenReturn(clienteCon5Cuentas);
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains("El cliente excede limite de cuentas");
            
            verify(clienteRepository, times(1)).buscarPorId(clienteId.getValor());
            verify(clienteRepository, never()).actualizar(any(Cliente.class));
            verify(cuentaRepository, never()).guardar(any(Cuenta.class));
        }


    }




    @Nested
    @DisplayName("Validaciones de Saldo Inicial")
    class ValidacionesSaldoInicialTest {
        
        @Test
        @DisplayName(" Debería fallar cuando saldo inicial es menor al mínimo")
        void ejecutarAperturaCuenta_SaldoInicialMenorMinimo_RespuestaError() {
            
            
            AperturaCuentaRequest requestSaldoBajo = new AperturaCuentaRequest(
                clienteId.getValor(),
                "CORRIENTE",
                "ARG",
                new BigDecimal("50.00"), // Menor al mínimo de $100
                "001"
            );
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestSaldoBajo);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertThat(response.getMensaje()).contains("Saldo inicial minimo $100");
            
            verify(clienteRepository, times(1)).buscarPorId(clienteId.getValor());
            verify(clienteRepository, never()).actualizar(any(Cliente.class));
            verify(cuentaRepository, never()).guardar(any(Cuenta.class));
            verify(transaccionRepository, never()).guardar(any(Transaccion.class));
        }
        
        @Test
        @DisplayName("Debería aceptar saldo inicial exactamente igual al mínimo")
        void ejecutarAperturaCuenta_SaldoInicialIgualMinimo_CuentaCreada() {
            
            
            AperturaCuentaRequest requestSaldoMinimo = new AperturaCuentaRequest(
                clienteId.getValor(),
                "CORRIENTE",
                "ARG",
                new BigDecimal("100.00"), 
                "001"
            );
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestSaldoMinimo);
            
            
            assertNotNull(response);
            assertNotNull(response.getCuentaId());
            assertThat(response.getSaldoInicial()).isEqualTo("100.00");
            
            verify(clienteRepository, times(1)).actualizar(cliente);
            verify(cuentaRepository, times(1)).guardar(any(Cuenta.class));
            verify(transaccionRepository, times(1)).guardar(any(Transaccion.class));
        }
        
        @Test
        @DisplayName("Debería crear transacción de depósito cuando hay saldo inicial")
        void ejecutarAperturaCuenta_ConSaldoInicial_CreaTransaccion() {
            
                
            aperturaCuentaService.ejecutarAperturaCuenta(requestConSaldoInicial);
            
            
            verify(transaccionRepository, times(1)).guardar(any(Transaccion.class));
        }


    }



    @Nested
    @DisplayName("Generación de Identificadores")
    class GeneracionIdsTest {
        
        @Test
        @DisplayName("Debería generar IDs de cuenta únicos")
        void generarNumeroCuenta_MultiplesLlamadas_IdsUnicos() {
            
            
            
            AperturaCuentaResponse response1 = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            AperturaCuentaResponse response2 = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            AperturaCuentaResponse response3 = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            
            
            assertNotNull(response1.getCuentaId());
            assertNotNull(response2.getCuentaId());
            assertNotNull(response3.getCuentaId());
            
            assertThat(response1.getCuentaId()).isNotEqualTo(response2);
            assertThat(response1.getCuentaId()).isNotEqualTo(response3);
            assertThat(response2.getCuentaId()).isNotEqualTo(response3);
        }
        
        @Test
        @DisplayName("Debería generar IDs de transacción válidos")
        void generarIdTransaccion_FormatoValido() {

            
            
            aperturaCuentaService.ejecutarAperturaCuenta(requestConSaldoInicial);
            
            // argThat - validar argumentos cuando se llama a un método mockeado
            verify(transaccionRepository).guardar(argThat(transaccion -> {
                String id = transaccion.getId().getValor();
                return id.matches("^TXN-\\d{4}-\\d{7}$");
            }));
        }
        

        @Test
        @DisplayName("Debería calcular dígito verificador correctamente")
        void generarNumeroCuenta_DigitoVerificador_Valido() {
            
            
            AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(requestValido);
            
            //El ID completo debería pasar la validación
            String cuentaId = response.getCuentaId();
            assertDoesNotThrow(() -> CuentaId.newCuentaId(cuentaId));
        }

    }




    @Nested
    @DisplayName("Cierre de Cuenta")
    class CierreCuentaTest {
        



        @Test
        @DisplayName("Debería cerrar cuenta exitosamente con saldo cero")
        void cerrarCuenta_CuentaActivaSaldoCero_CuentaCerrada() {
            
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
            Dinero saldoCero = Dinero.nuevoCero(Moneda.ARG);
            Cuenta cuenta = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldoCero, true);
            
            when(cuentaRepository.buscarPorId(cuentaId))
            .thenReturn(Optional.of(cuenta));
            
            
            assertDoesNotThrow(() -> aperturaCuentaService.cerrarCuenta(cuentaId.getValor()));
            
            
            verify(cuentaRepository, times(1)).buscarPorId(cuentaId);
            verify(cuentaRepository, times(1)).actualizar(cuenta);
            assertThat(cuenta.getActiva()).isFalse();
        }
        
        @Test
        @DisplayName("Debería fallar al cerrar cuenta con saldo positivo")
        void cerrarCuenta_CuentaConSaldo_LanzaExcepcion() {
            
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
            Dinero saldo = Dinero.nuevo(new BigDecimal("1000.00"), Moneda.ARG);
            Cuenta cuenta = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldo, true);
            
            when(cuentaRepository.buscarPorId(cuentaId))
            .thenReturn(Optional.of(cuenta));
            
            assertThatThrownBy(()-> aperturaCuentaService.cerrarCuenta(cuenta.getCuentaId().getValor()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No se puede cerrar cuenta");
            
            verify(cuentaRepository, times(1)).buscarPorId(cuentaId);
            verify(cuentaRepository, never()).actualizar(any(Cuenta.class));
        }
        
        @Test
        @DisplayName("Debería fallar al cerrar cuenta inexistente")
        void cerrarCuenta_CuentaNoExiste_LanzaExcepcion() {
            
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
            
            when(cuentaRepository.buscarPorId(cuentaId))
            .thenReturn(Optional.empty());
            
            assertThatThrownBy(()-> aperturaCuentaService.cerrarCuenta(cuentaId.getValor()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cuenta no encontrada");
            
   
            verify(cuentaRepository, never()).actualizar(any(Cuenta.class));
        }
        
        @Test
        @DisplayName("Debería activar cuenta cerrada exitosamente")
        void abrirCuenta_CuentaInactiva_CuentaActivada() {
            
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
            Dinero saldo = Dinero.nuevo(new BigDecimal("500.00"), Moneda.ARG);
            Cuenta cuenta = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldo, false); 
            
            when(cuentaRepository.buscarPorId(cuentaId))
            .thenReturn(Optional.of(cuenta));
            
            assertThat(cuenta.getActiva()).isFalse();
            
            assertDoesNotThrow(() -> aperturaCuentaService.abrirCuenta(cuentaId.getValor()));
            
            assertThat(cuenta.getActiva()).isTrue();

            verify(cuentaRepository, times(1)).buscarPorId(cuentaId);
            verify(cuentaRepository, times(1)).actualizar(cuenta);

        }


    }



    @Nested
    @DisplayName("Métodos de Respuesta")
    class MetodosRespuestaTest {
        
        @Test
        @DisplayName("Debería crear respuesta exitosa correctamente")
        void crearRespuestaExitosa_DatosValidos_ResponseCompleta() {
            
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
            Cuenta cuenta = new Cuenta(cuentaId, clienteId, Moneda.ARG);
            
            
            AperturaCuentaResponse response = aperturaCuentaService.crearRespuestaExitosa(
            cuenta, requestConSaldoInicial, cliente
            );
            
            
            assertNotNull(response);
            assertThat(cuenta.getCuentaId().getValor()).isEqualTo(response.getCuentaId());
            assertThat(cuenta.getClienteId().getValor()).isEqualTo(response.getClienteId());
            assertThat(requestConSaldoInicial.getTipoCuenta()).isEqualTo(response.getTipoCuenta());
            assertThat(requestConSaldoInicial.getMoneda()).isEqualTo(response.getMoneda());
            assertThat(requestConSaldoInicial.getSaldoInicial()).isEqualTo(response.getSaldoInicial());
            assertNotNull(response.getFechaApertura());
            assertThat(response.getMensaje()).contains("creada exitosamente");
        }


        
        @Test
        @DisplayName("Debería crear respuesta de error correctamente")
        void crearRespuestaError_MensajeError_ResponseConError() {
            
            String mensajeError = "Error de prueba";
            
            
            AperturaCuentaResponse response = aperturaCuentaService.crearRespuestaError(mensajeError);
            
            
            assertNotNull(response);
            assertNull(response.getCuentaId());
            assertNull(response.getClienteId());
            assertNull(response.getTipoCuenta());
            assertNull(response.getMoneda());
            assertThat(response.getSaldoInicial()).isEqualTo(BigDecimal.ZERO);
            assertNotNull(response.getFechaApertura());
            assertThat(response.getMensaje()).contains("Error");
        }
    }



}
