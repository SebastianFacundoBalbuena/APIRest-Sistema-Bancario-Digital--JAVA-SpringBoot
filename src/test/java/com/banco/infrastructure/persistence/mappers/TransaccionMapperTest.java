package com.banco.infrastructure.persistence.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.EstadoTransaccion;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;
import com.banco.infrastructure.persistence.entities.TransaccionEntity;





@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // permite test sin mocks pre configurados
public class TransaccionMapperTest {
    

    private TransaccionMapper transaccionMapper;

    // Datos de prueba
    private TransaccionId transaccionId;
    private CuentaId cuentaOrigenId;
    private CuentaId cuentaDestinoId;
    private Dinero monto;
    private LocalDateTime fechaFija;
    private String referencia;
    private String descripcion;

    // Transacciones de dominio para cada tipo
    private Transaccion transferenciaCompletada;
    private Transaccion depositoCompletado;
    private Transaccion retiroCompletado;

    // Entities correspondientes
    private TransaccionEntity entityTransferencia;
    private TransaccionEntity entityDeposito;
    private TransaccionEntity entityRetiro;
    private TransaccionEntity entityPendiente;
    private TransaccionEntity entityRechazada;
    private TransaccionEntity entityRevertida;



    @BeforeEach  // se ejecuta antes de cada test
    void setUp() {
        transaccionMapper = new TransaccionMapper();

        // IDs
        transaccionId = new TransaccionId("TXN-2024-0000001");
        cuentaOrigenId = CuentaId.newCuentaId("ARG0170001000000012345000");
        cuentaDestinoId = CuentaId.newCuentaId("ARG0170001000000012345010");
        
        // Monto y fecha
        monto = Dinero.nuevo(new BigDecimal("1000.50"), Moneda.ARG);
        fechaFija = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        referencia = "REF-123456";
        descripcion = "Transferencia de prueba";

        //  DOMINIO - TRANSFERENCIAS
        // Transferencia COMPLETADA
        transferenciaCompletada = new Transaccion(
            transaccionId,
            TipoTransaccion.TRANSFERENCIA,
            cuentaOrigenId,
            cuentaDestinoId,
            fechaFija,
            EstadoTransaccion.COMPLETADA,
            referencia,
            monto,
            descripcion
        );



        // DOMINIO - DEPÓSITO (sin origen)
        depositoCompletado = new Transaccion(
            transaccionId,
            TipoTransaccion.DEPOSITO,
            null,  // sin origen
            cuentaDestinoId,
            fechaFija,
            EstadoTransaccion.COMPLETADA,
            referencia,
            monto,
            "Depósito de prueba"
        );

        //  DOMINIO - RETIRO (sin destino)
        retiroCompletado = new Transaccion(
            transaccionId,
            TipoTransaccion.RETIRO,
            cuentaOrigenId,
            null,  // sin destino
            fechaFija,
            EstadoTransaccion.COMPLETADA,
            referencia,
            monto,
            "Retiro de prueba"
        );

        // ENTITIES
        // Entity Transferencia COMPLETADA
        entityTransferencia = new TransaccionEntity();
        entityTransferencia.setTransaccionId(transaccionId.getValor());
        entityTransferencia.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA.name());
        entityTransferencia.setCuentaOrigenId(cuentaOrigenId.getValor());
        entityTransferencia.setCuentaDestinoId(cuentaDestinoId.getValor());
        entityTransferencia.setMonto(monto.getMonto());
        entityTransferencia.setMoneda(Moneda.ARG.name());
        entityTransferencia.setDescripcion(descripcion);
        entityTransferencia.setFechaDeCreacion(fechaFija);
        entityTransferencia.setEstado(EstadoTransaccion.COMPLETADA);
        entityTransferencia.setReferencia(referencia);

