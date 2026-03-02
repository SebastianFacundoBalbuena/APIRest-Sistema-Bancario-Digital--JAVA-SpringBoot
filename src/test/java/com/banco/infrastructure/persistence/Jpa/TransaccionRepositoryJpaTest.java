package com.banco.infrastructure.persistence.Jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;
import com.banco.infrastructure.persistence.entities.TransaccionEntity;
import com.banco.infrastructure.persistence.jpa.TransaccionRepositoryJpa;
import com.banco.infrastructure.persistence.jpa.Interface.TransaccionJpaRepository;
import com.banco.infrastructure.persistence.mappers.TransaccionMapper;






@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite mocks pre confg sin uso
class TransaccionRepositoryJpaTest {
    

        @Mock
    private TransaccionJpaRepository jpaRepository;

    @Mock
    private TransaccionMapper transaccionMapper;

    @InjectMocks
    private TransaccionRepositoryJpa repository;

    private TransaccionId transaccionId;
    private CuentaId cuentaOrigenId;
    private CuentaId cuentaDestinoId;
    private Cuenta cuenta;
    private Transaccion transaccion;
    private TransaccionEntity transaccionEntity;
    private String transaccionIdString;
    private String cuentaIdString;
    private UUID uuid;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;

    @BeforeEach
    void setUp() {
        transaccionIdString = "TXN-2024-0000001";
        cuentaIdString = "ARG0170001000000012345000";
        
        transaccionId = new TransaccionId(transaccionIdString);
        cuentaOrigenId = CuentaId.newCuentaId(cuentaIdString);
        cuentaDestinoId = CuentaId.newCuentaId("ARG0170001000000012345010");
        
        uuid = UUID.randomUUID();
        fechaDesde = LocalDateTime.of(2024, 1, 1, 0, 0);
        fechaHasta = LocalDateTime.of(2024, 1, 31, 23, 59);

        Dinero monto = Dinero.nuevo(new BigDecimal("1000.50"), Moneda.ARG);
        
        transaccion = new Transaccion(
            transaccionId,
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId,
            monto,
            "Transferencia de prueba"
        );

        cuenta = new Cuenta(cuentaOrigenId, 
            com.banco.domain.model.valueobjects.ClienteId.newCliente("CLI-12345678"), 
            Moneda.ARG, monto, true);
        
        transaccionEntity = new TransaccionEntity();
        transaccionEntity.setId(uuid);
        transaccionEntity.setTransaccionId(transaccionIdString);
        transaccionEntity.setTipoTransaccion("TRANSFERENCIA");
        transaccionEntity.setCuentaOrigenId(cuentaIdString);
        transaccionEntity.setCuentaDestinoId("ARG0170001000000012345010");
        transaccionEntity.setMonto(new BigDecimal("1000.50"));
        transaccionEntity.setMoneda("ARG");
        transaccionEntity.setDescripcion("Transferencia de prueba");
        transaccionEntity.setFechaDeCreacion(LocalDateTime.now());
        transaccionEntity.setEstado(EstadoTransaccion.COMPLETADA);
        transaccionEntity.setReferencia("REF-001");
    }



        @Nested
    @DisplayName(" buscarPorId")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Debería retornar transacción cuando existe en BD")
        void buscarPorId_TransaccionExistente_RetornaTransaccion() {
            
            when(jpaRepository.findByTransaccionId(transaccionIdString))
                .thenReturn(Optional.of(transaccionEntity));
            when(transaccionMapper.aDominio(transaccionEntity)).thenReturn(transaccion);

           
            Optional<Transaccion> resultado = repository.buscarPorId(transaccionId);

           
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId().getValor()).isEqualTo(transaccionIdString);
            assertThat(resultado.get().getTipo()).isEqualTo(TipoTransaccion.TRANSFERENCIA);

