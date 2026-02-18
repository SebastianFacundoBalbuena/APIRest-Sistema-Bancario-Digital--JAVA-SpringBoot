package com.banco.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import com.banco.application.dto.ActualizarClienteRequest;
import com.banco.application.dto.ClienteRequest;
import com.banco.application.dto.ClienteResponse;
import com.banco.application.port.out.ClienteRepository;
import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite mocks sin uso
public class GestionClienteServiceTest {
    


    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private GestionClienteService gestionClienteService;


    private ClienteId clienteId;
    private Cliente cliente;
    private ClienteRequest clienteRequest;
    private CuentaId cuentaId1;
    private CuentaId cuentaId2;
    private List<CuentaId> cuentasCliente;
    private Cliente clienteConDosCuentas;



    @BeforeEach  // se ejecuta antes de cada test
    void setUp() {
        clienteId = ClienteId.newCliente("CLI-12345678");
        cliente = new Cliente(clienteId, "Juan Pérez", "juan@email.com");
        
        clienteRequest = new ClienteRequest("Juan Pérez", "juan@email.com");
        
        cuentaId1 = CuentaId.newCuentaId("ARG0170001000000012345000");
        cuentaId2 = CuentaId.newCuentaId("ARG0170001000000012345010");
        cuentasCliente = List.of(cuentaId1, cuentaId2);

        // cliente con 2 cuentas
        clienteConDosCuentas = new Cliente(
        clienteId, "Juan Pérez", "juan@email.com", true, cuentasCliente);

    }




    @Nested
    @DisplayName(" Creación de Clientes")
    class CreacionClientesTest {