        // Entity Depósito
        entityDeposito = new TransaccionEntity();
        entityDeposito.setTransaccionId(transaccionId.getValor());
        entityDeposito.setTipoTransaccion(TipoTransaccion.DEPOSITO.name());
        entityDeposito.setCuentaOrigenId(null);
        entityDeposito.setCuentaDestinoId(cuentaDestinoId.getValor());
        entityDeposito.setMonto(monto.getMonto());
        entityDeposito.setMoneda(Moneda.ARG.name());
        entityDeposito.setDescripcion("Depósito de prueba");
        entityDeposito.setFechaDeCreacion(fechaFija);
        entityDeposito.setEstado(EstadoTransaccion.COMPLETADA);
        entityDeposito.setReferencia(referencia);

        // Entity Retiro
        entityRetiro = new TransaccionEntity();
        entityRetiro.setTransaccionId(transaccionId.getValor());
        entityRetiro.setTipoTransaccion(TipoTransaccion.RETIRO.name());
        entityRetiro.setCuentaOrigenId(cuentaOrigenId.getValor());
        entityRetiro.setCuentaDestinoId(null);
        entityRetiro.setMonto(monto.getMonto());
        entityRetiro.setMoneda(Moneda.ARG.name());
        entityRetiro.setDescripcion("Retiro de prueba");
        entityRetiro.setFechaDeCreacion(fechaFija);
        entityRetiro.setEstado(EstadoTransaccion.COMPLETADA);
        entityRetiro.setReferencia(referencia);

