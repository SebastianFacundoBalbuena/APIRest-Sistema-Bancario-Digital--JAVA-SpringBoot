package com.banco.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.banco.application.dto.AperturaCuentaRequest;
import com.banco.application.dto.AperturaCuentaResponse;
import com.banco.application.dto.ConsultaSaldoRequest;
import com.banco.application.dto.ConsultaSaldoResponse;
import com.banco.application.services.AperturaCuentaService;
import com.banco.application.services.ConsultaSaldoService;
import com.fasterxml.jackson.databind.ObjectMapper;






@WebMvcTest(CuentaController.class)
public class CuentaControllerTest {
    

    @Autowired
    private MockMvc mockMvc;   // MockMvc simula peticiones HTTP sin levantar un servidor real.

    @Autowired
    private ObjectMapper objectMapper;  // ObjectMapper es un traductor entre Java y JSON.

    @MockitoBean
    private AperturaCuentaService aperturaCuentaService;

    @MockitoBean
    private ConsultaSaldoService consultaSaldoService;

    private AperturaCuentaRequest aperturaRequest;
    private AperturaCuentaResponse aperturaResponse;
    private ConsultaSaldoRequest consultaRequest;
    private ConsultaSaldoResponse consultaResponse;

    @BeforeEach
    void setUp() {
        // Request de apertura
        aperturaRequest = new AperturaCuentaRequest(
            "CLI-12345678",
            "CORRIENTE",
            "ARG",
            new BigDecimal("1000.00"),
            "001"
        );

        // Response de apertura
        aperturaResponse = new AperturaCuentaResponse(
            "ARG0170001000000012345000",
            "CLI-12345678",
            "CORRIENTE",
            "ARG",
            new BigDecimal("1000.00"),
            LocalDateTime.now(),
            "Cuenta creada exitosamente"
        );

        // Request de consulta
        consultaRequest = new ConsultaSaldoRequest();
        consultaRequest.setCuentaId("ARG0170001000000012345000");
        consultaRequest.setIncluirMovimientos(true);
        consultaRequest.setLimiteMovimientos(10);

        // Response de consulta
        consultaResponse = new ConsultaSaldoResponse(
            "ARG0170001000000012345000",
            "CLI-12345678",
            "CORRIENTE",
            "ARG",
            new BigDecimal("1500.50"),
            "Consulta exitosa"
        );
    }




        @Nested
    @DisplayName("POST /api/cuentas - Abrir cuenta")
    class AbrirCuentaTest {

