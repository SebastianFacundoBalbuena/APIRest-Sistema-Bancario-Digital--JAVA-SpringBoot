package com.banco.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.banco.application.dto.MovimientoDTO;
import com.banco.application.dto.OperacionCuentaRequest;
import com.banco.application.dto.OperacionCuentaResponse;
import com.banco.application.dto.TransferenciaRequest;
import com.banco.application.dto.TransferenciaResponse;
import com.banco.application.services.TransaccionService;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;
import com.fasterxml.jackson.databind.ObjectMapper;






@WebMvcTest(TransaccionController.class)
public class TransaccionControllerTest {
    


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransaccionService transaccionService;

    private TransferenciaRequest transferenciaRequest;
    private TransferenciaResponse transferenciaResponse;
    private OperacionCuentaRequest operacionRequest;
    private OperacionCuentaResponse operacionResponse;
    private Transaccion transaccion;
    private List<MovimientoDTO> movimientos;
    private MovimientoDTO movimiento1;
    private MovimientoDTO movimiento2;

    @BeforeEach
    void setUp() {
        // Request de transferencia
        transferenciaRequest = new TransferenciaRequest(
            "ARG0170001000000012345000",
            "ARG0170001000000012345010",
            new BigDecimal("1000.00"),
            "ARG",
            "Transferencia de prueba"
        );

        // Response de transferencia exitosa
        transferenciaResponse = new TransferenciaResponse(
            "TXN-2024-0000001",
            "COMPLETADA",
            new BigDecimal("1000.00"),
            "ARG",
            LocalDateTime.now(),
            "ARG0170001000000012345000",
            "ARG0170001000000012345010",
            "Transaccion realizada exitosamente"
        );

        // Request de operación (depósito/retiro)
        operacionRequest = new OperacionCuentaRequest(
            "ARG0170001000000012345000",
            new BigDecimal("500.00"),
            "ARG",
            "Depósito de prueba",
            "REF-001"
        );
        
        

        // Response de operación
        operacionResponse = new OperacionCuentaResponse(
            "TXN-2024-0000002",
            "COMPLETADA",
            new BigDecimal("500.00"),
            "Peso Argentino",
            LocalDateTime.now(),
            "ARG0170001000000012345000",
            "DEPOSITO",
            "Deposito exitoso"
        );

        // Transacción para depósito/retiro
        Dinero monto = Dinero.nuevo(new BigDecimal("500.00"), Moneda.ARG);
        transaccion = new Transaccion(
            new TransaccionId("TXN-2024-0000002"),
            TipoTransaccion.DEPOSITO,
            null,
            CuentaId.newCuentaId("ARG0170001000000012345000"),
            monto,
            "Depósito de prueba"
        );
        transaccion.completar();

        // Movimientos para consulta
        movimiento1 = new MovimientoDTO(
            "TXN-2024-0000001",
            "TRANSFERENCIA",
            LocalDateTime.now().minusDays(1),
            new BigDecimal("1000.00"),
            "Transferencia recibida",
            "REF-001",
            "ARG0170001000000012345010",
            new BigDecimal("1500.00")
        );

        movimiento2 = new MovimientoDTO(
            "TXN-2024-0000002",
            "DEPOSITO",
            LocalDateTime.now(),
            new BigDecimal("500.00"),
            "Depósito en efectivo",
            "REF-002",
            null,
            new BigDecimal("2000.00")
        );

        movimientos = Arrays.asList(movimiento1, movimiento2);
    }




    @Nested
    @DisplayName("POST /api/transacciones/transferir - Transferir")
    class TransferirTest {


        @Test
        @DisplayName("Debería transferir y retornar 200 OK")
        void transferir_DatosValidos_Retorna200() throws Exception {
            
            when(transaccionService.ejecutarTransferencia(any(TransferenciaRequest.class)))
                .thenReturn(transferenciaResponse);

            
            mockMvc.perform(post("/api/transacciones/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferenciaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaccionId").value("TXN-2024-0000001"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.monto").value(1000.00))
                .andExpect(jsonPath("$.moneda").value("ARG"))
                .andExpect(jsonPath("$.cuentaOrigenId").value("ARG0170001000000012345000"))
                .andExpect(jsonPath("$.cuentaDestinoId").value("ARG0170001000000012345010"))
                .andExpect(jsonPath("$.mensaje").value(containsString("exitosa")));

            verify(transaccionService, times(1))
                .ejecutarTransferencia(any(TransferenciaRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 200 con respuesta de error cuando request es inválido")
        void transferir_RequestInvalido_Retorna200ConError() throws Exception {
           
            TransferenciaRequest requestInvalido = new TransferenciaRequest(
                "", // cuenta origen vacía
                "ARG0170001000000012345010",
                new BigDecimal("-100.00"), // monto negativo
                "ARG",
                "Test"
            );                

          
            mockMvc.perform(post("/api/transacciones/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el servicio lanza excepción")
        void transferir_ServiceLanzaExcepcion_Retorna400() throws Exception {
            
            when(transaccionService.ejecutarTransferencia(any(TransferenciaRequest.class)))
                .thenThrow(new IllegalArgumentException("Fondos insuficientes"));

            
            mockMvc.perform(post("/api/transacciones/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferenciaRequest)))
                .andExpect(status().isBadRequest());
        }

    }



    @Nested
    @DisplayName("POST /api/transacciones/deposito - Depositar")
    class DepositarTest {

        @Test
        @DisplayName("Debería depositar y retornar 200 OK")
        void depositar_DatosValidos_Retorna200() throws Exception {
            
            when(transaccionService.depositar(any(OperacionCuentaRequest.class)))
                .thenReturn(operacionResponse);

            

          
            mockMvc.perform(post("/api/transacciones/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(operacionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaccionId").value("TXN-2024-0000002"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.monto").value(500.00))
                .andExpect(jsonPath("$.tipoDeOperacion").value("DEPOSITO"))
                .andExpect(jsonPath("$.mensaje").value("Deposito exitoso"));

            verify(transaccionService, times(1))
                .depositar(any(OperacionCuentaRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el servicio lanza excepción")
        void depositar_ServiceLanzaExcepcion_Retorna400() throws Exception {
            
            when(transaccionService.depositar(any(OperacionCuentaRequest.class)))
                .thenThrow(new IllegalArgumentException("Cuenta no encontrada"));

            
            mockMvc.perform(post("/api/transacciones/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(operacionRequest)))
                .andExpect(status().isBadRequest());
        }
    }


}