            verify(jpaRepository, times(1)).findByTransaccionId(transaccionIdString);
            verify(transaccionMapper, times(1)).aDominio(transaccionEntity);
        }

        @Test
        @DisplayName("Debería retornar empty cuando transacción no existe")
        void buscarPorId_TransaccionNoExiste_RetornaEmpty() {
            
            when(jpaRepository.findByTransaccionId(transaccionIdString))
                .thenReturn(Optional.empty());

            
            Optional<Transaccion> resultado = repository.buscarPorId(transaccionId);

            
            assertThat(resultado).isEmpty();
            verify(jpaRepository, times(1)).findByTransaccionId(transaccionIdString);
            verify(transaccionMapper, never()).aDominio(any());
        }
    }

    @Nested
    @DisplayName(" guardar")
    class GuardarTest {

        @Test
        @DisplayName("Debería guardar transacción nueva")
        void guardar_TransaccionNueva_GuardaCorrectamente() {
            
            when(jpaRepository.findByTransaccionId(transaccionIdString))
                .thenReturn(Optional.empty());
            when(transaccionMapper.aEntity(transaccion, null)).thenReturn(transaccionEntity);

           
            repository.guardar(transaccion);

            
            verify(jpaRepository, times(1)).findByTransaccionId(transaccionIdString);
            verify(transaccionMapper, times(1)).aEntity(transaccion, null);
            verify(jpaRepository, times(1)).save(transaccionEntity);
        }

        @Test
        @DisplayName("Debería actualizar transacción existente")
        void guardar_TransaccionExistente_ActualizaCorrectamente() {
           
            when(jpaRepository.findByTransaccionId(transaccionIdString))
                .thenReturn(Optional.of(transaccionEntity));
            when(transaccionMapper.aEntity(transaccion, transaccionEntity)).thenReturn(transaccionEntity);

           
            repository.guardar(transaccion);

            
            verify(jpaRepository, times(1)).findByTransaccionId(transaccionIdString);
            verify(transaccionMapper, times(1)).aEntity(transaccion, transaccionEntity);
            verify(jpaRepository, times(1)).save(transaccionEntity);
        }

        @Test
        @DisplayName("No debería guardar si mapper retorna null")
        void guardar_MapperRetornaNull_NoGuarda() {
            
            when(jpaRepository.findByTransaccionId(transaccionIdString))
                .thenReturn(Optional.empty());
            when(transaccionMapper.aEntity(transaccion, null)).thenReturn(null);

           
            repository.guardar(transaccion);

            
            verify(jpaRepository, times(1)).findByTransaccionId(transaccionIdString);
            verify(transaccionMapper, times(1)).aEntity(transaccion, null);
            verify(jpaRepository, never()).save(any());
        }
    }



        @Nested
    @DisplayName("buscarPorCuenta")
    class BuscarPorCuentaTest {

        @Test
        @DisplayName("Debería buscar transacciones por cuenta en rango de fechas")
        void buscarPorCuenta_CuentaConTransacciones_RetornaLista() {
            
            TransaccionEntity entity2 = new TransaccionEntity();
            entity2.setTransaccionId("TXN-2024-0000002");
            
            List<TransaccionEntity> entities = Arrays.asList(transaccionEntity, entity2);
            
            Transaccion transaccion2 = new Transaccion(
                new TransaccionId("TXN-2024-0000002"),
                TipoTransaccion.DEPOSITO, null, cuentaOrigenId,
                Dinero.nuevo(new BigDecimal("500.00"), Moneda.ARG), "Depósito"
            );

            when(jpaRepository.buscarPorCuentaYFechas(
                cuentaIdString, fechaDesde, fechaHasta))
                .thenReturn(entities);
            when(transaccionMapper.aDominio(transaccionEntity)).thenReturn(transaccion);
            when(transaccionMapper.aDominio(entity2)).thenReturn(transaccion2);

          
            List<Transaccion> resultados = repository.buscarPorCuenta(cuenta, fechaDesde, fechaHasta);

            
            assertThat(resultados).hasSize(2);
            assertThat(resultados).extracting(t -> t.getId().getValor())
                .containsExactlyInAnyOrder("TXN-2024-0000001", "TXN-2024-0000002");

            verify(jpaRepository, times(1))
                .buscarPorCuentaYFechas(
                    cuentaIdString,  fechaDesde, fechaHasta);
            verify(transaccionMapper, times(2)).aDominio(any(TransaccionEntity.class));
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay transacciones")
        void buscarPorCuenta_SinTransacciones_RetornaListaVacia() {
          
            when(jpaRepository.buscarPorCuentaYFechas(
                cuentaIdString, fechaDesde, fechaHasta))
                .thenReturn(Arrays.asList());

           
            List<Transaccion> resultados = repository.buscarPorCuenta(cuenta, fechaDesde, fechaHasta);

          
            assertThat(resultados).isEmpty();
            verify(transaccionMapper, never()).aDominio(any());
        }
    }

    @Nested
    @DisplayName("buscarCuentas (por cuentaId)")
    class BuscarCuentasTest {

        @Test
        @DisplayName("Debería buscar transacciones por cuentaId (origen)")
        void buscarCuentas_CuentaConTransacciones_RetornaLista() {
          
            TransaccionEntity entity2 = new TransaccionEntity();
            entity2.setTransaccionId("TXN-2024-0000002");
            
            List<TransaccionEntity> entities = Arrays.asList(transaccionEntity, entity2);
            
            Transaccion transaccion2 = new Transaccion(
                new TransaccionId("TXN-2024-0000002"),
                TipoTransaccion.RETIRO, cuentaOrigenId, null,
                Dinero.nuevo(new BigDecimal("200.00"), Moneda.ARG), "Retiro"
            );

            when(jpaRepository.findByCuentaOrigenId(cuentaIdString)).thenReturn(entities);
            when(transaccionMapper.aDominio(transaccionEntity)).thenReturn(transaccion);
            when(transaccionMapper.aDominio(entity2)).thenReturn(transaccion2);

            
            List<Transaccion> resultados = repository.buscarCuentas(cuentaOrigenId);

            
            assertThat(resultados).hasSize(2);
            assertThat(resultados).extracting(t -> t.getId().getValor())
                .containsExactlyInAnyOrder("TXN-2024-0000001", "TXN-2024-0000002");

            verify(jpaRepository, times(1)).findByCuentaOrigenId(cuentaIdString);
            verify(transaccionMapper, times(2)).aDominio(any(TransaccionEntity.class));
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay transacciones")
        void buscarCuentas_SinTransacciones_RetornaListaVacia() {
            
            when(jpaRepository.findByCuentaOrigenId(cuentaIdString)).thenReturn(Arrays.asList());

            
            List<Transaccion> resultados = repository.buscarCuentas(cuentaOrigenId);

            
            assertThat(resultados).isEmpty();
            verify(transaccionMapper, never()).aDominio(any());
        }

        @Test
        @DisplayName("Debería filtrar mapeos nulos")
        void buscarCuentas_MapperRetornaNull_FiltraNulos() {
            
            TransaccionEntity entityInvalida = new TransaccionEntity();
            entityInvalida.setTransaccionId("INVALIDA");
            
            List<TransaccionEntity> entities = Arrays.asList(transaccionEntity, entityInvalida);
            
            when(jpaRepository.findByCuentaOrigenId(cuentaIdString)).thenReturn(entities);
            when(transaccionMapper.aDominio(transaccionEntity)).thenReturn(transaccion);
            when(transaccionMapper.aDominio(entityInvalida)).thenReturn(null);

           
            List<Transaccion> resultados = repository.buscarCuentas(cuentaOrigenId);

            
            assertThat(resultados).hasSize(1);
            assertThat(resultados.get(0).getId().getValor()).isEqualTo("TXN-2024-0000001");
        }
    }



    @Nested
    @DisplayName(" buscarPorReferencia")
    class BuscarPorReferenciaTest {

        @Test
        @DisplayName("Debería buscar transacciones por referencia")
        void buscarPorReferencia_ReferenciaExistente_RetornaLista() {
           
            TransaccionEntity entity2 = new TransaccionEntity();
            entity2.setTransaccionId("TXN-2024-0000002");
            entity2.setReferencia("REF-002");
            
            List<TransaccionEntity> entities = Arrays.asList(transaccionEntity, entity2);
            
            Transaccion transaccion2 = new Transaccion(
                new TransaccionId("TXN-2024-0000002"),
                TipoTransaccion.DEPOSITO, null, cuentaOrigenId,
                Dinero.nuevo(new BigDecimal("500.00"), Moneda.ARG), "Depósito"
            );

            when(jpaRepository.findByReferenciaContainingIgnoreCase("REF")).thenReturn(entities);
            when(transaccionMapper.aDominio(transaccionEntity)).thenReturn(transaccion);
            when(transaccionMapper.aDominio(entity2)).thenReturn(transaccion2);

        
            List<Transaccion> resultados = repository.buscarPorReferencia("REF");

            
            assertThat(resultados).hasSize(2);
            verify(jpaRepository, times(1)).findByReferenciaContainingIgnoreCase("REF");
            verify(transaccionMapper, times(2)).aDominio(any(TransaccionEntity.class));
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando referencia no existe")
        void buscarPorReferencia_ReferenciaNoExiste_RetornaListaVacia() {
           
            when(jpaRepository.findByReferenciaContainingIgnoreCase("NO-EXISTE"))
                .thenReturn(Arrays.asList());

            
            List<Transaccion> resultados = repository.buscarPorReferencia("NO-EXISTE");

            
            assertThat(resultados).isEmpty();
            verify(transaccionMapper, never()).aDominio(any());
        }

        @Test
        @DisplayName("Debería manejar referencia null")
        void buscarPorReferencia_ReferenciaNull_RetornaListaVacia() {
         
            when(jpaRepository.findByReferenciaContainingIgnoreCase(null))
                .thenReturn(Arrays.asList());

           
            List<Transaccion> resultados = repository.buscarPorReferencia(null);

            
            assertThat(resultados).isEmpty();
            verify(jpaRepository, times(1)).findByReferenciaContainingIgnoreCase(null);
        }
    }

    @Nested
    @DisplayName( "Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar cuenta null en buscarPorCuenta")
        void buscarPorCuenta_CuentaNull_RetornaListaVacia() {
            
            List<Transaccion> resultados = repository.buscarPorCuenta(null, fechaDesde, fechaHasta);

            
            assertThat(resultados).isEmpty();
            verify(jpaRepository, never())
                .buscarPorCuentaYFechas(any(), any(), any());
        }

        @Test
        @DisplayName("Debería manejar fechas null en buscarPorCuenta")
        void buscarPorCuenta_FechasNull_RetornaListaVacia() {
            
            List<Transaccion> resultados = repository.buscarPorCuenta(cuenta, null, null);

            
            assertThat(resultados).isEmpty();
            verify(jpaRepository, never())
                .buscarPorCuentaYFechas(any(), any(), any());
        }

        @Test
        @DisplayName("Debería manejar excepción del mapper")
        void buscarPorCuenta_MapperLanzaExcepcion_PropagaExcepcion() {
            
            List<TransaccionEntity> entities = Arrays.asList(transaccionEntity);
            
            when(jpaRepository.buscarPorCuentaYFechas(
                cuentaIdString,  fechaDesde, fechaHasta))
                .thenReturn(entities);
            when(transaccionMapper.aDominio(transaccionEntity))
                .thenThrow(new RuntimeException("Error de mapeo"));

          
            assertThatThrownBy(() -> repository.buscarPorCuenta(cuenta, fechaDesde, fechaHasta))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error de mapeo");
        }

        @Test
        @DisplayName("Debería manejar TransaccionId null en buscarPorId")
        void buscarPorId_TransaccionIdNull_RetornaEmpty() {
          
            Optional<Transaccion> resultado = repository.buscarPorId(null);

         
            assertThat(resultado).isEmpty();
            verify(jpaRepository, never()).findByTransaccionId(any());
        }

        @Test
        @DisplayName("Debería manejar CuentaId null en buscarCuentas")
        void buscarCuentas_CuentaIdNull_RetornaListaVacia() {
         
            List<Transaccion> resultados = repository.buscarCuentas(null);

            
            assertThat(resultados).isEmpty();
            verify(jpaRepository, never()).findByCuentaOrigenId(any());
        }
    }



}
