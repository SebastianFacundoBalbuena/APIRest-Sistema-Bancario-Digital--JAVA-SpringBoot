package com.banco.infrastructure.persistence.Jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
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

import com.banco.infrastructure.persistence.entities.CuentaEntity;
import com.banco.infrastructure.persistence.jpa.Interface.CuentaJpaRepository;






@DataJpaTest  // crea una copia de la DATA BASE EN MEMORIA H2 para simular  la rela
public class CuentaJpaRepositoryTest {
    

    @Autowired   // datajpatest lo conectara automaticamente a la DB falsa
    private CuentaJpaRepository cuentaJpaRepository;

    private CuentaEntity cuentaEntity;
    private CuentaEntity cuentaEntity2;
    private CuentaEntity cuentaEntity3;

    @BeforeEach  // se ejeucta antes de cada test
    void setUp() {

        // Limpiar la BD antes de cada test
        cuentaJpaRepository.deleteAll();

        // Cuenta 1 - ARG activa
        cuentaEntity = new CuentaEntity();
        cuentaEntity.setNumeroCuenta("ARG0170001000000012345000");
        cuentaEntity.setClienteId("CLI-12345678");
        cuentaEntity.setMoneda("ARG");
        cuentaEntity.setSaldo(new BigDecimal("1500.50"));
        cuentaEntity.setActiva(true);

        // Cuenta 2 - USD activa
        cuentaEntity2 = new CuentaEntity();
        cuentaEntity2.setNumeroCuenta("ARG0170001000000012345010");
        cuentaEntity2.setClienteId("CLI-12345678");
        cuentaEntity2.setMoneda("USD");
        cuentaEntity2.setSaldo(new BigDecimal("500.00"));
        cuentaEntity2.setActiva(true);

        // Cuenta 3 - ARG inactiva (otro cliente)
        cuentaEntity3 = new CuentaEntity();
        cuentaEntity3.setNumeroCuenta("ARG0170002000000012345000");
        cuentaEntity3.setClienteId("CLI-87654321");
        cuentaEntity3.setMoneda("ARG");
        cuentaEntity3.setSaldo(new BigDecimal("2000.00"));
        cuentaEntity3.setActiva(false);
    }



    @Nested
    @DisplayName("Guardar Cuenta")
    class GuardarCuentaTest {

        @Test
        @DisplayName("Debería guardar una cuenta nueva correctamente")
        void save_CuentaNueva_CuentaGuardada() {
        
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

           
            assertThat(guardado).isNotNull();
            assertThat(guardado.getId()).isNotNull();
            assertThat(guardado.getNumeroCuenta()).isEqualTo("ARG0170001000000012345000");
            assertThat(guardado.getClienteId()).isEqualTo("CLI-12345678");
            assertThat(guardado.getMoneda()).isEqualTo("ARG");
            assertThat(guardado.getSaldo()).isEqualByComparingTo("1500.50");
            assertThat(guardado.getActiva()).isTrue();
        }

        @Test
        @DisplayName("Debería guardar cuenta en USD correctamente")
        void save_CuentaUSD_CuentaGuardada() {
            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity2);

            
            assertThat(guardado.getMoneda()).isEqualTo("USD");
            assertThat(guardado.getSaldo()).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("Debería guardar cuenta inactiva correctamente")
        void save_CuentaInactiva_CuentaGuardada() {
           
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity3);

            
            assertThat(guardado.getActiva()).isFalse();
        }

        @Test
        @DisplayName("No debería permitir duplicados en numeroCuenta (unique)")
        void save_NumeroCuentaDuplicado_LanzaExcepcion() {
            
            cuentaJpaRepository.save(cuentaEntity);
            cuentaJpaRepository.flush(); // forzar ejecucion (en este caso de guardado)
            
            CuentaEntity duplicado = new CuentaEntity();
            duplicado.setNumeroCuenta("ARG0170001000000012345000"); // Mismo número
            duplicado.setClienteId("CLI-99999999");
            duplicado.setMoneda("ARG");
            duplicado.setSaldo(new BigDecimal("100.00"));
            duplicado.setActiva(true);

            
            assertThatThrownBy(() -> {
                cuentaJpaRepository.save(duplicado);
                cuentaJpaRepository.flush();})
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Debería guardar múltiples cuentas del mismo cliente")
        void save_MultiplesCuentasMismoCliente_TodasGuardadas() {
            
            cuentaJpaRepository.save(cuentaEntity);
            cuentaJpaRepository.save(cuentaEntity2);

            
            List<CuentaEntity> cuentas = cuentaJpaRepository.findByClienteId("CLI-12345678");
            assertThat(cuentas).hasSize(2);
        }
    }




    @Nested
    @DisplayName("Buscar Cuenta")
    class BuscarCuentaTest {