        @Test
        @DisplayName("Debería abrir cuenta y retornar 200 OK")
        void abrirCuenta_DatosValidos_Retorna200() throws Exception {
            
            when(aperturaCuentaService.ejecutarAperturaCuenta(any(AperturaCuentaRequest.class)))
                .thenReturn(aperturaResponse);

            
            mockMvc.perform(post("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aperturaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value("ARG0170001000000012345000"))
                .andExpect(jsonPath("$.clienteId").value("CLI-12345678"))
                .andExpect(jsonPath("$.tipoCuenta").value("CORRIENTE"))
                .andExpect(jsonPath("$.moneda").value("ARG"))
                .andExpect(jsonPath("$.saldoInicial").value(1000.00))
                .andExpect(jsonPath("$.mensaje").value("Cuenta creada exitosamente"));

            verify(aperturaCuentaService, times(1))
                .ejecutarAperturaCuenta(any(AperturaCuentaRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 200 con respuesta de ERROR cuando request es inválido")
        void abrirCuenta_RequestInvalido_Retorna400() throws Exception {

            AperturaCuentaRequest requestInvalido = new AperturaCuentaRequest(
                "", // clienteId vacío
                "CORRIENTE",
                "ARG",
                new BigDecimal("-100.00"), // monto negativo
                "001"
            );

            AperturaCuentaResponse respuestaError = new AperturaCuentaResponse(
            null, null, null, null, BigDecimal.ZERO, LocalDateTime.now(),
            "Error en apertura de cuenta: Se necesita id del cliente");
            

            when(aperturaCuentaService.ejecutarAperturaCuenta(any(AperturaCuentaRequest.class)))
            .thenReturn(respuestaError);
            
            mockMvc.perform(post("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Se necesita id del cliente")));
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el servicio lanza excepción")
        void abrirCuenta_ServiceLanzaExcepcion_Retorna400() throws Exception {
            
            when(aperturaCuentaService.ejecutarAperturaCuenta(any(AperturaCuentaRequest.class)))
                .thenThrow(new IllegalArgumentException("Cliente inactivo"));

           
            mockMvc.perform(post("/api/cuentas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(aperturaRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/cuentas - Consultar saldo")
    class ConsultarSaldoTest {

        @Test
        @DisplayName("Debería consultar saldo y retornar 200 OK")
        void consultarSaldo_RequestValido_Retorna200() throws Exception {
          
            when(consultaSaldoService.consultarSaldo(any(ConsultaSaldoRequest.class)))
                .thenReturn(consultaResponse);

          
            mockMvc.perform(get("/api/cuentas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(consultaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value("ARG0170001000000012345000"))
                .andExpect(jsonPath("$.clienteId").value("CLI-12345678"))
                .andExpect(jsonPath("$.saldoActual").value(1500.50));

            verify(consultaSaldoService, times(1))
                .consultarSaldo(any(ConsultaSaldoRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 400 cuando request es inválido")
        void consultarSaldo_RequestInvalido_Retorna400() throws Exception {
          
            ConsultaSaldoRequest requestInvalido = new ConsultaSaldoRequest();
            requestInvalido.setCuentaId(""); // cuentaId vacío

            ConsultaSaldoResponse respuestaError = new ConsultaSaldoResponse(
            "DESCONOCIDA",  
            null,           
            null,           
            null,          
            BigDecimal.ZERO,
            "Error en consulta: Se requiere ID de cuenta");

            when(consultaSaldoService.consultarSaldo(any(ConsultaSaldoRequest.class)))
            .thenReturn(respuestaError);
           
            mockMvc.perform(get("/api/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(containsString("Se requiere ID de cuenta")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/cuentas/{cuentaStringId} - Cerrar cuenta")
    class CerrarCuentaTest {

        @Test
        @DisplayName("Debería cerrar cuenta y retornar 200 OK")
        void cerrarCuenta_CuentaValida_Retorna200() throws Exception {
            
            doNothing().when(aperturaCuentaService).cerrarCuenta("ARG0170001000000012345000");

            
            mockMvc.perform(delete("/api/cuentas/ARG0170001000000012345000"))
                .andExpect(status().isOk());

            verify(aperturaCuentaService, times(1))
                .cerrarCuenta("ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería retornar 400 cuando hay error al cerrar")
        void cerrarCuenta_ErrorAlCerrar_Retorna400() throws Exception {
           
            doThrow(new IllegalArgumentException("Cuenta con saldo no puede cerrarse"))
                .when(aperturaCuentaService).cerrarCuenta("ARG0170001000000012345000");

            
            mockMvc.perform(delete("/api/cuentas/ARG0170001000000012345000"))
                .andExpect(status().isBadRequest());
        }
    }




    @Nested
    @DisplayName("POST /api/cuentas/{cuentaStringId} - Activar cuenta (reabrir)")
    class ActivarCuentaTest {

        @Test
        @DisplayName("Debería activar cuenta y retornar 200 OK")
        void activarCuenta_CuentaValida_Retorna200() throws Exception {
         
            doNothing().when(aperturaCuentaService).abrirCuenta("ARG0170001000000012345000");

          
            mockMvc.perform(post("/api/cuentas/ARG0170001000000012345000"))
                .andExpect(status().isOk());

            verify(aperturaCuentaService, times(1))
                .abrirCuenta("ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería retornar 400 cuando hay error al activar")
        void activarCuenta_ErrorAlActivar_Retorna400() throws Exception {
         
            doThrow(new IllegalArgumentException("Cuenta no encontrada"))
                .when(aperturaCuentaService).abrirCuenta("ARG0170001000000012345000");

           
            mockMvc.perform(post("/api/cuentas/ARG0170001000000012345000"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("DELETE con ID inválido debería retornar 400")
        void cerrarCuenta_IdInvalido_Retorna400() throws Exception {
            
            doThrow(new IllegalArgumentException("Formato de cuenta inválido"))
                .when(aperturaCuentaService).cerrarCuenta("ID-INVALIDO");

           
            mockMvc.perform(delete("/api/cuentas/ID-INVALIDO"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST activar con ID inexistente debería retornar 400")
        void activarCuenta_IdNoExiste_Retorna400() throws Exception {
            
            doThrow(new IllegalArgumentException("Cuenta no encontrada"))
                .when(aperturaCuentaService).abrirCuenta("NO-EXISTE");

           
            mockMvc.perform(post("/api/cuentas/NO-EXISTE"))
                .andExpect(status().isBadRequest());
        }
    }

}
