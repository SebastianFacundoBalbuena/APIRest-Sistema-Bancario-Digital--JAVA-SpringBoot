package com.banco.infrastructure.persistence.Jpa.Interface;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
import com.banco.infrastructure.persistence.entities.TransaccionEntity;
import com.banco.infrastructure.persistence.jpa.Interface.TransaccionJpaRepository;






@DataJpaTest // crea una copia de la BD real en MEMORIA
public class TransaccionJpaRepositoryTest {
    


    @Autowired  // datajpatest lo conecta autom. con la base de datos falsa
    private TransaccionJpaRepository transaccionJpaRepository;

    private TransaccionEntity transferenciaEntity;
    private TransaccionEntity depositoEntity;
    private TransaccionEntity retiroEntity;
    private TransaccionEntity reversoEntity;
    
    private LocalDateTime fechaBase;
    private LocalDateTime fechaAnterior;
    private LocalDateTime fechaPosterior;

    @BeforeEach
    void setUp() {
        // Limpiar BD
        transaccionJpaRepository.deleteAll();
        transaccionJpaRepository.flush();  

        // Fechas para pruebas
        fechaBase = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        fechaAnterior = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        fechaPosterior = LocalDateTime.of(2024, 1, 31, 23, 59, 59);

        // TRANSFERENCIA
        transferenciaEntity = new TransaccionEntity();
        transferenciaEntity.setTransaccionId("TXN-2024-0000001");
        transferenciaEntity.setTipoTransaccion("TRANSFERENCIA");
        transferenciaEntity.setCuentaOrigenId("ARG0170001000000012345000");
        transferenciaEntity.setCuentaDestinoId("ARG0170001000000012345010");
        transferenciaEntity.setMonto(new BigDecimal("1000.50"));
        transferenciaEntity.setMoneda("ARG");
        transferenciaEntity.setDescripcion("Transferencia de prueba");
        transferenciaEntity.setFechaDeCreacion(fechaBase);
        transferenciaEntity.setEstado(EstadoTransaccion.COMPLETADA);
        transferenciaEntity.setReferencia("REF-TRF-001");

        // DEPÓSITO 
        depositoEntity = new TransaccionEntity();
        depositoEntity.setTransaccionId("TXN-2024-0000002");
        depositoEntity.setTipoTransaccion("DEPOSITO");
        depositoEntity.setCuentaOrigenId(null);
        depositoEntity.setCuentaDestinoId("ARG0170001000000012345000");
        depositoEntity.setMonto(new BigDecimal("500.00"));
        depositoEntity.setMoneda("ARG");
        depositoEntity.setDescripcion("Depósito de prueba");
        depositoEntity.setFechaDeCreacion(fechaBase.plusDays(1));
        depositoEntity.setEstado(EstadoTransaccion.COMPLETADA);
        depositoEntity.setReferencia("REF-DEP-001");

        //  RETIRO 
        retiroEntity = new TransaccionEntity();
        retiroEntity.setTransaccionId("TXN-2024-0000003");
        retiroEntity.setTipoTransaccion("RETIRO");
        retiroEntity.setCuentaOrigenId("ARG0170001000000012345000");
        retiroEntity.setCuentaDestinoId(null);
        retiroEntity.setMonto(new BigDecimal("200.00"));
        retiroEntity.setMoneda("ARG");
        retiroEntity.setDescripcion("Retiro de prueba");
        retiroEntity.setFechaDeCreacion(fechaBase.plusDays(2));
        retiroEntity.setEstado(EstadoTransaccion.COMPLETADA);
        retiroEntity.setReferencia("REF-RET-001");

        //  REVERSO 
        reversoEntity = new TransaccionEntity();
        reversoEntity.setTransaccionId("TXN-2024-0000004");
        reversoEntity.setTipoTransaccion("REVERSO");
        reversoEntity.setCuentaOrigenId("ARG0170001000000012345010");
        reversoEntity.setCuentaDestinoId("ARG0170001000000012345000");
        reversoEntity.setMonto(new BigDecimal("1000.50"));
        reversoEntity.setMoneda("ARG");
        reversoEntity.setDescripcion("Reverso de TXN-2024-0000001");
        reversoEntity.setFechaDeCreacion(fechaBase.plusDays(3));
        reversoEntity.setEstado(EstadoTransaccion.COMPLETADA);
        reversoEntity.setReferencia("REF-REV-001");
    }



