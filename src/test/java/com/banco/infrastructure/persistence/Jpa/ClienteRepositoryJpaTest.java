package com.banco.infrastructure.persistence.Jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

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

import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.infrastructure.persistence.entities.ClienteEntity;
import com.banco.infrastructure.persistence.jpa.ClienteRepositoryJpa;
import com.banco.infrastructure.persistence.jpa.Interface.ClienteJpaRepository;
import com.banco.infrastructure.persistence.mappers.ClienteMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite mocks pre config sin uso
class ClienteRepositoryJpaTest {
    



    @Mock
    private ClienteJpaRepository jpaRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteRepositoryJpa repository;

    private ClienteId clienteId;
    private Cliente cliente;
    private ClienteEntity clienteEntity;
    private String clienteIdString;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        clienteIdString = "CLI-12345678";
        clienteId = ClienteId.newCliente(clienteIdString);
        uuid = UUID.randomUUID();

        cliente = new Cliente(clienteId, "Juan Pérez", "juan@email.com");
        
        clienteEntity = new ClienteEntity();
        clienteEntity.setId(uuid);
        clienteEntity.setClienteId(clienteIdString);
        clienteEntity.setNombre("Juan Pérez");
        clienteEntity.setEmail("juan@email.com");
        clienteEntity.setActiva(true);
    }




    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Debería retornar cliente cuando existe en BD")
        void buscarPorId_ClienteExistente_RetornaCliente() {
            
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.aDominio(clienteEntity)).thenReturn(cliente);

           
            Cliente resultado = repository.buscarPorId(clienteIdString);

           
            assertThat(resultado).isNotNull();
            assertThat(resultado.getClienteId().getValor()).isEqualTo(clienteIdString);
            assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");

            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(clienteMapper, times(1)).aDominio(clienteEntity);
        }

        @Test
        @DisplayName("Debería retornar null cuando cliente no existe")
        void buscarPorId_ClienteNoExiste_RetornaNull() {
           
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.empty());

            
            Cliente resultado = repository.buscarPorId(clienteIdString);

            
            assertThat(resultado).isNull();
            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(clienteMapper, never()).aDominio(any());
        }

        @Test
        @DisplayName("Debería manejar ID null")
        void buscarPorId_IdNull_RetornaNull() {
            
            Cliente resultado = repository.buscarPorId(null);

            
            assertThat(resultado).isNull();
            verify(clienteMapper, never()).aDominio(any());
            
        }
    }

    @Nested
    @DisplayName(" guardar")
    class GuardarTest {

        @Test
        @DisplayName("Debería guardar cliente nuevo")
        void guardar_ClienteNuevo_GuardaCorrectamente() {
            
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.empty());
            when(clienteMapper.aEntity(cliente, null)).thenReturn(clienteEntity);

            
            repository.guardar(cliente);

            
            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(clienteMapper, times(1)).aEntity(cliente, null);
            verify(jpaRepository, times(1)).save(clienteEntity);
        }

        @Test
        @DisplayName("Debería actualizar cliente existente")
        void guardar_ClienteExistente_ActualizaCorrectamente() {
          
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.aEntity(cliente, clienteEntity)).thenReturn(clienteEntity);

            
            repository.guardar(cliente);

            
            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(clienteMapper, times(1)).aEntity(cliente, clienteEntity);
            verify(jpaRepository, times(1)).save(clienteEntity);
        }

        @Test
        @DisplayName("No debería guardar si mapper retorna null")
        void guardar_MapperRetornaNull_NoGuarda() {
         
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.empty());
            when(clienteMapper.aEntity(cliente, null)).thenReturn(null);

            
            repository.guardar(cliente);

            
            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(clienteMapper, times(1)).aEntity(cliente, null);
            verify(jpaRepository, never()).save(any());
        }
    }




    @Nested
    @DisplayName("actualizar (es igual a guardar)")
    class ActualizarTest {

        @Test
        @DisplayName("Debería llamar a guardar internamente")
        void actualizar_ClienteExistente_LlamaAGuardar() {
            
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.aEntity(cliente, clienteEntity)).thenReturn(clienteEntity);

            
            repository.actualizar(cliente);

            
            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(clienteMapper, times(1)).aEntity(cliente, clienteEntity);
            verify(jpaRepository, times(1)).save(clienteEntity);
        }
    }

    @Nested
    @DisplayName("existePorEmail")
    class ExistePorEmailTest {

        @Test
        @DisplayName("Debería retornar true cuando email existe")
        void existePorEmail_EmailExistente_RetornaTrue() {
           
            when(jpaRepository.existsByEmail("juan@email.com")).thenReturn(true);

            boolean existe = repository.existePorEmail("juan@email.com");

            
            assertThat(existe).isTrue();
            verify(jpaRepository, times(1)).existsByEmail("juan@email.com");
        }

        @Test
        @DisplayName("Debería retornar false cuando email no existe")
        void existePorEmail_EmailNoExiste_RetornaFalse() {
            
            when(jpaRepository.existsByEmail("no@existe.com")).thenReturn(false);

            
            boolean existe = repository.existePorEmail("no@existe.com");

            
            assertThat(existe).isFalse();
            verify(jpaRepository, times(1)).existsByEmail("no@existe.com");
        }

        @Test
        @DisplayName("Debería manejar email null")
        void existePorEmail_EmailNull_RetornaFalse() {
            
            when(jpaRepository.existsByEmail(null)).thenReturn(false);

           
            boolean existe = repository.existePorEmail(null);

            
            assertThat(existe).isFalse();
            verify(jpaRepository, times(1)).existsByEmail(null);
        }
    }

    @Nested
    @DisplayName(" Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar excepción del mapper")
        void buscarPorId_MapperLanzaExcepcion_PropagaExcepcion() {
           
            when(jpaRepository.findByClienteId(clienteIdString))
                .thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.aDominio(clienteEntity))
                .thenThrow(new RuntimeException("Error de mapeo"));

           
            assertThatThrownBy(() -> repository.buscarPorId(clienteIdString))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de mapeo");
        }


    }


}
