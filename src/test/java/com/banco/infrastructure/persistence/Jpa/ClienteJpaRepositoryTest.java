package com.banco.infrastructure.persistence.Jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.banco.infrastructure.persistence.entities.ClienteEntity;
import com.banco.infrastructure.persistence.jpa.Interface.ClienteJpaRepository;





@DataJpaTest // anotacion que activa H2 = (BASE DE DATOS EN MEMORIA) realiza copia del repositorio, config(properties) y tablas
public class ClienteJpaRepositoryTest {
    

    @Autowired // DataJpaTest se conectara autom a el. Utilizara la DB en memoria y no el real.
    private ClienteJpaRepository clienteJpaRepository;

    private ClienteEntity clienteEntity;
    private ClienteEntity clienteEntity2;


        @BeforeEach
    void setUp() {

        // Limpiar la BD antes de cada test
        clienteJpaRepository.deleteAll();

        // Cliente 1 - Activo sin cuentas
        clienteEntity = new ClienteEntity(
            "CLI-12345678",
            "Juan Pérez",
            "juan@email.com"
        );

        

        // Cliente 2 - Con cuentas
        clienteEntity2 = new ClienteEntity(
            "CLI-87654321",
            "María García",
            "maria@email.com"
        );
        clienteEntity2.setCuentasIds(Arrays.asList(
            "ARG0170001000000012345000",
            "ARG0170001000000012345010"
        ));
    }



    @Nested
    @DisplayName("Guardar Cliente")
    class GuardarClienteTest {

        @Test
        @DisplayName("Debería guardar un cliente nuevo correctamente")
        void save_ClienteNuevo_ClienteGuardado() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

            
            assertThat(guardado).isNotNull();
            assertThat(guardado.getId()).isNotNull();
            assertThat(guardado.getClienteId()).isEqualTo("CLI-12345678");
            assertThat(guardado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(guardado.getEmail()).isEqualTo("juan@email.com");
            assertThat(guardado.isActiva()).isTrue();
            assertThat(guardado.getCuentasIds()).isEmpty();
            assertThat(guardado.getMaxCuentasPermitidas()).isEqualTo(5);
        }

