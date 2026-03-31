package com.banco.infrastructure.persistence.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.infrastructure.persistence.entities.CuentaEntity;





@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite test sin mocks pre configurados
public class CuentaMapperTest {
    

    private CuentaMapper cuentaMapper;


    private CuentaId cuentaId;
    private ClienteId clienteId;
    private Dinero saldo;
    private Moneda moneda;
    
    private Cuenta cuentaActiva;
    private Cuenta cuentaInactiva;
    private Cuenta cuentaSinSaldo;
    
    private CuentaEntity entityActiva;
    private CuentaEntity entityInactiva;
    private CuentaEntity entitySinSaldo;



    @BeforeEach  // se ejecuta antes de cada test
    void setUp() {
        cuentaMapper = new CuentaMapper();

        // IDs
        cuentaId = CuentaId.newCuentaId("ARG0170001000000012345000");
        clienteId = ClienteId.newCliente("CLI-12345678");
        moneda = Moneda.ARG;
        
        // Saldos
        saldo = Dinero.nuevo(new BigDecimal("1500.50"), moneda);
        Dinero saldoCero = Dinero.nuevoCero(moneda);

        // Dominio
        // Cuenta activa con saldo
        cuentaActiva = new Cuenta(cuentaId, clienteId, moneda, saldo, true);
        
        // Cuenta inactiva con saldo
        cuentaInactiva = new Cuenta(cuentaId, clienteId, moneda, saldo, false);
        
        // Cuenta activa sin saldo
        cuentaSinSaldo = new Cuenta(cuentaId, clienteId, moneda, saldoCero, true);

        // Entity
        // Entity activa con saldo
        entityActiva = new CuentaEntity();
        entityActiva.setNumeroCuenta(cuentaId.getValor());
        entityActiva.setClienteId(clienteId.getValor());
        entityActiva.setMoneda(moneda.name());
        entityActiva.setSaldo(new BigDecimal("1500.50"));
        entityActiva.setActiva(true);
        
        // Entity inactiva con saldo
        entityInactiva = new CuentaEntity();
        entityInactiva.setNumeroCuenta(cuentaId.getValor());
        entityInactiva.setClienteId(clienteId.getValor());
        entityInactiva.setMoneda(moneda.name());
        entityInactiva.setSaldo(new BigDecimal("1500.50"));
        entityInactiva.setActiva(false);
        
        // Entity sin saldo
        entitySinSaldo = new CuentaEntity();
        entitySinSaldo.setNumeroCuenta(cuentaId.getValor());
        entitySinSaldo.setClienteId(clienteId.getValor());
        entitySinSaldo.setMoneda(moneda.name());
        entitySinSaldo.setSaldo(BigDecimal.ZERO);
        entitySinSaldo.setActiva(true);
    }




    @Nested
    @DisplayName("Conversión Entity -> Dominio (aDominio)")
    class EntityToDominioTest {



        @Test
        @DisplayName("Debería convertir entity activa a dominio correctamente")
        void aDominio_EntityActiva_DominioActivo() {
            
            Cuenta resultado = cuentaMapper.aDominio(entityActiva);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCuentaId().getValor()).isEqualTo(cuentaId.getValor());
            assertThat(resultado.getClienteId().getValor()).isEqualTo(clienteId.getValor());
            assertThat(resultado.getMoneda()).isEqualTo(Moneda.ARG);
            assertThat(resultado.getSaldo().getMonto()).isEqualByComparingTo("1500.50");
            assertThat(resultado.getActiva()).isTrue();
        }

        @Test
        @DisplayName("Debería convertir entity inactiva a dominio inactivo")
        void aDominio_EntityInactiva_DominioInactivo() {
           
            Cuenta resultado = cuentaMapper.aDominio(entityInactiva);

            
            assertThat(resultado.getActiva()).isFalse();
        }

        @Test
        @DisplayName("Debería convertir entity sin saldo a dominio con saldo cero")
        void aDominio_EntitySinSaldo_DominioSaldoCero() {
           
            Cuenta resultado = cuentaMapper.aDominio(entitySinSaldo);

            
            assertThat(resultado.getSaldo().getMonto()).isEqualByComparingTo("0");
            assertThat(resultado.getSaldo().esCero()).isTrue();
        }

        @Test
        @DisplayName("Debería reconstruir correctamente el Value Object Dinero")
        void aDominio_EntityConSaldo_DineroReconstruido() {
            
            Cuenta resultado = cuentaMapper.aDominio(entityActiva);

           
            Dinero saldoReconstruido = resultado.getSaldo();
            assertThat(saldoReconstruido.getMonto()).isEqualByComparingTo("1500.50");
            assertThat(saldoReconstruido.getMoneda()).isEqualTo(Moneda.ARG);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el código de moneda es inválido")
        void aDominio_MonedaInvalida_LanzaExcepcion() {
            
            entityActiva.setMoneda("XYZ");

            
            assertThatThrownBy(() -> cuentaMapper.aDominio(entityActiva))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de moneda no válido");
        }

    }