        // Entity Pendiente
        entityPendiente = new TransaccionEntity();
        entityPendiente.setTransaccionId(transaccionId.getValor());
        entityPendiente.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA.name());
        entityPendiente.setCuentaOrigenId(cuentaOrigenId.getValor());
        entityPendiente.setCuentaDestinoId(cuentaDestinoId.getValor());
        entityPendiente.setMonto(monto.getMonto());
        entityPendiente.setMoneda(Moneda.ARG.name());
        entityPendiente.setDescripcion(descripcion);
        entityPendiente.setFechaDeCreacion(fechaFija);
        entityPendiente.setEstado(EstadoTransaccion.PENDIENTE);
        entityPendiente.setReferencia(referencia);

        // Entity Rechazada
        entityRechazada = new TransaccionEntity();
        entityRechazada.setTransaccionId(transaccionId.getValor());
        entityRechazada.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA.name());
        entityRechazada.setCuentaOrigenId(cuentaOrigenId.getValor());
        entityRechazada.setCuentaDestinoId(cuentaDestinoId.getValor());
        entityRechazada.setMonto(monto.getMonto());
        entityRechazada.setMoneda(Moneda.ARG.name());
        entityRechazada.setDescripcion(descripcion);
        entityRechazada.setFechaDeCreacion(fechaFija);
        entityRechazada.setEstado(EstadoTransaccion.RECHAZADA);
        entityRechazada.setReferencia(referencia);

        // Entity Revertida
        entityRevertida = new TransaccionEntity();
        entityRevertida.setTransaccionId(transaccionId.getValor());
        entityRevertida.setTipoTransaccion(TipoTransaccion.TRANSFERENCIA.name());
        entityRevertida.setCuentaOrigenId(cuentaOrigenId.getValor());
        entityRevertida.setCuentaDestinoId(cuentaDestinoId.getValor());
        entityRevertida.setMonto(monto.getMonto());
        entityRevertida.setMoneda(Moneda.ARG.name());
        entityRevertida.setDescripcion(descripcion);
        entityRevertida.setFechaDeCreacion(fechaFija);
        entityRevertida.setEstado(EstadoTransaccion.REVERTIDA);
        entityRevertida.setReferencia(referencia);
    }




    @Nested
    @DisplayName("Conversión Entity -> Dominio (aDominio)")
    class EntityToDominioTest {

        @Test
        @DisplayName("Debería convertir transferencia completada correctamente")
        void aDominio_TransferenciaCompletada_DominioCorrecto() {
           
            Transaccion resultado = transaccionMapper.aDominio(entityTransferencia);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId().getValor()).isEqualTo("TXN-2024-0000001");
            assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.TRANSFERENCIA);
            assertThat(resultado.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
            assertThat(resultado.getCuentaOrigen().getValor()).isEqualTo(cuentaOrigenId.getValor());
            assertThat(resultado.getCuentaDestino().getValor()).isEqualTo(cuentaDestinoId.getValor());
            assertThat(resultado.getMonto().getMonto()).isEqualByComparingTo("1000.50");
            assertThat(resultado.getMonto().getMoneda()).isEqualTo(Moneda.ARG);
            assertThat(resultado.getDescripcion()).isEqualTo(descripcion);
            assertThat(resultado.getFechaCreacion()).isEqualTo(fechaFija);
            assertThat(resultado.getReferencia()).isEqualTo(referencia);
        }

        @Test
        @DisplayName("Debería convertir depósito (sin origen) correctamente")
        void aDominio_DepositoSinOrigen_DominioCorrecto() {
          
            Transaccion resultado = transaccionMapper.aDominio(entityDeposito);

            
            assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.DEPOSITO);
            assertThat(resultado.getCuentaOrigen()).isNull();
            assertThat(resultado.getCuentaDestino().getValor()).isEqualTo(cuentaDestinoId.getValor());
        }

        @Test
        @DisplayName("Debería convertir retiro (sin destino) correctamente")
        void aDominio_RetiroSinDestino_DominioCorrecto() {
           
            Transaccion resultado = transaccionMapper.aDominio(entityRetiro);

           
            assertThat(resultado.getTipo()).isEqualTo(TipoTransaccion.RETIRO);
            assertThat(resultado.getCuentaOrigen().getValor()).isEqualTo(cuentaOrigenId.getValor());
            assertThat(resultado.getCuentaDestino()).isNull();
        }

        @Test
        @DisplayName("Debería convertir transacción pendiente correctamente")
        void aDominio_TransaccionPendiente_DominioPendiente() {
            
            Transaccion resultado = transaccionMapper.aDominio(entityPendiente);

           
            assertThat(resultado.getEstado()).isEqualTo(EstadoTransaccion.PENDIENTE);
        }

        @Test
        @DisplayName("Debería convertir transacción rechazada correctamente")
        void aDominio_TransaccionRechazada_DominioRechazada() {
            
            Transaccion resultado = transaccionMapper.aDominio(entityRechazada);

           
            assertThat(resultado.getEstado()).isEqualTo(EstadoTransaccion.RECHAZADA);
        }

        @Test
        @DisplayName("Debería convertir transacción revertida correctamente")
        void aDominio_TransaccionRevertida_DominioRevertida() {
            
            Transaccion resultado = transaccionMapper.aDominio(entityRevertida);

            
            assertThat(resultado.getEstado()).isEqualTo(EstadoTransaccion.REVERTIDA);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el tipo de transacción es inválido")
        void aDominio_TipoInvalido_LanzaExcepcion() {
            
            entityTransferencia.setTipoTransaccion("INVALIDO");

            
            assertThatThrownBy(() -> transaccionMapper.aDominio(entityTransferencia))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando el estado es inválido")
        void aDominio_EstadoInvalido_LanzaExcepcion() {
           
            entityTransferencia.setEstado(null); // Esto causará NPE en valueOf

            
            assertThatThrownBy(() -> transaccionMapper.aDominio(entityTransferencia))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando la moneda es inválida")
        void aDominio_MonedaInvalida_LanzaExcepcion() {
         
            entityTransferencia.setMoneda("XYZ");

           
            assertThatThrownBy(() -> transaccionMapper.aDominio(entityTransferencia))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de moneda no válido");
        }
    }



    @Nested
    @DisplayName("Conversión Dominio → Entity (aEntity)")
    class DominioToEntityTest {



        @Test
        @DisplayName("Debería convertir transferencia a entity correctamente (nueva)")
        void aEntity_Transferencia_SinEntityExistente_EntityNueva() {
           
            TransaccionEntity resultado = transaccionMapper.aEntity(transferenciaCompletada, null);

          
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTransaccionId()).isEqualTo("TXN-2024-0000001");
            assertThat(resultado.getTipoTransaccion()).isEqualTo("TRANSFERENCIA");
            assertThat(resultado.getCuentaOrigenId()).isEqualTo(cuentaOrigenId.getValor());
            assertThat(resultado.getCuentaDestinoId()).isEqualTo(cuentaDestinoId.getValor());
            assertThat(resultado.getMonto()).isEqualByComparingTo("1000.50");
            assertThat(resultado.getMoneda()).isEqualTo("ARG");
            assertThat(resultado.getDescripcion()).isEqualTo(descripcion);
            assertThat(resultado.getFechaDeCreacion()).isEqualTo(fechaFija);
            assertThat(resultado.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
            assertThat(resultado.getReferencia()).isEqualTo(referencia);
        }

        @Test
        @DisplayName("Debería convertir depósito (sin origen) a entity correctamente")
        void aEntity_Deposito_EntitySinOrigen() {
  
            TransaccionEntity resultado = transaccionMapper.aEntity(depositoCompletado, null);

         
            assertThat(resultado.getTipoTransaccion()).isEqualTo("DEPOSITO");
            assertThat(resultado.getCuentaOrigenId()).isNull();
            assertThat(resultado.getCuentaDestinoId()).isEqualTo(cuentaDestinoId.getValor());
        }

        @Test
        @DisplayName("Debería convertir retiro (sin destino) a entity correctamente")
        void aEntity_Retiro_EntitySinDestino() {
            
            TransaccionEntity resultado = transaccionMapper.aEntity(retiroCompletado, null);

            
            assertThat(resultado.getTipoTransaccion()).isEqualTo("RETIRO");
            assertThat(resultado.getCuentaOrigenId()).isEqualTo(cuentaOrigenId.getValor());
            assertThat(resultado.getCuentaDestinoId()).isNull();
        }

        @Test
        @DisplayName("Debería actualizar entity existente en lugar de crear nueva")
        void aEntity_ConEntityExistente_ActualizaExistente() {
            
            TransaccionEntity entityExistente = new TransaccionEntity();
            UUID idExistente = UUID.randomUUID();
            entityExistente.setId(idExistente);
            entityExistente.setTransaccionId("TXN-OLD");

            
            TransaccionEntity resultado = transaccionMapper.aEntity(transferenciaCompletada, entityExistente);

            
            assertThat(resultado).isSameAs(entityExistente);
            assertThat(resultado.getId()).isEqualTo(idExistente);
            assertThat(resultado.getTransaccionId()).isEqualTo("TXN-2024-0000001"); // Actualizado
        }

        @Test
        @DisplayName("Debería mantener el ID de la entity existente")
        void aEntity_ConEntityExistente_MantieneId() {
           
            UUID idExistente = UUID.randomUUID();
            TransaccionEntity entityExistente = new TransaccionEntity();
            entityExistente.setId(idExistente);

     
            TransaccionEntity resultado = transaccionMapper.aEntity(transferenciaCompletada, entityExistente);

        
            assertThat(resultado.getId()).isEqualTo(idExistente);
        }

        @Test
        @DisplayName("Debería sobrescribir todos los datos de entity existente")
        void aEntity_ConEntityExistente_SobrescribeDatos() {
            
            TransaccionEntity entityExistente = new TransaccionEntity();
            entityExistente.setTransaccionId("OLD");
            entityExistente.setTipoTransaccion("OLD");
            entityExistente.setCuentaOrigenId("OLD");
            entityExistente.setCuentaDestinoId("OLD");
            entityExistente.setMonto(BigDecimal.ZERO);
            entityExistente.setMoneda("USD");
            entityExistente.setDescripcion("OLD");
            entityExistente.setFechaDeCreacion(LocalDateTime.now().minusDays(1));
            entityExistente.setEstado(EstadoTransaccion.PENDIENTE);
            entityExistente.setReferencia("OLD");

            
            TransaccionEntity resultado = transaccionMapper.aEntity(transferenciaCompletada, entityExistente);

           
            assertThat(resultado.getTransaccionId()).isEqualTo("TXN-2024-0000001");
            assertThat(resultado.getTipoTransaccion()).isEqualTo("TRANSFERENCIA");
            assertThat(resultado.getCuentaOrigenId()).isEqualTo(cuentaOrigenId.getValor());
            assertThat(resultado.getCuentaDestinoId()).isEqualTo(cuentaDestinoId.getValor());
            assertThat(resultado.getMonto()).isEqualByComparingTo("1000.50");
            assertThat(resultado.getMoneda()).isEqualTo("ARG");
            assertThat(resultado.getDescripcion()).isEqualTo(descripcion);
            assertThat(resultado.getFechaDeCreacion()).isEqualTo(fechaFija);
            assertThat(resultado.getEstado()).isEqualTo(EstadoTransaccion.COMPLETADA);
            assertThat(resultado.getReferencia()).isEqualTo(referencia);
        }
    }




    @Nested
    @DisplayName("Bidireccional - Consistencia")
    class BidirectionalTest {



        @Test
        @DisplayName("Dominio -> Entity -> Dominio debería mantener los datos (transferencia)")
        void dominioToEntityToDominio_Transferencia_MantieneDatos() {
            
            Transaccion dominioOriginal = transferenciaCompletada;

           
            TransaccionEntity entity = transaccionMapper.aEntity(dominioOriginal, null);
            Transaccion dominioReconstruido = transaccionMapper.aDominio(entity);

            
            assertThat(dominioReconstruido.getId().getValor()).isEqualTo(dominioOriginal.getId().getValor());
            assertThat(dominioReconstruido.getTipo()).isEqualTo(dominioOriginal.getTipo());
            assertThat(dominioReconstruido.getEstado()).isEqualTo(dominioOriginal.getEstado());
            assertThat(dominioReconstruido.getCuentaOrigen().getValor()).isEqualTo(dominioOriginal.getCuentaOrigen().getValor());
            assertThat(dominioReconstruido.getCuentaDestino().getValor()).isEqualTo(dominioOriginal.getCuentaDestino().getValor());
            assertThat(dominioReconstruido.getMonto().getMonto()).isEqualByComparingTo(dominioOriginal.getMonto().getMonto());
            assertThat(dominioReconstruido.getDescripcion()).isEqualTo(dominioOriginal.getDescripcion());
            assertThat(dominioReconstruido.getFechaCreacion()).isEqualTo(dominioOriginal.getFechaCreacion());
            assertThat(dominioReconstruido.getReferencia()).isEqualTo(dominioOriginal.getReferencia());
        }

        @Test
        @DisplayName("Dominio → Entity → Dominio debería mantener los datos (depósito)")
        void dominioToEntityToDominio_Deposito_MantieneDatos() {
            
            Transaccion dominioOriginal = depositoCompletado;

         
            TransaccionEntity entity = transaccionMapper.aEntity(dominioOriginal, null);
            Transaccion dominioReconstruido = transaccionMapper.aDominio(entity);

     
            assertThat(dominioReconstruido.getTipo()).isEqualTo(TipoTransaccion.DEPOSITO);
            assertThat(dominioReconstruido.getCuentaOrigen()).isNull();
            assertThat(dominioReconstruido.getCuentaDestino().getValor()).isEqualTo(dominioOriginal.getCuentaDestino().getValor());
        }

        @Test
        @DisplayName("Entity → Dominio → Entity debería mantener los datos")
        void entityToDominioToEntity_MantieneDatos() {
           
            TransaccionEntity entityOriginal = entityTransferencia;

            
            Transaccion dominio = transaccionMapper.aDominio(entityOriginal);
            TransaccionEntity entityReconstruida = transaccionMapper.aEntity(dominio, null);

           
            assertThat(entityReconstruida.getTransaccionId()).isEqualTo(entityOriginal.getTransaccionId());
            assertThat(entityReconstruida.getTipoTransaccion()).isEqualTo(entityOriginal.getTipoTransaccion());
            assertThat(entityReconstruida.getCuentaOrigenId()).isEqualTo(entityOriginal.getCuentaOrigenId());
            assertThat(entityReconstruida.getCuentaDestinoId()).isEqualTo(entityOriginal.getCuentaDestinoId());
            assertThat(entityReconstruida.getMonto()).isEqualByComparingTo(entityOriginal.getMonto());
            assertThat(entityReconstruida.getMoneda()).isEqualTo(entityOriginal.getMoneda());
            assertThat(entityReconstruida.getDescripcion()).isEqualTo(entityOriginal.getDescripcion());
            assertThat(entityReconstruida.getFechaDeCreacion()).isEqualTo(entityOriginal.getFechaDeCreacion());
            assertThat(entityReconstruida.getEstado()).isEqualTo(entityOriginal.getEstado());
            assertThat(entityReconstruida.getReferencia()).isEqualTo(entityOriginal.getReferencia());
        }
    }



    @Nested
    @DisplayName("Casos Edge")
    class EdgeCasesTest {



        @Test
        @DisplayName("Debería lanzar excepción cuando entity es nula en aDominio")
        void aDominio_EntityNula_LanzaExcepcion() {

            assertThatThrownBy(() -> transaccionMapper.aDominio(null))
            .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería lanzar excepción cuando dominio es nulo en aEntity")
        void aEntity_DominioNulo_LanzaExcepcion() {

            assertThatThrownBy(() -> transaccionMapper.aEntity(null, null))
            .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería manejar entity con valores nulos en campos obligatorios")
        void aDominio_EntityConNulos_LanzaExcepcion() {
         
            TransaccionEntity entityInvalida = new TransaccionEntity();
            
            
            assertThatThrownBy(() -> transaccionMapper.aDominio(entityInvalida))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debería manejar correctamente entity sin referencia")
        void aDominio_EntitySinReferencia_DominioSinReferencia() {
          
            entityTransferencia.setReferencia(null);

            
            Transaccion resultado = transaccionMapper.aDominio(entityTransferencia);

            
            assertThat(resultado.getReferencia()).isNull();
        }

        @Test
        @DisplayName("Debería manejar entity con entity existente null (crea nueva)")
        void aEntity_EntityExistenteNull_CreaNueva() {
            
            TransaccionEntity resultado = transaccionMapper.aEntity(transferenciaCompletada, null);

            
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isNull();
        }
    }




    @Nested
    @DisplayName("Pruebas con diferentes monedas")
    class DiferentesMonedasTest {



        @Test
        @DisplayName("Debería mapear transacción en USD correctamente")
        void aDominio_TransaccionUSD_MapeoCorrecto() {
           
            entityTransferencia.setMoneda("USD");
            entityTransferencia.setMonto(new BigDecimal("1000.00"));

           
            Transaccion resultado = transaccionMapper.aDominio(entityTransferencia);

       
            assertThat(resultado.getMonto().getMoneda()).isEqualTo(Moneda.USD);
            assertThat(resultado.getMonto().getMonto()).isEqualByComparingTo("1000.00");
        }

        @Test
        @DisplayName("Debería mapear transacción en EUR correctamente")
        void aDominio_TransaccionEUR_MapeoCorrecto() {
         
            entityTransferencia.setMoneda("EUR");
            entityTransferencia.setMonto(new BigDecimal("2000.00"));

          
            Transaccion resultado = transaccionMapper.aDominio(entityTransferencia);

            
            assertThat(resultado.getMonto().getMoneda()).isEqualTo(Moneda.EUR);
            assertThat(resultado.getMonto().getMonto()).isEqualByComparingTo("2000.00");
        }

        @Test
        @DisplayName("Debería convertir dominio USD a entity USD correctamente")
        void aEntity_TransaccionUSD_EntityUSD() {
            
            Transaccion transaccionUSD = new Transaccion(
                transaccionId,
                TipoTransaccion.TRANSFERENCIA,
                cuentaOrigenId,
                cuentaDestinoId,
                fechaFija,
                EstadoTransaccion.COMPLETADA,
                referencia,
                Dinero.nuevo(new BigDecimal("1000.00"), Moneda.USD),
                descripcion
            );

            
            TransaccionEntity resultado = transaccionMapper.aEntity(transaccionUSD, null);

            
            assertThat(resultado.getMoneda()).isEqualTo("USD");
            assertThat(resultado.getMonto()).isEqualByComparingTo("1000.00");
        }
    }





}
