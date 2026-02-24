package com.banco.infrastructure.persistence.mappers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.infrastructure.persistence.entities.ClienteEntity;



@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite test sin mocks pre configurados
public class ClienteMapperTest {
    

    private ClienteMapper clienteMapper;

    private ClienteId clienteId;
    private Cliente clienteActivo;
    private Cliente clienteInactivo;
    private Cliente clienteConCuentas;
    
    private ClienteEntity entityActiva;
    private ClienteEntity entityInactiva;
    private ClienteEntity entityConCuentas;
    
    private CuentaId cuentaId1;
    private CuentaId cuentaId2;
    private List<String> cuentasIdsString;



    @BeforeEach  // se ejecuta antes de cada test
    void setUp() {
        clienteMapper = new ClienteMapper();

        // IDs
        clienteId = ClienteId.newCliente("CLI-12345678");
        cuentaId1 = CuentaId.newCuentaId("ARG0170001000000012345000");
        cuentaId2 = CuentaId.newCuentaId("ARG0170001000000012345010");
        
        // Lista de cuentas en formato String (para Entity)
        cuentasIdsString = Arrays.asList(
            cuentaId1.getValor(),
            cuentaId2.getValor()
        );

        
        // DOMINIO
        // Cliente activo sin cuentas
        clienteActivo = new Cliente(clienteId, "Juan Pérez", "juan@email.com");
        
        // Cliente inactivo sin cuentas
        clienteInactivo = new Cliente(clienteId, "Juan Pérez", "juan@email.com");
        clienteInactivo.desactivar();
        
        // Cliente con cuentas
        clienteConCuentas = new Cliente(clienteId, "Juan Pérez", "juan@email.com");
        clienteConCuentas.agregarCuenta(cuentaId1);
        clienteConCuentas.agregarCuenta(cuentaId2);

        
        
        // Entity
        // Entity activa sin cuentas
        entityActiva = new ClienteEntity(
            clienteId.getValor(),
            "Juan Pérez",
            "juan@email.com"
        );
        
        // Entity inactiva sin cuentas
        entityInactiva = new ClienteEntity(
            clienteId.getValor(),
            "Juan Pérez", 
            "juan@email.com"
        );
        entityInactiva.setActiva(false);
        
        // Entity con cuentas
        entityConCuentas = new ClienteEntity(
            clienteId.getValor(),
            "Juan Pérez",
            "juan@email.com"
        );
        entityConCuentas.setCuentasIds(cuentasIdsString);
    }




    @Nested
    @DisplayName("Conversión Entity → Dominio (aDominio)")
    class EntityToDominioTest {



        @Test
        @DisplayName("Debería convertir entity activa a dominio correctamente")
        void aDominio_EntityActiva_DominioActivo() {
            
            Cliente resultado = clienteMapper.aDominio(entityActiva);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getClienteId().getValor()).isEqualTo("CLI-12345678");
            assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(resultado.getEmail()).isEqualTo("juan@email.com");
            assertThat(resultado.getActiva()).isTrue();
            assertThat(resultado.getCuentas()).isEmpty();

        }

