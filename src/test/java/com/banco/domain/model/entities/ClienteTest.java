package com.banco.domain.model.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;

class ClienteTest {

    
    // propiedades que usaran los test
    private ClienteId clienteId;
    private String nombre;
    private String email;



    @BeforeEach  // PREPARA los datos que TODOS los tests necesitan - Se ejecuta antes de cada TEST.
    void setUp() {
        clienteId = ClienteId.newCliente("CLI-12345678");
        nombre = "Juan Pérez";
        email = "juan.perez@email.com";
    }


    @Test
    @DisplayName("Constructor - Crea cliente activo sin cuentas")
    void constructor_CreaClienteActivoSinCuentas() {
        Cliente cliente = new Cliente(clienteId, nombre, email);
        
        assertThat(cliente.getClienteId()).isEqualTo(clienteId);
        assertThat(cliente.getNombre()).isEqualTo(nombre);
        assertThat(cliente.getEmail()).isEqualTo(email);
        assertThat(cliente.getActiva()).isTrue();
        assertThat(cliente.getCuentas()).isEmpty();  // true esta vacio
        assertThat(cliente.getMaxCuentas()).isEqualTo(5);
    }



    @Test
    @DisplayName("Constructor completo - Con cuentas y estado")
    void constructorCompleto_ConCuentasYEstado() {
        List<CuentaId> cuentas = Arrays.asList(
            CuentaId.newCuentaId("ARG0170000000000000000000"),
            CuentaId.newCuentaId("ARG0170000000000000000001")
        );
        
        Cliente cliente = new Cliente(clienteId, nombre, email, false, cuentas);
        
        assertThat(cliente.getActiva()).isFalse();
        assertThat(cliente.getCuentas()).hasSize(2);
        assertThat(cliente.getCuentas()).containsExactlyElementsOf(cuentas);
    }



