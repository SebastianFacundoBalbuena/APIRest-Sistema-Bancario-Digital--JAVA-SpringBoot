package com.banco.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

import com.banco.application.dto.ActualizarClienteRequest;
import com.banco.application.dto.ClienteRequest;
import com.banco.application.dto.ClienteResponse;
import com.banco.application.services.GestionClienteService;
import com.banco.infrastructure.config.TestSecurityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;






@SuppressWarnings("all") // elimina los warings 
@SpringBootTest  //carga todo el contexto como si fuera real (utilizando configuracion real, no mocks)
@AutoConfigureMockMvc // Te inyecta un MockMvc listo para usar y con @SpringBootTest, MockMvc usa los controladores reales
@ActiveProfiles("test") //que use el perfil "test" de TestSecurity
@Import(TestSecurityConfig.class) 
class ClienteControllerTest {


    @Autowired
    private MockMvc mockMvc;  // MockMvc simula peticiones HTTP sin levantar un servidor real.

    @Autowired
    private ObjectMapper objectMapper;  // ObjectMapper es un traductor entre Java y JSON.


    @MockitoBean
    private GestionClienteService gestionClienteService;



    private ClienteResponse clienteResponse;
    private ClienteRequest clienteRequest;
    private ActualizarClienteRequest actualizarRequest;
    private List<String> cuentasIds;