        @Test
        @DisplayName("Debería convertir entity inactiva a dominio inactivo")
        void aDominio_EntityInactiva_DominioInactivo() {
            
            Cliente resultado = clienteMapper.aDominio(entityInactiva);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getActiva()).isFalse();
        }

        @Test
        @DisplayName("Debería convertir entity con cuentas a dominio con cuentas")
        void aDominio_EntityConCuentas_DominioConCuentas() {
            
            Cliente resultado = clienteMapper.aDominio(entityConCuentas);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCuentas()).hasSize(2);
            
            List<String> cuentasResultado = resultado.getCuentas().stream()
                .map(CuentaId::getValor)
                .toList();
            
            assertThat(cuentasResultado).containsExactlyInAnyOrder(
                cuentaId1.getValor(),
                cuentaId2.getValor()
            );
        }

        @Test
        @DisplayName("Debería manejar entity sin cuentas correctamente")
        void aDominio_EntitySinCuentas_DominioSinCuentas() {
            
            Cliente resultado = clienteMapper.aDominio(entityActiva);

            
            assertThat(resultado.getCuentas()).isEmpty();
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando entity tiene cuentasIds inválidos")
        void aDominio_EntityCuentasInvalidas_LanzaExcepcion() {
            
            entityConCuentas.setCuentasIds(Arrays.asList("CUENTA-INVALIDA"));

            
            assertThatThrownBy(() -> clienteMapper.aDominio(entityConCuentas))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El formato no es el correcto");
        }


    }



    @Nested
    @DisplayName("Conversión Dominio → Entity (aEntity)")
    class DominioToEntityTest {



        @Test
        @DisplayName("Debería convertir dominio activo a entity activa (nueva)")
        void aEntity_DominioActivo_SinEntityExistente_EntityNueva() {
            
            ClienteEntity resultado = clienteMapper.aEntity(clienteActivo, null);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getClienteId()).isEqualTo("CLI-12345678");
            assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(resultado.getEmail()).isEqualTo("juan@email.com");
            assertThat(resultado.isActiva()).isTrue();
            assertThat(resultado.getCuentasIds()).isEmpty();
            assertThat(resultado.getMaxCuentasPermitidas()).isEqualTo(5);
        }

        @Test
        @DisplayName("Debería convertir dominio inactivo a entity inactiva")
        void aEntity_DominioInactivo_EntityInactiva() {
            
            ClienteEntity resultado = clienteMapper.aEntity(clienteInactivo, null);

            
            assertThat(resultado.isActiva()).isFalse();
        }

        @Test
        @DisplayName("Debería convertir dominio con cuentas a entity con cuentas")
        void aEntity_DominioConCuentas_EntityConCuentas() {
            
            ClienteEntity resultado = clienteMapper.aEntity(clienteConCuentas, null);

            
            assertThat(resultado.getCuentasIds()).hasSize(2);
            assertThat(resultado.getCuentasIds()).containsExactlyInAnyOrder(
                cuentaId1.getValor(),
                cuentaId2.getValor()
            );
        }

        @Test
        @DisplayName("Debería actualizar entity existente en lugar de crear nueva")
        void aEntity_ConEntityExistente_ActualizaExistente() {
            
            ClienteEntity entityExistente = new ClienteEntity();
            entityExistente.setId(UUID.randomUUID());
            entityExistente.setClienteId("CLI-12345678");

            
            ClienteEntity resultado = clienteMapper.aEntity(clienteConCuentas, entityExistente);

            
            assertThat(resultado).isSameAs(entityExistente); // Misma instancia
            assertThat(resultado.getCuentasIds()).hasSize(2);
        }

        @Test
        @DisplayName("Debería mantener el ID de la entity existente")
        void aEntity_ConEntityExistente_MantieneId() {
            
            UUID idExistente = UUID.randomUUID();
            ClienteEntity entityExistente = new ClienteEntity();
            entityExistente.setId(idExistente);

            
            ClienteEntity resultado = clienteMapper.aEntity(clienteActivo, entityExistente);

            
            assertThat(resultado.getId()).isEqualTo(idExistente);
        }

        @Test
        @DisplayName("Debería sobrescribir datos de entity existente")
        void aEntity_ConEntityExistente_SobrescribeDatos() {
            
            ClienteEntity entityExistente = new ClienteEntity(
                "CLI-OLD", 
                "Nombre Viejo", 
                "viejo@email.com"
            );

            
            ClienteEntity resultado = clienteMapper.aEntity(clienteActivo, entityExistente);

            
            assertThat(resultado.getClienteId()).isEqualTo("CLI-12345678");
            assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(resultado.getEmail()).isEqualTo("juan@email.com");
        }


    }




    @Nested
    @DisplayName("Bidireccional - Consistencia")
    class BidirectionalTest {



        @Test
        @DisplayName("Dominio -> Entity -  Dominio debería mantener los datos")
        void dominioToEntityToDominio_MantieneDatos() {
            
            Cliente dominioOriginal = clienteConCuentas;

            
            ClienteEntity entity = clienteMapper.aEntity(dominioOriginal, null);
            
            // Entity → Dominio
            Cliente dominioReconstruido = clienteMapper.aDominio(entity);

            // Verificar que se mantienen los datos
            assertThat(dominioReconstruido.getClienteId().getValor()).isEqualTo(dominioOriginal.getClienteId().getValor());
            assertThat(dominioReconstruido.getNombre()).isEqualTo(dominioOriginal.getNombre());
            assertThat(dominioReconstruido.getEmail()).isEqualTo(dominioOriginal.getEmail());
            assertThat(dominioReconstruido.getActiva()).isEqualTo(dominioOriginal.getActiva());
            
            // Verificar cuentas
            List<String> cuentasReconstruidas = dominioReconstruido.getCuentas().stream()
                .map(CuentaId::getValor)
                .toList();
            
            List<String> cuentasOriginales = dominioOriginal.getCuentas().stream()
                .map(CuentaId::getValor)
                .toList();
            
            assertThat(cuentasReconstruidas).containsExactlyInAnyOrderElementsOf(cuentasOriginales);
        }

        @Test
        @DisplayName("Entity -> Dominio -> Entity debería mantener los datos")
        void entityToDominioToEntity_MantieneDatos() {
            
            ClienteEntity entityOriginal = entityConCuentas;

            
            Cliente dominio = clienteMapper.aDominio(entityOriginal);
            
            // Dominio → Entity
            ClienteEntity entityReconstruida = clienteMapper.aEntity(dominio, null);

            // Verificar que se mantienen los datos
            assertThat(entityReconstruida.getClienteId()).isEqualTo(entityOriginal.getClienteId());
            assertThat(entityReconstruida.getNombre()).isEqualTo(entityOriginal.getNombre());
            assertThat(entityReconstruida.getEmail()).isEqualTo(entityOriginal.getEmail());
            assertThat(entityReconstruida.isActiva()).isEqualTo(entityOriginal.isActiva());
            
            // Verificar cuentas
            assertThat(entityReconstruida.getCuentasIds())
                .containsExactlyInAnyOrderElementsOf(entityOriginal.getCuentasIds());
        }

    }




    @Nested
    @DisplayName("Casos Edge")
    class EdgeCasesTest {



        @Test
        @DisplayName("Debería manejar entity nula en aDominio lanzando excepción")
        void aDominio_EntityNula_LanzaExcepcion() {

            assertThatThrownBy(() -> clienteMapper.aDominio(null))
            .isInstanceOf(NullPointerException.class);
                
        }

        @Test
        @DisplayName("Debería manejar dominio nulo en aEntity lanzando excepción")
        void aEntity_DominioNulo_LanzaExcepcion() {

            assertThatThrownBy(() -> clienteMapper.aEntity(null, null))
            .isInstanceOf(NullPointerException.class);

        }

        @Test
        @DisplayName("Entity con cuentas null - Debería lanzar exception porque stream() sobre null")
        void aDominio_EntityCuentasNull_ListaVacia() {
            
            entityActiva.setCuentasIds(null);

            
            assertThatThrownBy(() -> clienteMapper.aDominio(entityActiva))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería manejar dominio con lista de cuentas vacía")
        void aEntity_DominioCuentasVacias_EntitySinCuentas() {
            
            ClienteEntity resultado = clienteMapper.aEntity(clienteActivo, null);

            
            assertThat(resultado.getCuentasIds()).isEmpty();
        }
    }


}