    @Test
    @DisplayName("Constructor con email inválido - Debe lanzar excepción")
    void constructor_EmailInvalido_DebeLanzarExcepcion() {
        // Email sin @
        assertThatThrownBy(() -> new Cliente(clienteId, nombre, "emailinvalido.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("debe contener @");
        
        // Email vacío
        assertThatThrownBy(() -> new Cliente(clienteId, nombre, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no puede estar vacio");
        
        // Email null
        assertThatThrownBy(() -> new Cliente(clienteId, nombre, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no puede estar vacio");
    }



    @Nested
    @DisplayName("Gestion de cuenta - eliminar y agregar")
    class GetsionCuentasTest{



        @Test
        @DisplayName("agregarCuenta() cliente activo - Debe agregar cuenta")
        void agregarCuenta_ClienteActivo_DebeAgregarCuenta() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
            
            cliente.agregarCuenta(cuentaId);
            
            assertThat(cliente.getCuentas()).hasSize(1);
            assertThat(cliente.getCuentas()).contains(cuentaId);
            assertThat(cliente.verificarCuenta(cuentaId)).isTrue();
        }



        @Test
        @DisplayName("agregarCuenta() hasta 5 cuentas - Todas deben funcionar")
        void agregarCuenta_Hasta5Cuentas_TodasDebenFuncionar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            
            // Agregar 5 cuentas (límite máximo)
            for (int i = 0; i < 5; i++) {
                CuentaId cuentaId = CuentaId.newCuentaId("ARG017000000000000000000" + i);
                cliente.agregarCuenta(cuentaId);
            }
            
            assertThat(cliente.getCuentas()).hasSize(5);
        }



        @Test
        @DisplayName("agregarCuenta() más de 5 cuentas - Debe lanzar excepción")
        void agregarCuenta_MasDe5Cuentas_DebeLanzarExcepcion() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            
            // Llenar hasta el límite (5 cuentas)
            for (int i = 0; i < 5; i++) {
                CuentaId cuentaId = CuentaId.newCuentaId("ARG017000000000000000000" + i);
                cliente.agregarCuenta(cuentaId);
            }
            
            // Intento de agregar la 6ta cuenta
            CuentaId cuentaExtra = CuentaId.newCuentaId("ARG0170000000000000000005");
            
            assertThatThrownBy(() -> cliente.agregarCuenta(cuentaExtra))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Limite de cuentas excedido");
        }



        @Test
        @DisplayName("agregarCuenta() cuenta duplicada - Debe lanzar excepción")
        void agregarCuenta_CuentaDuplicada_DebeLanzarExcepcion() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
            
            cliente.agregarCuenta(cuentaId); // Primera vez OK
            
            assertThatThrownBy(() -> cliente.agregarCuenta(cuentaId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ya se encuentra asociada");
        }



        @Test
        @DisplayName("agregarCuenta() cliente inactivo - Debe lanzar excepción")
        void agregarCuenta_ClienteInactivo_DebeLanzarExcepcion() {
            Cliente cliente = new Cliente(clienteId, nombre, email, false, new ArrayList<>());
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
            
            assertThatThrownBy(() -> cliente.agregarCuenta(cuentaId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cliente esta inactivo");
        }



        @Test
        @DisplayName("eliminarCuenta() cuenta existente - Debe eliminar")
        void eliminarCuenta_CuentaExistente_DebeEliminar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
            
            cliente.agregarCuenta(cuentaId);
            assertThat(cliente.getCuentas()).hasSize(1);
            
            cliente.eliminarCuenta(cuentaId);
            
            assertThat(cliente.getCuentas()).isEmpty();
            assertThat(cliente.verificarCuenta(cuentaId)).isFalse();
        }



        @Test
        @DisplayName("eliminarCuenta() cuenta no existente - Debe lanzar excepción")
        void eliminarCuenta_CuentaNoExistente_DebeLanzarExcepcion() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
            
            assertThatThrownBy(() -> cliente.eliminarCuenta(cuentaId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no se ecuentra asociada");
        }
        

    }



    @Nested
    @DisplayName("Estado del cliente - activacion y desactivacion")
    class EstadoClienteTest{



        @Test
        @DisplayName("desactivar() cliente activo - Debe cambiar a inactivo")
        void desactivar_ClienteActivo_DebeCambiarAInactivo() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            
            cliente.desactivar();
            
            assertThat(cliente.getActiva()).isFalse();
        }
        
        @Test
        @DisplayName("activar() cliente inactivo - Debe cambiar a activo")
        void activar_ClienteInactivo_DebeCambiarAActivo() {
            Cliente cliente = new Cliente(clienteId, nombre, email, false, new ArrayList<>());
            
            cliente.activar();
            
            assertThat(cliente.getActiva()).isTrue();
        }



        @Test
        @DisplayName("setActiva() - Cambio directo de estado")
        void setActiva_CambioDirectoDeEstado() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            
            cliente.setActiva(false);
            assertThat(cliente.getActiva()).isFalse();
            
            cliente.setActiva(true);
            assertThat(cliente.getActiva()).isTrue();
        }

    }



    @Nested
    @DisplayName("Actualizacion de datos")
    class ActualizacionDatosTest{




        @Test
        @DisplayName("setNombre() - Cambiar nombre del cliente")
        void setNombre_CambiarNombreCliente() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            String nuevoNombre = "Carlos González";
            
            cliente.setNombre(nuevoNombre);
            
            assertThat(cliente.getNombre()).isEqualTo(nuevoNombre);
            assertThat(cliente.getNombre()).isNotEqualTo(nombre);
        }
        
        @Test
        @DisplayName("setEmail() con email válido - Debe actualizar")
        void setEmail_EmailValido_DebeActualizar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            String nuevoEmail = "carlos.gonzalez@empresa.com";
            
            cliente.setEmail(nuevoEmail);
            
            assertThat(cliente.getEmail()).isEqualTo(nuevoEmail);
        }
        
        @Test
        @DisplayName("setEmail() con email inválido - Debe lanzar excepción")
        void setEmail_EmailInvalido_DebeLanzarExcepcion() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            
            assertThatThrownBy(() -> cliente.setEmail("email.invalido.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email invalido");
        }


    }



    @Nested
    @DisplayName("VALIDACION - Transferencias propias")
    class ValidacionTransferenciasTest{



        @Test
        @DisplayName("validarTransferenciaEntrePropiasCuentas() cuentas válidas - Debe aprobar")
        void validarTransferenciaEntrePropiasCuentas_CuentasValidas_DebeAprobar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaOrigenId = CuentaId.newCuentaId("ARG0170000000000000000000");
            CuentaId cuentaDestinoId = CuentaId.newCuentaId("ARG0170000000000000000001");
            
            cliente.agregarCuenta(cuentaOrigenId);
            cliente.agregarCuenta(cuentaDestinoId);
            
            // No debe lanzar excepción
            cliente.validarTransferenciaEntrePropiasCuentas(cuentaOrigenId, cuentaDestinoId);
        }


        @Test
        @DisplayName("validarTransferenciaEntrePropiasCuentas() cuenta origen no propia - Debe rechazar")
        void validarTransferenciaEntrePropiasCuentas_CuentaOrigenNoPropia_DebeRechazar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaOrigenId = CuentaId.newCuentaId("ARG0170000000000000000000"); // No agregada
            CuentaId cuentaDestinoId = CuentaId.newCuentaId("ARG0170000000000000000001");
            
            cliente.agregarCuenta(cuentaDestinoId); // Solo destino agregada
            
            assertThatThrownBy(() -> 
            cliente.validarTransferenciaEntrePropiasCuentas(cuentaOrigenId, cuentaDestinoId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no pertenece al cliente");
        }




        @Test
        @DisplayName("validarTransferenciaEntrePropiasCuentas() cuenta destino no propia - Debe rechazar")
        void validarTransferenciaEntrePropiasCuentas_CuentaDestinoNoPropia_DebeRechazar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaOrigenId = CuentaId.newCuentaId("ARG0170000000000000000000");
            CuentaId cuentaDestinoId = CuentaId.newCuentaId("ARG0170000000000000000001"); // No agregada
            
            cliente.agregarCuenta(cuentaOrigenId); // Solo origen agregada
            
            assertThatThrownBy(() -> 
            cliente.validarTransferenciaEntrePropiasCuentas(cuentaOrigenId, cuentaDestinoId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no pertenece al cliente");
        }


        @Test
        @DisplayName("validarTransferenciaEntrePropiasCuentas() misma cuenta - Debe rechazar")
        void validarTransferenciaEntrePropiasCuentas_MismaCuenta_DebeRechazar() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId mismaCuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
            
            cliente.agregarCuenta(mismaCuentaId);
            
            assertThatThrownBy(() -> cliente.validarTransferenciaEntrePropiasCuentas(mismaCuentaId, mismaCuentaId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("misma cuenta");
        }



        @Test
        @DisplayName("validarTransferenciaEntrePropiasCuentas() cliente inactivo - Debe rechazar")
        void validarTransferenciaEntrePropiasCuentas_ClienteInactivo_DebeRechazar() {
            Cliente cliente = new Cliente(clienteId, nombre, email, false, new ArrayList<>());
            CuentaId cuentaOrigenId = CuentaId.newCuentaId("ARG0170000000000000000000");
            CuentaId cuentaDestinoId = CuentaId.newCuentaId("ARG0170000000000000000001");
            
            assertThatThrownBy(() -> cliente.validarTransferenciaEntrePropiasCuentas(cuentaOrigenId, cuentaDestinoId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cliente esta inactivo");
        }


        @Test
        @DisplayName("getCuentas() - Debe retornar copia defensiva (no la lista original)")
        void getCuentas_DebeRetornarCopiaDefensiva() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
        
            cliente.agregarCuenta(cuentaId);
        
            List<CuentaId> cuentasObtenidas = cliente.getCuentas();
        
            // Modificar la lista obtenida NO debe afectar al cliente
            cuentasObtenidas.clear();
        
            // El cliente aún debe tener su cuenta
            assertThat(cliente.getCuentas()).hasSize(1);
            assertThat(cliente.verificarCuenta(cuentaId)).isTrue();
        }
    
        @Test
        @DisplayName("verificarCuenta() cuenta existente - Debe devolver true")
        void verificarCuenta_CuentaExistente_DebeDevolverTrue() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
        
            cliente.agregarCuenta(cuentaId);
        
            assertThat(cliente.verificarCuenta(cuentaId)).isTrue();
        }



        @Test
        @DisplayName("verificarCuenta() cuenta no existente - Debe devolver false")
        void verificarCuenta_CuentaNoExistente_DebeDevolverFalse() {
            Cliente cliente = new Cliente(clienteId, nombre, email);
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0170000000000000000000");
        
            assertThat(cliente.verificarCuenta(cuentaId)).isFalse();
        }

    

    }


    

}
