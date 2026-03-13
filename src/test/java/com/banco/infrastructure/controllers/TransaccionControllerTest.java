package com.banco.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private OperacionCuentaResponse operacionResponseRetiro;
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

        // operacion de retiro
        operacionResponseRetiro = new OperacionCuentaResponse(
        "TXN-2024-0000002",
        "COMPLETADA",
        new BigDecimal("500.00"),
        "ARG",
        LocalDateTime.now(),
        "ARG0170001000000012345000",
        "RETIRO",
        "Retiro exitoso"
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



    @Nested
    @DisplayName(" POST /api/transacciones/retiro - Retirar")
    class RetirarTest {

        @Test
        @DisplayName("Debería retirar y retornar 200 OK")
        void retirar_DatosValidos_Retorna200() throws Exception {
            
            when(transaccionService.retirar(any(OperacionCuentaRequest.class)))
                .thenReturn(operacionResponseRetiro);

            
            mockMvc.perform(post("/api/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(operacionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaccionId").value("TXN-2024-0000002"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.monto").value(500.00))
                .andExpect(jsonPath("$.tipoDeOperacion").value("RETIRO"))
                .andExpect(jsonPath("$.mensaje").value(containsString("Retiro exitoso")));

            verify(transaccionService, times(1))
                .retirar(any(OperacionCuentaRequest.class));
        }

        @Test
        @DisplayName("Debería retornar 400 cuando el servicio lanza excepción")
        void retirar_ServiceLanzaExcepcion_Retorna400() throws Exception {
            
            when(transaccionService.retirar(any(OperacionCuentaRequest.class)))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente"));

            
            mockMvc.perform(post("/api/transacciones/retiro")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(operacionRequest)))
                .andExpect(status().isBadRequest());
        }
    }



    @Nested
    @DisplayName("POST /api/transacciones/{transaccionId}/revertir - Revertir")
    class RevertirTest {

        @Test
        @DisplayName("Debería revertir y retornar 200 OK")
        void revertir_TransaccionValida_Retorna200() throws Exception {
            
            Dinero monto = Dinero.nuevo(new BigDecimal("1000.00"), Moneda.ARG);
            
            Transaccion transaccionRevertida = new Transaccion(
                new TransaccionId("TXN-2024-0000003"),
                TipoTransaccion.REVERSO,
                CuentaId.newCuentaId("ARG0170001000000012345010"),
                CuentaId.newCuentaId("ARG0170001000000012345000"),
                monto,
                "Reverso de TXN-2024-0000001"
            );
            transaccionRevertida.completar();

            OperacionCuentaResponse responseRevertida = new OperacionCuentaResponse(
                transaccionRevertida.getId().getValor(), 
                transaccionRevertida.getEstado().name(), 
                transaccionRevertida.getMonto().getMonto(), 
                transaccionRevertida.getMonto().getMoneda().getNombre(), 
                transaccionRevertida.getFechaCreacion(), 
                transaccionRevertida.getCuentaOrigen().getValor(), 
                transaccionRevertida.getTipo().name(), 
                transaccionRevertida.getDescripcion());

            when(transaccionService.revertir("TXN-2024-0000001"))
                .thenReturn(responseRevertida);

            
            mockMvc.perform(post("/api/transacciones/TXN-2024-0000001/revertir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaccionId").value("TXN-2024-0000003"))
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.monto").value(1000.00))
                .andExpect(jsonPath("$.tipoDeOperacion").value("REVERSO"))
                .andExpect(jsonPath("$.mensaje").value("Reverso de TXN-2024-0000001"));

            verify(transaccionService, times(1)).revertir("TXN-2024-0000001");
        }

        @Test
        @DisplayName("Debería retornar 400 cuando la transacción no es reversible")
        void revertir_TransaccionNoReversible_Retorna400() throws Exception {
            
            when(transaccionService.revertir("TXN-2024-0000001"))
                .thenThrow(new IllegalArgumentException("Transacción no reversible"));

            
            mockMvc.perform(post("/api/transacciones/TXN-2024-0000001/revertir"))
            .andExpect(status().isBadRequest());
        }
    }




    @Nested
    @DisplayName(" GET /api/transacciones/{cuentaStringId}/movimientos - Obtener movimientos")
    class ObtenerMovimientosTest {

        @Test
        @DisplayName("Debería retornar lista de movimientos")
        void obtenerMovimientos_CuentaValida_Retorna200() throws Exception {
           
            when(transaccionService.consultarMovimiento("ARG0170001000000012345000"))
                .thenReturn(movimientos);

            
            mockMvc.perform(get("/api/transacciones/ARG0170001000000012345000/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("TXN-2024-0000001"))
                .andExpect(jsonPath("$[0].tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$[0].monto").value(1000.00))
                .andExpect(jsonPath("$[1].id").value("TXN-2024-0000002"))
                .andExpect(jsonPath("$[1].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$[1].monto").value(500.00));

            verify(transaccionService, times(1))
                .consultarMovimiento("ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería retornar 400 cuando hay error en la consulta")
        void obtenerMovimientos_ErrorConsulta_Retorna400() throws Exception {
            
            when(transaccionService.consultarMovimiento("ID-INVALIDO"))
                .thenThrow(new IllegalArgumentException("Formato de cuenta inválido"));

            
            mockMvc.perform(get("/api/transacciones/ID-INVALIDO/movimientos"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay movimientos")
        void obtenerMovimientos_SinMovimientos_RetornaListaVacia() throws Exception {
            
            when(transaccionService.consultarMovimiento("ARG0170001000000012345000"))
                .thenReturn(Arrays.asList());

            
            mockMvc.perform(get("/api/transacciones/ARG0170001000000012345000/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }
    }





    @Nested
    @DisplayName("Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("POST /transferir con IDs inválidos debería retornar 400")
        void transferir_IdsInvalidos_Retorna400() throws Exception {
            
            when(transaccionService.ejecutarTransferencia(any(TransferenciaRequest.class)))
                .thenThrow(new IllegalArgumentException("Formato de cuenta inválido"));

            
            mockMvc.perform(post("/api/transacciones/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferenciaRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /deposito con monto cero debería manejarse correctamente")
        void depositar_MontoCero_Retorna400() throws Exception {
            
            OperacionCuentaRequest requestMontoCero = new OperacionCuentaRequest(
                "ARG0170001000000012345000",
                BigDecimal.ZERO,
                "ARG",
                "Depósito con monto cero",
                "REF-003");
            

            when(transaccionService.depositar(requestMontoCero))
                .thenThrow(new IllegalArgumentException("El monto debe ser positivo"));

            
            mockMvc.perform(post("/api/transacciones/deposito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMontoCero)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /retiro con moneda inválida debería retornar 400")
        void retirar_MonedaInvalida_Retorna400() throws Exception {
           
            OperacionCuentaRequest requestMonedaInvalida = new OperacionCuentaRequest(
                "ARG0170001000000012345000",
                new BigDecimal("500.00"),
                "XYZ",
                "Retiro",
                "REF-004");
            

            when(transaccionService.retirar(any(OperacionCuentaRequest.class)))
                .thenThrow(new IllegalArgumentException("Moneda no válida"));

            
            mockMvc.perform(post("/api/transacciones/retiro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMonedaInvalida)))
                .andExpect(status().isBadRequest());
        }
    }



}