        @Test
        @DisplayName("Debería buscar cuenta por número de cuenta existente")
        void findByNumeroCuenta_NumeroExistente_RetornaCuenta() {
           
            cuentaJpaRepository.save(cuentaEntity);

            
            Optional<CuentaEntity> encontrado = cuentaJpaRepository.findByNumeroCuenta("ARG0170001000000012345000");

           
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getClienteId()).isEqualTo("CLI-12345678");
            assertThat(encontrado.get().getSaldo()).isEqualByComparingTo("1500.50");
        }

        @Test
        @DisplayName("Debería retornar empty cuando número de cuenta no existe")
        void findByNumeroCuenta_NumeroNoExiste_RetornaEmpty() {
            
            Optional<CuentaEntity> encontrado = cuentaJpaRepository.findByNumeroCuenta("ARG9999999999999999999999");

            
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería buscar cuenta por ID de base de datos")
        void findById_IdExistente_RetornaCuenta() {
            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

            
            Optional<CuentaEntity> encontrado = cuentaJpaRepository.findById(guardado.getId());

            
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getNumeroCuenta()).isEqualTo("ARG0170001000000012345000");
        }

        @Test
        @DisplayName("Debería buscar cuentas por clienteId")
        void findByClienteId_ClienteExistente_RetornaCuentas() {
           
            cuentaJpaRepository.save(cuentaEntity);
            cuentaJpaRepository.save(cuentaEntity2);
            cuentaJpaRepository.save(cuentaEntity3); // Otro cliente

            
            List<CuentaEntity> cuentas = cuentaJpaRepository.findByClienteId("CLI-12345678");

            
            assertThat(cuentas).hasSize(2);
            assertThat(cuentas).extracting(CuentaEntity::getNumeroCuenta)  // De la clase CuentaEntity, usá su método getNumeroCuenta
                .containsExactlyInAnyOrder(
                    "ARG0170001000000012345000",
                    "ARG0170001000000012345010"
                );
        }

        @Test
        @DisplayName("Debería retornar lista vacía cuando cliente no tiene cuentas")
        void findByClienteId_ClienteSinCuentas_RetornaListaVacia() {
            
            List<CuentaEntity> cuentas = cuentaJpaRepository.findByClienteId("CLI-99999999");

            
            assertThat(cuentas).isEmpty();
        }