        @Test
        @DisplayName("Debería guardar cliente con cuentas asociadas")
        void save_ClienteConCuentas_CuentasGuardadas() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity2);

            
            assertThat(guardado.getCuentasIds()).hasSize(2);
            assertThat(guardado.getCuentasIds()).containsExactlyInAnyOrder(
                "ARG0170001000000012345000",
                "ARG0170001000000012345010"
            );
        }

        @Test
        @DisplayName("No debería permitir duplicados en clienteId (unique)")
        void save_ClienteIdDuplicado_LanzaExcepcion() {
            
            clienteJpaRepository.save(clienteEntity);
            
            ClienteEntity duplicado = new ClienteEntity(
                "CLI-12345678",  // Mismo clienteId
                "Otro Nombre",
                "otro@email.com"
            );

           
            assertThatThrownBy(() -> clienteJpaRepository.save(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("No debería permitir duplicados en email (unique)")
        void save_EmailDuplicado_LanzaExcepcion() {
            // Given
            clienteJpaRepository.save(clienteEntity);
            clienteJpaRepository.flush();  //Flush = "Ejecuta ahora"
            
            ClienteEntity duplicado = new ClienteEntity(
                "CLI-99999999",
                "Otro Nombre",
                "juan@email.com"  // Mismo email
            );

            // When & Then
            assertThatThrownBy(() -> {
                clienteJpaRepository.save(duplicado); 
                clienteJpaRepository.flush();})
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Debería guardar múltiples clientes")
        void save_MultiplesClientes_TodosGuardados() {
            
            clienteJpaRepository.save(clienteEntity);
            clienteJpaRepository.save(clienteEntity2);

            
            List<ClienteEntity> todos = clienteJpaRepository.findAll();
            assertThat(todos).hasSize(2);
        }
    }



    @Nested
    @DisplayName("Buscar Cliente")
    class BuscarClienteTest {

        @Test
        @DisplayName("Debería buscar cliente por clienteId existente")
        void findByClienteId_ClienteExistente_RetornaCliente() {
           
            clienteJpaRepository.save(clienteEntity);

            
            Optional<ClienteEntity> encontrado = clienteJpaRepository
                .findByClienteId("CLI-12345678");

            
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getNombre()).isEqualTo("Juan Pérez");
            assertThat(encontrado.get().getEmail()).isEqualTo("juan@email.com");
        }

        @Test
        @DisplayName("Debería retornar empty cuando clienteId no existe")
        void findByClienteId_ClienteNoExiste_RetornaEmpty() {
           
            Optional<ClienteEntity> encontrado = clienteJpaRepository
                .findByClienteId("CLI-99999999");

            
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería buscar cliente por ID de base de datos")
        void findById_IdExistente_RetornaCliente() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

           
            Optional<ClienteEntity> encontrado = clienteJpaRepository
                .findById(guardado.getId());

        
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getClienteId()).isEqualTo("CLI-12345678");
        }

        @Test
        @DisplayName("Debería retornar empty cuando ID no existe")
        void findById_IdNoExiste_RetornaEmpty() {
            
            Optional<ClienteEntity> encontrado = clienteJpaRepository
                .findById(UUID.randomUUID());

            
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería buscar todos los clientes")
        void findAll_ClientesExistentes_RetornaLista() {
           
            clienteJpaRepository.save(clienteEntity);
            clienteJpaRepository.save(clienteEntity2);

           
            List<ClienteEntity> todos = clienteJpaRepository.findAll();

            
            assertThat(todos).hasSize(2);
            assertThat(todos).extracting(ClienteEntity::getClienteId)
                .containsExactlyInAnyOrder("CLI-12345678", "CLI-87654321");
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay clientes")
        void findAll_SinClientes_RetornaListaVacia() {
            
            List<ClienteEntity> todos = clienteJpaRepository.findAll();

           
            assertThat(todos).isEmpty();
        }
    }



    @Nested
    @DisplayName("Verificación por Email")
    class VerificacionEmailTest {

        @Test
        @DisplayName("Debería verificar existencia por email")
        void existsByEmail_EmailExistente_RetornaTrue() {
           
            clienteJpaRepository.save(clienteEntity);

            
            boolean existe = clienteJpaRepository.existsByEmail("juan@email.com");

            
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Debería retornar false para email no existente")
        void existsByEmail_EmailNoExiste_RetornaFalse() {
           
            boolean existe = clienteJpaRepository.existsByEmail("noexiste@email.com");

            
            assertThat(existe).isFalse();
        }

        @Test
        @DisplayName("existsByEmail debería ser exacto (sensible a mayúsculas)")
        void existsByEmail_EmailConMayusculas_RetornaFalse() {
           
            clienteJpaRepository.save(clienteEntity);

            
            boolean existe = clienteJpaRepository.existsByEmail("JUAN@EMAIL.COM");

            
            assertThat(existe).isFalse();
        }
    }




    @Nested
    @DisplayName(" Actualizar Cliente")
    class ActualizarClienteTest {

        @Test
        @DisplayName("Debería actualizar datos básicos del cliente")
        void save_ClienteExistente_DatosActualizados() {
           
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

            
            guardado.setNombre("Juan Carlos Pérez");
            guardado.setEmail("juancarlos@email.com");
            ClienteEntity actualizado = clienteJpaRepository.save(guardado);

           
            assertThat(actualizado.getNombre()).isEqualTo("Juan Carlos Pérez");
            assertThat(actualizado.getEmail()).isEqualTo("juancarlos@email.com");
            
            // Verificar en BD
            Optional<ClienteEntity> encontrado = clienteJpaRepository.findByClienteId("CLI-12345678");
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getNombre()).isEqualTo("Juan Carlos Pérez");
            assertThat(encontrado.get().getEmail()).isEqualTo("juancarlos@email.com");
        }

        @Test
        @DisplayName("Debería actualizar estado activo/inactivo")
        void save_CambiarEstado_EstadoActualizado() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);
            assertThat(guardado.isActiva()).isTrue();

            
            guardado.setActiva(false);
            ClienteEntity actualizado = clienteJpaRepository.save(guardado);

           
            assertThat(actualizado.isActiva()).isFalse();
            
            Optional<ClienteEntity> encontrado = clienteJpaRepository.findByClienteId("CLI-12345678");
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().isActiva()).isFalse();
        }

        @Test
        @DisplayName("Debería agregar cuentas a cliente existente")
        void save_AgregarCuentas_CuentasActualizadas() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);
            assertThat(guardado.getCuentasIds()).isEmpty();

         
            List<String> cuentas = new ArrayList<>();
            cuentas.add("ARG0170001000000012345000");
            guardado.setCuentasIds(cuentas);

    
            ClienteEntity actualizado = clienteJpaRepository.save(guardado);

            assertThat(actualizado.getCuentasIds()).hasSize(1);
            assertThat(actualizado.getCuentasIds().get(0))
                .isEqualTo("ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería mantener el ID al actualizar")
        void save_MantieneId_AlActualizar() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);
            UUID idOriginal = guardado.getId();

           
            guardado.setNombre("Nuevo Nombre");
            ClienteEntity actualizado = clienteJpaRepository.save(guardado);

           
            assertThat(actualizado.getId()).isEqualTo(idOriginal);
        }

        @Test
        @DisplayName("Debería poder limpiar la lista de cuentas")
        void save_LimpiarCuentas_CuentasVacias() {
           
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity2);
            assertThat(guardado.getCuentasIds()).hasSize(2);

         
            guardado.setCuentasIds(null);
            ClienteEntity actualizado = clienteJpaRepository.save(guardado);

            
            assertThat(actualizado.getCuentasIds()).isNull();
        }
    }




    @Nested
    @DisplayName("Eliminar Cliente")
    class EliminarClienteTest {

        @Test
        @DisplayName("Debería eliminar cliente físicamente")
        void delete_ClienteExistente_ClienteEliminado() {
           
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);
            
            
            clienteJpaRepository.delete(guardado);

            
            Optional<ClienteEntity> encontrado = clienteJpaRepository.findByClienteId("CLI-12345678");
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería eliminar por ID")
        void deleteById_IdExistente_ClienteEliminado() {
            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);
            
           
            clienteJpaRepository.deleteById(guardado.getId());

            
            Optional<ClienteEntity> encontrado = clienteJpaRepository.findById(guardado.getId());
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería eliminar todos los clientes")
        void deleteAll_TodosLosClientes_Eliminados() {
            
            clienteJpaRepository.save(clienteEntity);
            clienteJpaRepository.save(clienteEntity2);
            assertThat(clienteJpaRepository.findAll()).hasSize(2);

            
            clienteJpaRepository.deleteAll();

            
            assertThat(clienteJpaRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName(" Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar lista de cuentas null")
        void save_ListaCuentasNull_GuardaConListaNull() {
            
            clienteEntity.setCuentasIds(null);

            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

           
            assertThat(guardado).isNotNull();
            assertThat(guardado.getCuentasIds()).isNull();
        }

        @Test
        @DisplayName("Debería guardar cliente con máximo de cuentas personalizado")
        void save_MaxCuentasPersonalizado_GuardaValor() {
            
            clienteEntity.setMaxCuentasPermitidas(10);

            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

            
            assertThat(guardado.getMaxCuentasPermitidas()).isEqualTo(10);
        }

        @Test
        @DisplayName("Debería guardar cliente con valores en blanco (si permite)")
        void save_NombreVacio_Guarda() {
            
            clienteEntity.setNombre("");

            
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

           
            assertThat(guardado.getNombre()).isEmpty();
        }

        @Test
        @DisplayName("Debería contar clientes correctamente")
        void count_ClientesExistentes_RetornaCantidad() {
           
            clienteJpaRepository.save(clienteEntity);
            clienteJpaRepository.save(clienteEntity2);

            
            long count = clienteJpaRepository.count();

          
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("existsById debería funcionar correctamente")
        void existsById_IdExistente_RetornaTrue() {
           
            ClienteEntity guardado = clienteJpaRepository.save(clienteEntity);

           
            boolean existe = clienteJpaRepository.existsById(guardado.getId());

            
            assertThat(existe).isTrue();
        }
    }



}