    @Nested
    @DisplayName("Guardar Transacción")
    class GuardarTransaccionTest {

        @Test
        @DisplayName("Debería guardar transferencia correctamente")
        void save_Transferencia_TransaccionGuardada() {
           
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);

            
            assertThat(guardado).isNotNull();
            assertThat(guardado.getId()).isNotNull();
            assertThat(guardado.getTransaccionId()).isEqualTo("TXN-2024-0000001");
            assertThat(guardado.getTipoTransaccion()).isEqualTo("TRANSFERENCIA");
            assertThat(guardado.getCuentaOrigenId()).isEqualTo("ARG0170001000000012345000");
            assertThat(guardado.getCuentaDestinoId()).isEqualTo("ARG0170001000000012345010");
            assertThat(guardado.getMonto()).isEqualByComparingTo("1000.50");
            assertThat(guardado.getMoneda()).isEqualTo("ARG");
            assertThat(guardado.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
        }

        @Test
        @DisplayName("Debería guardar depósito (sin origen) correctamente")
        void save_Deposito_TransaccionGuardada() {
            
            TransaccionEntity guardado = transaccionJpaRepository.save(depositoEntity);

            
            assertThat(guardado.getTipoTransaccion()).isEqualTo("DEPOSITO");
            assertThat(guardado.getCuentaOrigenId()).isNull();
            assertThat(guardado.getCuentaDestinoId()).isEqualTo("ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería guardar retiro (sin destino) correctamente")
        void save_Retiro_TransaccionGuardada() {
            
            TransaccionEntity guardado = transaccionJpaRepository.save(retiroEntity);

            
            assertThat(guardado.getTipoTransaccion()).isEqualTo("RETIRO");
            assertThat(guardado.getCuentaOrigenId()).isEqualTo("ARG0170001000000012345000");
            assertThat(guardado.getCuentaDestinoId()).isNull();
        }

        @Test
        @DisplayName("Debería guardar reverso correctamente")
        void save_Reverso_TransaccionGuardada() {
            
            TransaccionEntity guardado = transaccionJpaRepository.save(reversoEntity);

            
            assertThat(guardado.getTipoTransaccion()).isEqualTo("REVERSO");
            assertThat(guardado.getReferencia()).isEqualTo("REF-REV-001");
        }

        @Test
        @DisplayName("No debería permitir duplicados en transaccionId (unique)")
        void save_TransaccionIdDuplicado_LanzaExcepcion() {
           
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.flush(); // forzar ejecucion de guardado
            
            TransaccionEntity duplicado = new TransaccionEntity();
            duplicado.setTransaccionId("TXN-2024-0000001"); // Mismo ID
            duplicado.setTipoTransaccion("DEPOSITO");
            duplicado.setCuentaDestinoId("ARG0170001000000012345000");
            duplicado.setMonto(new BigDecimal("100.00"));
            duplicado.setMoneda("ARG");
            duplicado.setFechaDeCreacion(LocalDateTime.now());
            duplicado.setEstado(EstadoTransaccion.PENDIENTE);

           
            assertThatThrownBy(() -> {
                transaccionJpaRepository.save(duplicado);
                transaccionJpaRepository.flush();})
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Debería guardar múltiples transacciones de la misma cuenta")
        void save_MultiplesTransaccionesMismaCuenta_TodasGuardadas() {
         
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(depositoEntity);
            transaccionJpaRepository.save(retiroEntity);

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByCuentaOrigenId("ARG0170001000000012345000");
            assertThat(encontradas).hasSize(2); // transferencia y retiro
        }
    }




    @Nested
    @DisplayName(" Buscar por ID")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Debería buscar transacción por transaccionId existente")
        void findByTransaccionId_IdExistente_RetornaTransaccion() {
            
            transaccionJpaRepository.save(transferenciaEntity);

           
            Optional<TransaccionEntity> encontrado = transaccionJpaRepository
                .findByTransaccionId("TXN-2024-0000001");

            
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getTipoTransaccion()).isEqualTo("TRANSFERENCIA");
            assertThat(encontrado.get().getMonto()).isEqualByComparingTo("1000.50");
        }