        @Test
        @DisplayName("Debería buscar todas las cuentas")
        void findAll_CuentasExistentes_RetornaLista() {
           
            cuentaJpaRepository.save(cuentaEntity);
            cuentaJpaRepository.save(cuentaEntity2);
            cuentaJpaRepository.save(cuentaEntity3);

            
            List<CuentaEntity> todas = cuentaJpaRepository.findAll();

            
            assertThat(todas).hasSize(3);
        }
    }



    @Nested
    @DisplayName("Verificación de Existencia")
    class VerificacionExistenciaTest {

        @Test
        @DisplayName("Debería verificar existencia por número de cuenta")
        void existsByNumeroCuenta_NumeroExistente_RetornaTrue() {
           
            cuentaJpaRepository.save(cuentaEntity);

            
            boolean existe = cuentaJpaRepository.existsByNumeroCuenta("ARG0170001000000012345000");

            
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Debería retornar false para número no existente")
        void existsByNumeroCuenta_NumeroNoExiste_RetornaFalse() {
          
            boolean existe = cuentaJpaRepository.existsByNumeroCuenta("ARG9999999999999999999999");

            
            assertThat(existe).isFalse();
        }

        @Test
        @DisplayName("existsById debería funcionar correctamente")
        void existsById_IdExistente_RetornaTrue() {
            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

            
            boolean existe = cuentaJpaRepository.existsById(guardado.getId());

            
            assertThat(existe).isTrue();
        }
    }

    @Nested
    @DisplayName("Actualizar Cuenta")
    class ActualizarCuentaTest {

        @Test
        @DisplayName("Debería actualizar saldo de cuenta existente")
        void save_ActualizarSaldo_SaldoActualizado() {
            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);
            assertThat(guardado.getSaldo()).isEqualByComparingTo("1500.50");

            
            guardado.setSaldo(new BigDecimal("2000.00"));
            CuentaEntity actualizado = cuentaJpaRepository.save(guardado);

            
            assertThat(actualizado.getSaldo()).isEqualByComparingTo("2000.00");
            
            // Verificar en BD
            Optional<CuentaEntity> encontrado = cuentaJpaRepository
                .findByNumeroCuenta("ARG0170001000000012345000");
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getSaldo()).isEqualByComparingTo("2000.00");
        }

        @Test
        @DisplayName("Debería actualizar estado activo/inactivo")
        void save_CambiarEstado_EstadoActualizado() {
           
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);
            assertThat(guardado.getActiva()).isTrue();

            
            guardado.setActiva(false);
            CuentaEntity actualizado = cuentaJpaRepository.save(guardado);

            
            assertThat(actualizado.getActiva()).isFalse();
        }

        @Test
        @DisplayName("Debería mantener el ID al actualizar")
        void save_MantieneId_AlActualizar() {
            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);
            UUID idOriginal = guardado.getId();

            
            guardado.setSaldo(new BigDecimal("9999.99"));
            CuentaEntity actualizado = cuentaJpaRepository.save(guardado);

            
            assertThat(actualizado.getId()).isEqualTo(idOriginal);
        }

        @Test
        @DisplayName("Debería poder actualizar múltiples campos")
        void save_ActualizarMultiplesCampos_TodosActualizados() {
            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

            
            guardado.setMoneda("USD");
            guardado.setSaldo(new BigDecimal("3000.00"));
            guardado.setActiva(false);
            CuentaEntity actualizado = cuentaJpaRepository.save(guardado);

            
            assertThat(actualizado.getMoneda()).isEqualTo("USD");
            assertThat(actualizado.getSaldo()).isEqualByComparingTo("3000.00");
            assertThat(actualizado.getActiva()).isFalse();
        }
    }




    @Nested
    @DisplayName("Eliminar Cuenta")
    class EliminarCuentaTest {

        @Test
        @DisplayName("Debería eliminar cuenta físicamente")
        void delete_CuentaExistente_CuentaEliminada() {
           
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);
            
            
            cuentaJpaRepository.delete(guardado);

            
            Optional<CuentaEntity> encontrado = cuentaJpaRepository
                .findByNumeroCuenta("ARG0170001000000012345000");
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería eliminar por ID")
        void deleteById_IdExistente_CuentaEliminada() {
           
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);
            
         
            cuentaJpaRepository.deleteById(guardado.getId());

            
            Optional<CuentaEntity> encontrado = cuentaJpaRepository
                .findById(guardado.getId());
            assertThat(encontrado).isEmpty();
        }

        @Test
        @DisplayName("Debería eliminar todas las cuentas")
        void deleteAll_TodasLasCuentas_Eliminadas() {
           
            cuentaJpaRepository.save(cuentaEntity);
            cuentaJpaRepository.save(cuentaEntity2);
            cuentaJpaRepository.save(cuentaEntity3);
            assertThat(cuentaJpaRepository.findAll()).hasSize(3);

           
            cuentaJpaRepository.deleteAll();

            
            assertThat(cuentaJpaRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Casos con Saldos")
    class CasosSaldosTest {

        @Test
        @DisplayName("Debería guardar cuenta con saldo cero")
        void save_SaldoCero_GuardaCorrectamente() {
            
            cuentaEntity.setSaldo(BigDecimal.ZERO);

            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

            
            assertThat(guardado.getSaldo()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Debería guardar cuenta con saldo muy grande")
        void save_SaldoGrande_GuardaCorrectamente() {
           
            BigDecimal saldoGrande = new BigDecimal("999999999.99");
            cuentaEntity.setSaldo(saldoGrande);

            
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

            
            assertThat(guardado.getSaldo()).isEqualByComparingTo(saldoGrande);
        }

        @Test
        @DisplayName("Debería guardar cuenta con saldo negativo (si permite)")
        void save_SaldoNegativo_GuardaCorrectamente() {
           
            cuentaEntity.setSaldo(new BigDecimal("-100.00"));

           
            CuentaEntity guardado = cuentaJpaRepository.save(cuentaEntity);

            
            assertThat(guardado.getSaldo()).isEqualByComparingTo("-100.00");
        }
    }




    @Nested
    @DisplayName("Casos borde")
    class EdgeCasesTest {

        @Test
        @DisplayName("Debería contar cuentas correctamente")
        void count_CuentasExistentes_RetornaCantidad() {
         
            cuentaJpaRepository.save(cuentaEntity);
            cuentaJpaRepository.save(cuentaEntity2);

            
            long count = cuentaJpaRepository.count();

            
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("findByClienteId con clienteId null")
        void findByClienteId_ClienteIdNull_RetornaListaVacia() {
          
            List<CuentaEntity> cuentas = cuentaJpaRepository.findByClienteId(null);

            
            assertThat(cuentas).isEmpty();
        }

        @Test
        @DisplayName("findByNumeroCuenta con null retorna empty")
        void findByNumeroCuenta_NumeroNull_RetornaEmpty() {
            
            Optional<CuentaEntity> encontrado = cuentaJpaRepository.findByNumeroCuenta(null);

            
            assertThat(encontrado).isEmpty();
        }
    }



}
