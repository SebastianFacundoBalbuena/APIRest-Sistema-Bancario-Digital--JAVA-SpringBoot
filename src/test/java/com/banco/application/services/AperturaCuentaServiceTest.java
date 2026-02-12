package com.banco.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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





@ExtendWith(MockitoExtension.class)
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



}
