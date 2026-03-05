package com.banco.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

import com.banco.application.dto.ActualizarClienteRequest;
import com.banco.application.dto.ClienteRequest;
import com.banco.application.dto.ClienteResponse;
import com.banco.application.services.GestionClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;







@WebMvcTest(ClienteController.class)
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

            mockMvc.perform(get("/api/clientes/CLI-99999999"))
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

            verify(gestionClienteService, never()).actualizarCliente(any(), any());
        }
    }


    
    
}