    @Nested
    @DisplayName(" Conversión Dominio -> Entity (aEntity)")
    class DominioToEntityTest {

        @Test
        @DisplayName("Debería convertir dominio activo a entity activa (nueva)")
        void aEntity_DominioActivo_SinEntityExistente_EntityNueva() {
          
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaActiva, null);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getNumeroCuenta()).isEqualTo(cuentaId.getValor());
            assertThat(resultado.getClienteId()).isEqualTo(clienteId.getValor());
            assertThat(resultado.getMoneda()).isEqualTo("ARG");
            assertThat(resultado.getSaldo()).isEqualByComparingTo("1500.50");
            assertThat(resultado.getActiva()).isTrue();
        }

        @Test
        @DisplayName("Debería convertir dominio inactivo a entity inactiva")
        void aEntity_DominioInactivo_EntityInactiva() {
            
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaInactiva, null);

            
            assertThat(resultado.getActiva()).isFalse();
        }

        @Test
        @DisplayName("Debería convertir dominio sin saldo a entity con saldo cero")
        void aEntity_DominioSinSaldo_EntitySaldoCero() {
         
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaSinSaldo, null);

           
            assertThat(resultado.getSaldo()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Debería actualizar entity existente en lugar de crear nueva")
        void aEntity_ConEntityExistente_ActualizaExistente() {
            
            CuentaEntity entityExistente = new CuentaEntity();
            UUID idExistente = UUID.randomUUID();
            entityExistente.setId(idExistente);
            entityExistente.setNumeroCuenta("OTRO-NUMERO");

            
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaActiva, entityExistente);

           
            assertThat(resultado).isSameAs(entityExistente); // Misma instancia
            assertThat(resultado.getId()).isEqualTo(idExistente); // Mantiene ID
            assertThat(resultado.getNumeroCuenta()).isEqualTo(cuentaId.getValor()); // Actualizado
            assertThat(resultado.getSaldo()).isEqualByComparingTo("1500.50"); // Actualizado
        }

        @Test
        @DisplayName("Debería mantener el ID de la entity existente")
        void aEntity_ConEntityExistente_MantieneId() {
            
            UUID idExistente = UUID.randomUUID();
            CuentaEntity entityExistente = new CuentaEntity();
            entityExistente.setId(idExistente);

            
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaActiva, entityExistente);

            
            assertThat(resultado.getId()).isEqualTo(idExistente);
        }

        @Test
        @DisplayName("Debería sobrescribir todos los datos de entity existente")
        void aEntity_ConEntityExistente_SobrescribeDatos() {
            
            CuentaEntity entityExistente = new CuentaEntity();
            entityExistente.setNumeroCuenta("OLD-123");
            entityExistente.setClienteId("OLD-CLIENT");
            entityExistente.setMoneda("USD");
            entityExistente.setSaldo(new BigDecimal("999.99"));
            entityExistente.setActiva(false);

            
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaActiva, entityExistente);

            
            assertThat(resultado.getNumeroCuenta()).isEqualTo(cuentaId.getValor());
            assertThat(resultado.getClienteId()).isEqualTo(clienteId.getValor());
            assertThat(resultado.getMoneda()).isEqualTo("ARG");
            assertThat(resultado.getSaldo()).isEqualByComparingTo("1500.50");
            assertThat(resultado.getActiva()).isTrue();
        }

    }



    @Nested
    @DisplayName("Bidireccional - Consistencia")
    class BidirectionalTest {


        @Test
        @DisplayName("Dominio -> Entity -> Dominio debería mantener los datos")
        void dominioToEntityToDominio_MantieneDatos() {
            
            Cuenta dominioOriginal = cuentaActiva;

            
            CuentaEntity entity = cuentaMapper.aEntity(dominioOriginal, null);
            
            // Entity → Dominio
            Cuenta dominioReconstruido = cuentaMapper.aDominio(entity);

            // Verificar todos los campos
            assertThat(dominioReconstruido.getCuentaId().getValor()).isEqualTo(dominioOriginal.getCuentaId().getValor());
            assertThat(dominioReconstruido.getClienteId().getValor()).isEqualTo(dominioOriginal.getClienteId().getValor());
            assertThat(dominioReconstruido.getMoneda()).isEqualTo(dominioOriginal.getMoneda());
            assertThat(dominioReconstruido.getSaldo().getMonto()).isEqualByComparingTo(dominioOriginal.getSaldo().getMonto());
            assertThat(dominioReconstruido.getActiva()).isEqualTo(dominioOriginal.getActiva());
        }

        @Test
        @DisplayName("Entity -> Dominio -> Entity debería mantener los datos")
        void entityToDominioToEntity_MantieneDatos() {
            
            CuentaEntity entityOriginal = entityActiva;

      
            Cuenta dominio = cuentaMapper.aDominio(entityOriginal);
            
            //  Dominio → Entity
            CuentaEntity entityReconstruida = cuentaMapper.aEntity(dominio, null);

            // Verificar todos los campos
            assertThat(entityReconstruida.getNumeroCuenta()).isEqualTo(entityOriginal.getNumeroCuenta());
            assertThat(entityReconstruida.getClienteId()).isEqualTo(entityOriginal.getClienteId());
            assertThat(entityReconstruida.getMoneda()).isEqualTo(entityOriginal.getMoneda());
            assertThat(entityReconstruida.getSaldo()).isEqualByComparingTo(entityOriginal.getSaldo());
            assertThat(entityReconstruida.getActiva()).isEqualTo(entityOriginal.getActiva());
        }
    }



    @Nested
    @DisplayName("Casos Edge y Validaciones")
    class EdgeCasesTest {


        @Test
        @DisplayName("Debería lanzar excepción cuando entity es nula en aDominio")
        void aDominio_EntityNula_LanzaExcepcion() {

            assertThatThrownBy(() -> cuentaMapper.aDominio(null))
            .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando dominio es nulo en aEntity")
        void aEntity_DominioNulo_LanzaExcepcion() {

            assertThatThrownBy(() -> cuentaMapper.aEntity(null, null))
            .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería fallar al crear CuentaId con null")
        void aDominio_EntityConNulos_LanzaExcepcion() {
            
            CuentaEntity entityInvalida = new CuentaEntity();
            
            
            assertThatThrownBy(() -> cuentaMapper.aDominio(entityInvalida))
            .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Debería convertir correctamente con entity existente null (crea nueva)")
        void aEntity_EntityExistenteNull_CreaNueva() {
            
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaActiva, null);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNull(); // Nueva entity sin ID
        }

        @Test
        @DisplayName("Debería manejar correctamente montos con muchos decimales")
        void aDominio_MontoConMuchosDecimales_RedondeoCorrecto() {
            
            entityActiva.setSaldo(new BigDecimal("1500.50678")); // 5 decimales

           
            Cuenta resultado = cuentaMapper.aDominio(entityActiva);

            // Then - Dinero usa escala 10 internamente, pero debería mantener el valor
            assertThat(resultado.getSaldo().getMonto()).isEqualByComparingTo("1500.50678");
        }
    }




    @Nested
    @DisplayName(" Pruebas con diferentes monedas")
    class DiferentesMonedasTest {



        @Test
        @DisplayName("Debería mapear cuenta en USD correctamente")
        void aDominio_CuentaUSD_MapeoCorrecto() {
            
            entityActiva.setMoneda("USD");
            entityActiva.setSaldo(new BigDecimal("1000.00"));

          
            Cuenta resultado = cuentaMapper.aDominio(entityActiva);

         
            assertThat(resultado.getMoneda()).isEqualTo(Moneda.USD);
            assertThat(resultado.getSaldo().getMoneda()).isEqualTo(Moneda.USD);
        }

        @Test
        @DisplayName("Debería mapear cuenta en EUR correctamente")
        void aDominio_CuentaEUR_MapeoCorrecto() {
            
            entityActiva.setMoneda("EUR");
            entityActiva.setSaldo(new BigDecimal("2000.00"));

           
            Cuenta resultado = cuentaMapper.aDominio(entityActiva);

      
            assertThat(resultado.getMoneda()).isEqualTo(Moneda.EUR);
            assertThat(resultado.getSaldo().getMoneda()).isEqualTo(Moneda.EUR);
        }

        @Test
        @DisplayName("Debería convertir dominio USD a entity USD correctamente")
        void aEntity_CuentaUSD_EntityUSD() {
            
            Cuenta cuentaUSD = new Cuenta(cuentaId, clienteId, Moneda.USD, 
                Dinero.nuevo(new BigDecimal("1000.00"), Moneda.USD), true);

        
            CuentaEntity resultado = cuentaMapper.aEntity(cuentaUSD, null);

       
            assertThat(resultado.getMoneda()).isEqualTo("USD");
            assertThat(resultado.getSaldo()).isEqualByComparingTo("1000.00");
        }
    }


}