        @Test
        @DisplayName("Debería retornar empty cuando transaccionId no existe")
        void findByTransaccionId_IdNoExiste_RetornaEmpty() {
           
            Optional<TransaccionEntity> encontrado = transaccionJpaRepository
                .findByTransaccionId("TXN-9999-9999999");

            
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería buscar por ID de base de datos")
        void findById_IdExistente_RetornaTransaccion() {
           
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);

            
            Optional<TransaccionEntity> encontrado = transaccionJpaRepository
                .findById(guardado.getId());

            
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getTransaccionId()).isEqualTo("TXN-2024-0000001");
        }
    }

    @Nested
    @DisplayName(" Buscar por Cuenta Origen")
    class BuscarPorCuentaOrigenTest {

        @Test
        @DisplayName("Debería buscar transacciones por cuenta origen")
        void findByCuentaOrigenId_CuentaConTransacciones_RetornaLista() {
            
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(retiroEntity);
            transaccionJpaRepository.save(depositoEntity); // No es origen

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByCuentaOrigenId("ARG0170001000000012345000");

            
            assertThat(encontradas).hasSize(2);
            assertThat(encontradas).extracting(TransaccionEntity::getTipoTransaccion)
                .containsExactlyInAnyOrder("TRANSFERENCIA", "RETIRO");
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando cuenta no tiene transacciones como origen")
        void findByCuentaOrigenId_CuentaSinTransacciones_RetornaListaVacia() {
            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByCuentaOrigenId("ARG9999999999999999999999");

           
            assertThat(encontradas).isEmpty();
        }

        @Test
        @DisplayName("findByCuentaOrigenId con null retorna lista vacía")
        void findByCuentaOrigenId_Null_RetornaListaVacia() {
            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByCuentaOrigenId(null);

            
            assertThat(encontradas).isEmpty();
        }
    }



    @Nested
    @DisplayName(" Buscar por Origen o Destino con Fechas")
    class BuscarPorOrigenODestinoConFechasTest {

        @Test
        @DisplayName("Debería buscar transacciones donde cuenta sea origen o destino")
        void findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween_Ambos_RetornaTransacciones() {
            
            transaccionJpaRepository.save(transferenciaEntity); // origen y destino
            transaccionJpaRepository.save(depositoEntity);     // destino
            transaccionJpaRepository.save(retiroEntity);       // origen
            transaccionJpaRepository.save(reversoEntity);      // origen y destino (inverso)
            transaccionJpaRepository.flush();

            String cuentaId = "ARG0170001000000012345000";

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .buscarPorCuentaYFechas(
                    cuentaId,  fechaAnterior, fechaPosterior);

            
            assertThat(encontradas).hasSize(4); // transferencia, deposito, retiro
            assertThat(encontradas).extracting(TransaccionEntity::getTipoTransaccion)
                .containsExactlyInAnyOrder("TRANSFERENCIA", "DEPOSITO", "RETIRO", "REVERSO");
        }

        @Test
        @DisplayName("Debería respetar el filtro de fechas")
        void findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween_ConFechas_FiltraCorrectamente() {
        

            transaccionJpaRepository.save(transferenciaEntity); // fechaBase
            transaccionJpaRepository.save(depositoEntity);     // fechaBase+1
            transaccionJpaRepository.save(retiroEntity);       
            transaccionJpaRepository.flush();     

            LocalDateTime desde = fechaBase.plusDays(1);
            LocalDateTime hasta = fechaBase.plusDays(2);
            String cuentaId = "ARG0170001000000012345000";

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .buscarPorCuentaYFechas(
                    cuentaId,  desde, hasta);

           
            assertThat(encontradas).hasSize(2);
            assertThat(encontradas).extracting(TransaccionEntity::getTipoTransaccion)
                .containsExactlyInAnyOrder("DEPOSITO", "RETIRO");
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando no hay transacciones en el rango")
        void findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween_SinResultados_RetornaListaVacia() {
           
            transaccionJpaRepository.save(transferenciaEntity);

            LocalDateTime desde = LocalDateTime.now().plusDays(1);
            LocalDateTime hasta = LocalDateTime.now().plusDays(2);
            String cuentaId = "ARG0170001000000012345000";

           
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .buscarPorCuentaYFechas(
                    cuentaId,  desde, hasta);

         
            assertThat(encontradas).isEmpty();
        }

        @Test
        @DisplayName("Debería funcionar con fechas exactas")
        void findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween_FechasExactas_Funciona() {
            
            transaccionJpaRepository.save(transferenciaEntity);

            LocalDateTime desde = fechaBase;
            LocalDateTime hasta = fechaBase;
            String cuentaId = "ARG0170001000000012345000";

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .buscarPorCuentaYFechas(
                    cuentaId,  desde, hasta);

           
            assertThat(encontradas).hasSize(1);
        }
    }

    @Nested
    @DisplayName(" Buscar por Referencia")
    class BuscarPorReferenciaTest {

        @Test
        @DisplayName("Debería buscar por referencia exacta")
        void findByReferenciaContainingIgnoreCase_ReferenciaExacta_RetornaTransacciones() {
           
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(depositoEntity);

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByReferenciaContainingIgnoreCase("REF-TRF-001");

            
            assertThat(encontradas).hasSize(1);
            assertThat(encontradas.get(0).getTipoTransaccion()).isEqualTo("TRANSFERENCIA");
        }

        @Test
        @DisplayName("Debería buscar por referencia parcial")
        void findByReferenciaContainingIgnoreCase_ReferenciaParcial_RetornaTransacciones() {
          
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(depositoEntity);
            transaccionJpaRepository.save(retiroEntity);

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByReferenciaContainingIgnoreCase("REF");

            
            assertThat(encontradas).hasSize(3);
        }

        @Test
        @DisplayName("Debería buscar ignorando mayúsculas/minúsculas")
        void findByReferenciaContainingIgnoreCase_IgnoreCase_RetornaTransacciones() {
           
            transaccionJpaRepository.save(transferenciaEntity);

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByReferenciaContainingIgnoreCase("ref-trf-001");

            
            assertThat(encontradas).hasSize(1);
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando referencia no existe")
        void findByReferenciaContainingIgnoreCase_ReferenciaNoExiste_RetornaListaVacia() {
        
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .findByReferenciaContainingIgnoreCase("NO-EXISTE");

            
            assertThat(encontradas).isEmpty();
        }
    }




        @Nested
    @DisplayName(" Actualizar Transacción")
    class ActualizarTransaccionTest {

        @Test
        @DisplayName("Debería actualizar estado de transacción")
        void save_ActualizarEstado_EstadoActualizado() {
            // Given
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);
            assertThat(guardado.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);

            // When
            guardado.setEstado(EstadoTransaccion.REVERTIDA);
            TransaccionEntity actualizado = transaccionJpaRepository.save(guardado);

            // Then
            assertThat(actualizado.getEstado()).isEqualTo(EstadoTransaccion.REVERTIDA);
            
            Optional<TransaccionEntity> encontrado = transaccionJpaRepository
                .findByTransaccionId("TXN-2024-0000001");
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getEstado()).isEqualTo(EstadoTransaccion.REVERTIDA);
        }

        @Test
        @DisplayName("Debería mantener el ID al actualizar")
        void save_MantieneId_AlActualizar() {
            // Given
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);
            UUID idOriginal = guardado.getId();

            // When
            guardado.setDescripcion("Nueva descripción");
            TransaccionEntity actualizado = transaccionJpaRepository.save(guardado);

            // Then
            assertThat(actualizado.getId()).isEqualTo(idOriginal);
        }
    }

    @Nested
    @DisplayName("Eliminar Transacción")
    class EliminarTransaccionTest {

        @Test
        @DisplayName("Debería eliminar transacción físicamente")
        void delete_TransaccionExistente_TransaccionEliminada() {
          
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);
            
            
            transaccionJpaRepository.delete(guardado);

           
            Optional<TransaccionEntity> encontrado = transaccionJpaRepository
                .findByTransaccionId("TXN-2024-0000001");
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería eliminar todas las transacciones")
        void deleteAll_TodasLasTransacciones_Eliminadas() {
          
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(depositoEntity);
            transaccionJpaRepository.save(retiroEntity);
            assertThat(transaccionJpaRepository.findAll()).hasSize(3);

           
            transaccionJpaRepository.deleteAll();

            
            assertThat(transaccionJpaRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Métodos adicionales")
    class MetodosAdicionalesTest {

        @Test
        @DisplayName("count debería retornar cantidad correcta")
        void count_TransaccionesExistentes_RetornaCantidad() {
            
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(depositoEntity);

            
            long count = transaccionJpaRepository.count();

            
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("existsById debería funcionar correctamente")
        void existsById_IdExistente_RetornaTrue() {
        
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);

           
            boolean existe = transaccionJpaRepository.existsById(guardado.getId());

            
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("findAll debería retornar todas las transacciones")
        void findAll_TransaccionesExistentes_RetornaTodas() {
        
            transaccionJpaRepository.save(transferenciaEntity);
            transaccionJpaRepository.save(depositoEntity);
            transaccionJpaRepository.save(retiroEntity);

            
            List<TransaccionEntity> todas = transaccionJpaRepository.findAll();

            
            assertThat(todas).hasSize(3);
            assertThat(todas).extracting(TransaccionEntity::getTipoTransaccion)
                .containsExactlyInAnyOrder("TRANSFERENCIA", "DEPOSITO", "RETIRO");
        }
    }



    @Nested
    @DisplayName(" Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería manejar fechas null en la búsqueda")
        void findByCuentaOrigenIdOrCuentaDestinoIdAndFechaDeCreacionBetween_FechasNull_Comportamiento() {
           
            transaccionJpaRepository.save(transferenciaEntity);
            String cuentaId = "ARG0170001000000012345000";

            
            List<TransaccionEntity> encontradas = transaccionJpaRepository
                .buscarPorCuentaYFechas(
                    cuentaId,  null, null);

           
            assertThat(encontradas).isEmpty();
        }

        @Test
        @DisplayName("Debería guardar transacción con monto cero")
        void save_MontoCero_GuardaCorrectamente() {
            
            transferenciaEntity.setMonto(BigDecimal.ZERO);

            
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);

            
            assertThat(guardado.getMonto()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Debería guardar transacción con descripción muy larga")
        void save_DescripcionLarga_GuardaCorrectamente() {
          
            String descripcionLarga = "A".repeat(200);
            transferenciaEntity.setDescripcion(descripcionLarga);

            
            TransaccionEntity guardado = transaccionJpaRepository.save(transferenciaEntity);

            
            assertThat(guardado.getDescripcion()).hasSize(200);
        }
    }


}