    @BeforeEach
    void setUp() {
        cuentasIds = Arrays.asList(
            "ARG0170001000000012345000",
            "ARG0170001000000012345010"
        );

        clienteResponse = new ClienteResponse(
            "CLI-12345678",
            "Juan Pérez",
            "juan@email.com",
            true,
            2,
            5,
            cuentasIds,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        clienteRequest = new ClienteRequest("Juan Pérez", "juan@email.com");

        actualizarRequest = new ActualizarClienteRequest();
        actualizarRequest.setNombre("Juan Carlos Pérez");
        actualizarRequest.setEmail("juancarlos@email.com");
        actualizarRequest.setActivo(true);
    }



    @Nested
    @DisplayName("Post api/clientes - crear cliente")
    class crearClientesTest{



        @Test
        @DisplayName("deberia crear cliente y retornar 201")
        void crearClienteCorrectamente_deberiaRetornar201() throws Exception{

            when(gestionClienteService.crearCliente(any(ClienteRequest.class)))
            .thenReturn(clienteResponse);

            mockMvc.perform(post("/api/clientes")  // Simulanos una petición POST a la URL /api/clientes
            .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M=")
            .contentType(MediaType.APPLICATION_JSON) // Lo que envío va en formato JSON
            .content(objectMapper.writeValueAsString(actualizarRequest))) // El contenido del POST es este objeto convertido a JSON
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location")) // Verificá que la respuesta tenga un header llamado 'Location'
            .andExpect(header().string("Location", "api/cliente CLI-12345678")) // Verificá que el header 'Location' tenga exactamente este valor
            .andExpect(jsonPath("$.clienteId").value("CLI-12345678")) // en los json el ($.x) debe tener el valor de (x)
            .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
            .andExpect(jsonPath("$.email").value("juan@email.com"))
            .andExpect(jsonPath("$.activo").value("true"))
            .andExpect(jsonPath("$.cantidadCuentas").value("2"));

            verify(gestionClienteService, times(1)).crearCliente(any(ClienteRequest.class));

        }


        @Test
        @DisplayName("deberia retornar 400 cuando el request es invalido")
        void crearCliente_ReuquesInvalido_debeRetornar400() throws Exception{

            ClienteRequest requestInvalido = new ClienteRequest("", "email-invalido");

            mockMvc.perform(post("/api/clientes")
            .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M=")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestInvalido)))
            .andExpect(status().isBadRequest());

            verify(gestionClienteService, never()).crearCliente(any());


        }

    }


    @Nested
    @DisplayName("Get - api/clientes/{id} - obtener cliente")
    class ObtenerClienteTest{


        @Test
        @DisplayName("deberia retornar cliente cuando existe")
        void obtenerCliente_clienteExistente_retorna200() throws Exception{


            when(gestionClienteService.buscarClientePorId("CLI-12345678"))
            .thenReturn(clienteResponse);

            mockMvc.perform(get("/api/clientes/CLI-12345678") // simula peticion get  en url
            .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M=")
            .contentType(MediaType.APPLICATION_JSON) // lo que envio va en formato json
            .content(objectMapper.writeValueAsString(actualizarRequest))) // el contenido debe ser este objeto en json
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clienteId").value("CLI-12345678"))
            .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
            .andExpect(jsonPath("$.email").value("juan@email.com"));

            verify(gestionClienteService,times(1)).buscarClientePorId("CLI-12345678");

            

        }


        @Test
        @DisplayName("deberia retornar 404 cuando el cliente no existe")
        void obtenerCliente_clienteNoExiste_retorna404() throws Exception{


            when(gestionClienteService.buscarClientePorId("CLI-99999999"))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado"));


            mockMvc.perform(get("/api/clientes/CLI-99999999")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNotFound());



        }

    }




    @Nested
    @DisplayName("PUT - actualizar cliente")
    class actualizarClienteTest{



        @Test
        @DisplayName("deberia actualizar cliente y retornar 201 CREATED")
        void actualizarCliente_datosValidos_retorna201() throws Exception{


            ClienteResponse responseActualizado = new ClienteResponse(
                "CLI-12345678",
                "Juan Carlos Pérez",
                "juancarlos@email.com",
                true,
                2,
                5,
                cuentasIds,
                LocalDateTime.now(),
                LocalDateTime.now()
            );


            // eq = "Cuando llamen a buscarPorId con EXACTAMENTE '123', devolvé cliente"

            when(gestionClienteService.actualizarCliente(eq("CLI-12345678"), any(ActualizarClienteRequest.class)))
            .thenReturn(responseActualizado);


            mockMvc.perform(put("/api/clientes/CLI-12345678") // EJECUTAMOS la petición y reviamos valores
            .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M=")
            .contentType(MediaType.APPLICATION_JSON)  // Lo que te mando es JSON
            .content(objectMapper.writeValueAsString(actualizarRequest))) // Este es el JSON convertido
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Juan Carlos Pérez"))
            .andExpect(jsonPath("$.email").value("juancarlos@email.com"));

            verify(gestionClienteService, times(1))
            .actualizarCliente(eq("CLI-12345678"), any(ActualizarClienteRequest.class));

        }



        @Test
        @DisplayName("Debería retornar 400 cuando request es inválido")
        void actualizarCliente_RequestInvalido_Retorna400() throws Exception {
          
            ActualizarClienteRequest requestInvalido = new ActualizarClienteRequest();
            requestInvalido.setEmail("email-invalido"); // Email sin @

           
            mockMvc.perform(put("/api/clientes/CLI-12345678")
                    .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M=")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

            verify(gestionClienteService, never()).actualizarCliente(any(), any());
        }
    }



    @Nested
    @DisplayName("DELETE /api/clientes/{id} - Desactivar cliente")
    class DesactivarClienteTest {

        @Test
        @DisplayName("Debería desactivar cliente y retornar 204 NO CONTENT")
        void desactivarCliente_ClienteExistente_Retorna204() throws Exception {
            

            
            mockMvc.perform(delete("/api/clientes/CLI-12345678")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNoContent());

            verify(gestionClienteService, times(1)).descativarCliente("CLI-12345678");
        }

        @Test
        @DisplayName("Debería retornar 400 si hay error al desactivar")
        void desactivarCliente_ClienteConCuentas_Retorna400() throws Exception {
            
            //doThrow = igual que thenThrow, solo que para metodo que son void
            doThrow(new IllegalArgumentException("No se puede desactivar cliente con cuentas"))
                .when(gestionClienteService).descativarCliente("CLI-12345678");

            
            mockMvc.perform(delete("/api/clientes/CLI-12345678")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isBadRequest());
        }
    }




    @Nested
    @DisplayName("POST /api/clientes/{id} - Activar cliente")
    class ActivarClienteTest {

        @Test
        @DisplayName("Debería activar cliente y retornar 204 NO CONTENT")
        void activarCliente_ClienteExistente_Retorna204() throws Exception {
            
            // doNothing - le decimos al servicio que no ejecute su logica
            doNothing().when(gestionClienteService).activarCliente("CLI-12345678");

           
            mockMvc.perform(post("/api/clientes/CLI-12345678")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNoContent());

            verify(gestionClienteService, times(1)).activarCliente("CLI-12345678");
        }

        @Test
        @DisplayName("Debería retornar 400 si hay error al activar")
        void activarCliente_ClienteNoExiste_Retorna400() throws Exception {
            
            doThrow(new IllegalArgumentException("Cliente no encontrado"))
                .when(gestionClienteService).activarCliente("CLI-99999999");

            
            mockMvc.perform(post("/api/clientes/CLI-99999999")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNotFound());
        }
    }




    @Nested
    @DisplayName("POST /api/clientes/{clienteId}/cuenta/{cuentaId} - Agregar cuenta")
    class AgregarCuentaTest {

        @Test
        @DisplayName("Debería agregar cuenta y retornar 204 NO CONTENT")
        void agregarCuenta_DatosValidos_Retorna204() throws Exception {
            
            // doNothing - le decimos al servicio que no ejecute la logica real
            doNothing().when(gestionClienteService)
            .agregarCuentaAcliente("CLI-12345678", "ARG0170001000000012345000");

            
            mockMvc.perform(post("/api/clientes/CLI-12345678/cuenta/ARG0170001000000012345000")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNoContent());

            verify(gestionClienteService, times(1))
                .agregarCuentaAcliente("CLI-12345678", "ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería retornar 400 si ya tiene 5 cuentas")
        void agregarCuenta_LimiteExcedido_Retorna400() throws Exception {
            
            doThrow(new IllegalArgumentException("Límite de cuentas alcanzado"))
                .when(gestionClienteService)
                .agregarCuentaAcliente("CLI-12345678", "ARG0170001000000012345000");

            
            mockMvc.perform(post("/api/clientes/CLI-12345678/cuenta/ARG0170001000000012345000")
            .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
            .andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName("DELETE /api/clientes/{clienteId}/cuenta/{cuentaId} - Eliminar cuenta")
    class EliminarCuentaTest {

        @Test
        @DisplayName("Debería eliminar cuenta y retornar 204 NO CONTENT")
        void eliminarCuenta_CuentaExistente_Retorna204() throws Exception {
           
            doNothing().when(gestionClienteService)
                .removerCuentaAcliente("CLI-12345678", "ARG0170001000000012345000");

            
            mockMvc.perform(delete("/api/clientes/CLI-12345678/cuenta/ARG0170001000000012345000")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNoContent());

            verify(gestionClienteService, times(1))
                .removerCuentaAcliente("CLI-12345678", "ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería retornar 400 si la cuenta no pertenece al cliente")
        void eliminarCuenta_CuentaNoPertenece_Retorna400() throws Exception {
          
            doThrow(new IllegalArgumentException("La cuenta no pertenece al cliente"))
                .when(gestionClienteService)
                .removerCuentaAcliente("CLI-12345678", "ARG0170001000000012345000");

            
            mockMvc.perform(delete("/api/clientes/CLI-12345678/cuenta/ARG0170001000000012345000")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName(" GET /api/clientes/{id}/cuenta - Obtener cuentas del cliente")
    class ObtenerCuentasTest {

        @Test
        @DisplayName("Debería retornar lista de cuentas del cliente")
        void obtenerCuentas_ClienteExistente_Retorna200() throws Exception {
            
            when(gestionClienteService.buscarClientePorId("CLI-12345678"))
                .thenReturn(clienteResponse);

            
            mockMvc.perform(get("/api/clientes/CLI-12345678/cuenta")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ARG0170001000000012345000"))
                .andExpect(jsonPath("$[1]").value("ARG0170001000000012345010"));

            verify(gestionClienteService, times(1)).buscarClientePorId("CLI-12345678");
        }

        @Test
        @DisplayName("Debería retornar lista vacía si cliente no tiene cuentas")
        void obtenerCuentas_ClienteSinCuentas_RetornaListaVacia() throws Exception {
           
            ClienteResponse clienteSinCuentas = new ClienteResponse(
                "CLI-12345678",
                "Juan Pérez",
                "juan@email.com",
                true,
                0,
                5,
                Arrays.asList(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            when(gestionClienteService.buscarClientePorId("CLI-12345678"))
                .thenReturn(clienteSinCuentas);

            
            mockMvc.perform(get("/api/clientes/CLI-12345678/cuenta")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Debería retornar 400 si cliente no existe")
        void obtenerCuentas_ClienteNoExiste_Retorna400() throws Exception {
            
            when(gestionClienteService.buscarClientePorId("CLI-99999999"))
                .thenThrow(new IllegalArgumentException("Cliente no encontrado"));

            
            mockMvc.perform(get("/api/clientes/CLI-99999999/cuenta")
                .header("Authorization", "Basic dGVzdHVzZXI6dGVzdHBhc3M="))
                .andExpect(status().isNotFound());
        }
    }




    
    
}
