package com.banco.infrastructure.persistence.Jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.infrastructure.persistence.entities.CuentaEntity;
import com.banco.infrastructure.persistence.jpa.CuentaRepositoryJpa;
import com.banco.infrastructure.persistence.jpa.Interface.CuentaJpaRepository;
import com.banco.infrastructure.persistence.mappers.CuentaMapper;




@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite mocks pre config sin uso
class CuentaRepositoryJpaTest {
    

    @Mock
    private CuentaJpaRepository jpaRepository;

    @Mock
    private CuentaMapper cuentaMapper;

    @InjectMocks
    private CuentaRepositoryJpa repository;

    private CuentaId cuentaId;
    private ClienteId clienteId;
    private Cuenta cuenta;
    private CuentaEntity cuentaEntity;
    private String numeroCuenta;
    private String clienteIdString;
    private UUID uuid;

    @BeforeEach
    void setUp() {
        numeroCuenta = "ARG0170001000000012345000";
        clienteIdString = "CLI-12345678";
        cuentaId = CuentaId.newCuentaId(numeroCuenta);
        clienteId = ClienteId.newCliente(clienteIdString);
        uuid = UUID.randomUUID();

        Dinero saldo = Dinero.nuevo(new BigDecimal("1500.50"), Moneda.ARG);
        cuenta = new Cuenta(cuentaId, clienteId, Moneda.ARG, saldo, true);
        
        cuentaEntity = new CuentaEntity();
        cuentaEntity.setId(uuid);
        cuentaEntity.setNumeroCuenta(numeroCuenta);
        cuentaEntity.setClienteId(clienteIdString);
        cuentaEntity.setMoneda("ARG");
        cuentaEntity.setSaldo(new BigDecimal("1500.50"));
        cuentaEntity.setActiva(true);
    }




    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Debería retornar cuenta cuando existe en BD")
        void buscarPorId_CuentaExistente_RetornaCuenta() {
            
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.of(cuentaEntity));
            when(cuentaMapper.aDominio(cuentaEntity)).thenReturn(cuenta);

            
            Optional<Cuenta> resultado = repository.buscarPorId(cuentaId);

            
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getCuentaId().getValor()).isEqualTo(numeroCuenta);
            assertThat(resultado.get().getSaldo().getMonto()).isEqualByComparingTo("1500.50");

            verify(jpaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
            verify(cuentaMapper, times(1)).aDominio(cuentaEntity);
        }

        @Test
        @DisplayName("Debería retornar empty cuando cuenta no existe")
        void buscarPorId_CuentaNoExiste_RetornaEmpty() {
            
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.empty());

            
            Optional<Cuenta> resultado = repository.buscarPorId(cuentaId);

            
            assertThat(resultado).isEmpty();
            verify(jpaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
            verify(cuentaMapper, never()).aDominio(any());
        }
    }

    @Nested
    @DisplayName("guardar")
    class GuardarTest {

        @Test
        @DisplayName("Debería guardar cuenta nueva")
        void guardar_CuentaNueva_GuardaCorrectamente() {
            
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.empty());
            when(cuentaMapper.aEntity(cuenta, null)).thenReturn(cuentaEntity);

          
            repository.guardar(cuenta);

           
            verify(jpaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
            verify(cuentaMapper, times(1)).aEntity(cuenta, null);
            verify(jpaRepository, times(1)).save(cuentaEntity);
        }

        @Test
        @DisplayName("Debería actualizar cuenta existente")
        void guardar_CuentaExistente_ActualizaCorrectamente() {
            
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.of(cuentaEntity));
            when(cuentaMapper.aEntity(cuenta, cuentaEntity)).thenReturn(cuentaEntity);

            
            repository.guardar(cuenta);

           
            verify(jpaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
            verify(cuentaMapper, times(1)).aEntity(cuenta, cuentaEntity);
            verify(jpaRepository, times(1)).save(cuentaEntity);
        }

        @Test
        @DisplayName("No debería guardar si mapper retorna null")
        void guardar_MapperRetornaNull_NoGuarda() {
           
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.empty());
            when(cuentaMapper.aEntity(cuenta, null)).thenReturn(null);

            
            repository.guardar(cuenta);

           
            verify(jpaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
            verify(cuentaMapper, times(1)).aEntity(cuenta, null);
            verify(jpaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("actualizar (es igual a guardar)")
    class ActualizarTest {

        @Test
        @DisplayName("Debería llamar a guardar internamente")
        void actualizar_CuentaExistente_LlamaAGuardar() {
           
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.of(cuentaEntity));
            when(cuentaMapper.aEntity(cuenta, cuentaEntity)).thenReturn(cuentaEntity);

            
            repository.actualizar(cuenta);

            
            verify(jpaRepository, times(1)).findByNumeroCuenta(numeroCuenta);
            verify(cuentaMapper, times(1)).aEntity(cuenta, cuentaEntity);
            verify(jpaRepository, times(1)).save(cuentaEntity);
        }
    }

    @Nested
    @DisplayName("buscarPorCliente")
    class BuscarPorClienteTest {

        @Test
        @DisplayName("Debería retornar cuentas del cliente")
        void buscarPorCliente_ClienteConCuentas_RetornaLista() {
            
            CuentaEntity cuentaEntity2 = new CuentaEntity();
            cuentaEntity2.setNumeroCuenta("ARG0170001000000012345010");
            cuentaEntity2.setClienteId(clienteIdString);
            
            List<CuentaEntity> entities = Arrays.asList(cuentaEntity, cuentaEntity2);
            
            Cuenta cuenta2 = new Cuenta(
                CuentaId.newCuentaId("ARG0170001000000012345010"),
                clienteId, Moneda.ARG, Dinero.nuevoCero(Moneda.ARG), true
            );

            when(jpaRepository.findByClienteId(clienteIdString)).thenReturn(entities);
            when(cuentaMapper.aDominio(cuentaEntity)).thenReturn(cuenta);
            when(cuentaMapper.aDominio(cuentaEntity2)).thenReturn(cuenta2);

            
            List<Cuenta> resultados = repository.buscarPorCliente(clienteId);

          
            assertThat(resultados).hasSize(2);
            assertThat(resultados).extracting(c -> c.getCuentaId().getValor())
                .containsExactlyInAnyOrder(numeroCuenta, "ARG0170001000000012345010");

            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(cuentaMapper, times(2)).aDominio(any(CuentaEntity.class));
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando cliente no tiene cuentas")
        void buscarPorCliente_ClienteSinCuentas_RetornaListaVacia() {
          
            when(jpaRepository.findByClienteId(clienteIdString)).thenReturn(Arrays.asList());

         
            List<Cuenta> resultados = repository.buscarPorCliente(clienteId);

            
            assertThat(resultados).isEmpty();
            verify(jpaRepository, times(1)).findByClienteId(clienteIdString);
            verify(cuentaMapper, never()).aDominio(any());
        }

        @Test
        @DisplayName("Debería filtrar mapeos nulos")
        void buscarPorCliente_MapperRetornaNull_FiltraNulos() {
            
            CuentaEntity entityInvalida = new CuentaEntity();
            entityInvalida.setNumeroCuenta("INVALIDA");
            
            List<CuentaEntity> entities = Arrays.asList(cuentaEntity, entityInvalida);
            
            when(jpaRepository.findByClienteId(clienteIdString)).thenReturn(entities);
            when(cuentaMapper.aDominio(cuentaEntity)).thenReturn(cuenta);
            when(cuentaMapper.aDominio(entityInvalida)).thenReturn(null);

            
            List<Cuenta> resultados = repository.buscarPorCliente(clienteId);

           
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getCuentaId().getValor()).isEqualTo(numeroCuenta);
        }
    }

    @Nested
    @DisplayName("existeCuentaConNumero")
    class ExisteCuentaConNumeroTest {

        @Test
        @DisplayName("Debería retornar true cuando número existe")
        void existeCuentaConNumero_NumeroExistente_RetornaTrue() {
          
            when(jpaRepository.existsByNumeroCuenta(numeroCuenta)).thenReturn(true);

            
            boolean existe = repository.existeCuentaConNumero(numeroCuenta);

          
            assertThat(existe).isTrue();
            verify(jpaRepository, times(1)).existsByNumeroCuenta(numeroCuenta);
        }

        @Test
        @DisplayName("Debería retornar false cuando número no existe")
        void existeCuentaConNumero_NumeroNoExiste_RetornaFalse() {
            
            when(jpaRepository.existsByNumeroCuenta("NO-EXISTE")).thenReturn(false);

            
            boolean existe = repository.existeCuentaConNumero("NO-EXISTE");

            
            assertThat(existe).isFalse();
            verify(jpaRepository, times(1)).existsByNumeroCuenta("NO-EXISTE");
        }

        @Test
        @DisplayName("Debería manejar número null")
        void existeCuentaConNumero_NumeroNull_RetornaFalse() {
          
            when(jpaRepository.existsByNumeroCuenta(null)).thenReturn(false);

            
            boolean existe = repository.existeCuentaConNumero(null);

            
            assertThat(existe).isFalse();
            verify(jpaRepository, times(1)).existsByNumeroCuenta(null);
        }
    }

    @Nested
    @DisplayName(" Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar excepción del mapper en buscarPorId")
        void buscarPorId_MapperLanzaExcepcion_PropagaExcepcion() {
         
            when(jpaRepository.findByNumeroCuenta(numeroCuenta))
                .thenReturn(Optional.of(cuentaEntity));
            when(cuentaMapper.aDominio(cuentaEntity))
                .thenThrow(new RuntimeException("Error de mapeo"));

           
            assertThatThrownBy(() -> repository.buscarPorId(cuentaId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de mapeo");
        }

        @Test
        @DisplayName("Debería manejar CuentaId nulo en buscarPorId")
        void buscarPorId_CuentaIdNull_RetornaEmpty() {
            
            Optional<Cuenta> resultado = repository.buscarPorId(null);

            
            assertThat(resultado).isEmpty();
            verify(jpaRepository, never()).findByNumeroCuenta(any());
        }

        @Test
        @DisplayName("Debería manejar ClienteId nulo en buscarPorCliente")
        void buscarPorCliente_ClienteIdNull_RetornaListaVacia() {
            
            List<Cuenta> resultados = repository.buscarPorCliente(null);

            
            assertThat(resultados).isEmpty();
            verify(jpaRepository, never()).findByClienteId(any());
        }
    }


}