        @Test
        @DisplayName("Debería crear cliente exitosamente")
        void crearCliente_DatosValidos_ClienteCreado() {
            
            when(clienteRepository.existePorEmail(clienteRequest.getEmail())).thenReturn(false);

            
            ClienteResponse response = gestionClienteService.crearCliente(clienteRequest);

            
            assertNotNull(response);
            assertThat(response.getNombre()).isEqualTo("Juan Pérez");
            assertThat(response.getEmail()).isEqualTo("juan@email.com");
            assertThat(response.isActivo()).isTrue();
            assertThat(response.getCantidadCuentas()).isZero();
            assertThat(response.getMaxCuentasPermitidas()).isEqualTo(5);

            verify(clienteRepository, times(1)).existePorEmail(clienteRequest.getEmail());
            verify(clienteRepository, times(1)).guardar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería fallar cuando email ya está registrado")
        void crearCliente_EmailExistente_LanzaExcepcion() {
            
            when(clienteRepository.existePorEmail(clienteRequest.getEmail())).thenReturn(true);

            
            assertThatThrownBy(() -> gestionClienteService.crearCliente(clienteRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email")
            .hasMessageContaining("ya esta registrado");

            verify(clienteRepository, times(1)).existePorEmail(clienteRequest.getEmail());
            verify(clienteRepository, never()).guardar(any(Cliente.class));
        }


    }



    @Nested
    @DisplayName("Búsqueda de Clientes")
    class BusquedaClientesTest {

        @Test
        @DisplayName("Debería buscar cliente por ID existente")
        void buscarClientePorId_ClienteExiste_RetornaCliente() {
           
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(cliente);

            
            ClienteResponse response = gestionClienteService.buscarClientePorId(clienteId.getValor());

            
            assertNotNull(response);
            assertThat(response.getClienteId()).isEqualTo(clienteId.getValor());
            assertThat(response.getNombre()).isEqualTo("Juan Pérez");
            assertThat(response.getEmail()).isEqualTo("juan@email.com");

            verify(clienteRepository, times(2)).buscarPorId(anyString());
        }

        @Test
        @DisplayName("Debería fallar al buscar cliente con ID inexistente")
        void buscarClientePorId_ClienteNoExiste_LanzaExcepcion() {
            
            when(clienteRepository.buscarPorId(anyString())).thenReturn(null);

            
            assertThatThrownBy(() -> gestionClienteService.buscarClientePorId("CLI-99999999"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cliente no encontrado");
        }


    }




    @Nested
    @DisplayName("Actualización de Clientes")
    class ActualizacionClientesTest {

        @Test
        @DisplayName("Debería actualizar cliente exitosamente")
        void actualizarCliente_DatosValidos_ClienteActualizado() {
            
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(cliente);
            when(clienteRepository.existePorEmail("nuevo@email.com")).thenReturn(false);

            ActualizarClienteRequest request = new ActualizarClienteRequest();
            request.setNombre("Juan Carlos Pérez");
            request.setEmail("nuevo@email.com");

            
            ClienteResponse response = gestionClienteService.actualizarCliente(clienteId.getValor(), request);

            
            assertNotNull(response);
            assertThat(response.getNombre()).isEqualTo("Juan Carlos Pérez");
            assertThat(response.getEmail()).isEqualTo("nuevo@email.com");

            verify(clienteRepository, times(1)).guardar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería fallar al actualizar con email ya existente")
        void actualizarCliente_EmailExistente_LanzaExcepcion() {
            
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(cliente);
            when(clienteRepository.existePorEmail("existente@email.com")).thenReturn(true);

            ActualizarClienteRequest request = new ActualizarClienteRequest();
            request.setEmail("existente@email.com");

            
            assertThatThrownBy(() -> gestionClienteService.actualizarCliente(clienteId.getValor(), request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email")
            .hasMessageContaining("ya esta registrado");

            verify(clienteRepository, never()).guardar(any(Cliente.class));
        }


    }



    @Nested
    @DisplayName("Activación/Desactivación de Clientes")
    class EstadoClientesTest {



        @Test
        @DisplayName("Debería desactivar cliente sin cuentas")
        void desactivarCliente_SinCuentas_ClienteDesactivado() {
            
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(cliente);

            
            assertDoesNotThrow(() -> gestionClienteService.descativarCliente(clienteId.getValor()));

            
            verify(clienteRepository, times(1)).guardar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería fallar al desactivar cliente con cuentas")
        void desactivarCliente_ConCuentas_LanzaExcepcion() {
            // Cliente con cuentas
            Cliente clienteConCuentas = new Cliente(
            clienteId, "Juan Pérez", "juan@email.com", true, cuentasCliente);

            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteConCuentas);

            
            assertThatThrownBy(() -> gestionClienteService.descativarCliente(clienteId.getValor()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No se puede desactivar cliente con cuentas activas");

            verify(clienteRepository, never()).guardar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería activar cliente inactivo")
        void activarCliente_ClienteInactivo_ClienteActivado() {
            
            Cliente clienteInactivo = new Cliente(
            clienteId, "Juan Pérez", "juan@email.com", false, List.of());

            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteInactivo);

            
            assertDoesNotThrow(() -> gestionClienteService.activarCliente(clienteId.getValor()));

            
            verify(clienteRepository, times(1)).guardar(any(Cliente.class));
        }


    }



    @Nested
    @DisplayName("Gestión de Cuentas del Cliente")
    class GestionCuentasTest {


        @Test
        @DisplayName("Debería agregar cuenta a cliente")
        void agregarCuentaACliente_ClienteValido_CuentaAgregada() {
            
            CuentaId nuevaCuenta = CuentaId.newCuentaId("ARG0170001000000012345020");

            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteConDosCuentas);

            
            
            assertDoesNotThrow(() -> gestionClienteService.agregarCuentaAcliente(
                clienteId.getValor(), nuevaCuenta.getValor()
            ));

            
            verify(clienteRepository, times(1)).actualizar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería fallar al agregar cuenta cuando cliente tiene 5")
        void agregarCuentaACliente_ClienteCon5Cuentas_LanzaExcepcion() {
            // Cliente con 5 cuentas
            CuentaId c1 = CuentaId.newCuentaId("ARG0170001000000012345000");
            CuentaId c2 = CuentaId.newCuentaId("ARG0170001000000012345010");
            CuentaId c3 = CuentaId.newCuentaId("ARG0170001000000012345020");
            CuentaId c4 = CuentaId.newCuentaId("ARG0170001000000012345030");
            CuentaId c5 = CuentaId.newCuentaId("ARG0170001000000012345040");
            
            Cliente clienteCon5 = new Cliente(
                clienteId, "Juan Pérez", "juan@email.com", true, 
                List.of(c1, c2, c3, c4, c5)
            );
            
            CuentaId nuevaCuenta = CuentaId.newCuentaId("ARG0170001000000012345050");
            
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteCon5);

            
            assertThatThrownBy(() -> gestionClienteService.agregarCuentaAcliente(
                clienteId.getValor(), nuevaCuenta.getValor()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Limite de cuentas alcanzado");

            verify(clienteRepository, never()).actualizar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería fallar al agregar cuenta ya existente")
        void agregarCuentaACliente_CuentaYaAsignada_LanzaExcepcion() {
            
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteConDosCuentas);

            
            assertThatThrownBy(() -> gestionClienteService.agregarCuentaAcliente(
                clienteId.getValor(), cuentaId1.getValor()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La cuenta ya esta asignada");

            verify(clienteRepository, never()).actualizar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería remover cuenta de cliente")
        void removerCuentaACliente_CuentaExistente_CuentaRemovida() {
            
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteConDosCuentas);

            
            assertDoesNotThrow(() -> gestionClienteService.removerCuentaAcliente(
                clienteId.getValor(), cuentaId1.getValor()
            ));

            
            verify(clienteRepository, times(1)).actualizar(any(Cliente.class));
        }

        @Test
        @DisplayName("Debería fallar al remover cuenta no asignada")
        void removerCuentaACliente_CuentaNoAsignada_LanzaExcepcion() {
            // Configurar
            CuentaId cuentaNoAsignada = CuentaId.newCuentaId("ARG0170001000000012345999");
            when(clienteRepository.buscarPorId(clienteId.getValor())).thenReturn(clienteConDosCuentas);

            
            assertThatThrownBy(() -> gestionClienteService.removerCuentaAcliente(
                clienteId.getValor(), cuentaNoAsignada.getValor()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La cuenta no está asignada a este cliente");

            verify(clienteRepository, never()).actualizar(any(Cliente.class));
        }
    }




}
