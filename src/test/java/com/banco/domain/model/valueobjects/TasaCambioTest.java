package com.banco.domain.model.valueobjects;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class TasaCambioTest {

    
    // TESTS DE CONSTRUCCIÓN Y VALIDACIÓN

    @Nested
    @DisplayName("Constructor y validaciones de parametros")
    class constructorTest{


        @Test
        @DisplayName("crear con parametros validos- debe crear correctamente")
        void crearConParametrosValidos_DebeCrearCorrectamente(){

            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, 
                Moneda.USD, 
                new BigDecimal("1.08")
            );

            assertThat(tasa.getMonedaOrigen()).isEqualTo(Moneda.EUR);
            assertThat(tasa.getMonedaDestino()).isEqualTo(Moneda.USD);
            assertThat(tasa.getTasa()).isEqualByComparingTo("1.08");
        }


        @Test
        @DisplayName("Crear con double - Conversión precisa")
        void crearConDouble_ConversionPrecisa() {
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.USD, 
                Moneda.EUR, 
                0.93
            );
            
            assertThat(tasa.getTasa()).isEqualByComparingTo("0.93");
        }


        @Test
        @DisplayName("Crear con tasa null - debe lanzar exception")
        void crearConTasaNull_debeLanzarException(){

            assertThatThrownBy(()-> TasaCambio.nuevaTasaCambio(Moneda.EUR, Moneda.USD, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La tasa debe ser un número positivo");
        }


        @Test
        @DisplayName("Crear con moneda origen null - debe lanzar exception")
        void crearConMonedaOrigenNull_debeLanzarException(){

            assertThatThrownBy(()-> TasaCambio.nuevaTasaCambio(null, Moneda.USD, 1.08))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Las monedas no pueden ser nulas");
        }


        @Test
        @DisplayName("Crear con moneda destino null - debe lanzar exception")
        void crearConMonedaDestinoNull_debeLanzarException(){

            assertThatThrownBy(()-> TasaCambio.nuevaTasaCambio(Moneda.EUR, null, 1.08))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Las monedas no pueden ser nulas");
        }


        @ParameterizedTest // va a recibir por parametros los valores de ValueSource 1x1
        @ValueSource(strings = {"0","-1.01", "-5.2", "0.0"})
        @DisplayName("Crear con cero o negativo - debe lanzar exception")
        void crearConCeroNegativo_debeLanzarException(String valor){

            assertThatThrownBy(()-> TasaCambio.nuevaTasaCambio(Moneda.ARG, Moneda.EUR, new BigDecimal(valor)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La tasa debe ser un número positivo");
        }


        @Test
        @DisplayName("Crear con misma moneda origen y destino - Debe lanzar excepción")
        void crearConMismaMoneda_DebeLanzarExcepcion() {
            // Tasa de EUR a EUR no tiene sentido (siempre sería 1.0)
            assertThatThrownBy(() -> 
                TasaCambio.nuevaTasaCambio(Moneda.EUR, Moneda.EUR, new BigDecimal("1.0"))
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No se necesita tasa para la misma moneda");
        }


        @Test
        @DisplayName("Constructor aplica escala de 8 decimales")
        void constructor_AplicaEscala8Decimales() {
            // Tasa con muchos decimales
            BigDecimal tasaLarga = new BigDecimal("1.123456789012345");
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, 
                Moneda.USD, 
                tasaLarga
            );
            
            // Debe redondear a 8 decimales con HALF_EVEN
            assertThat(tasa.getTasa())
                .isEqualByComparingTo("1.12345679"); 
            assertThat(tasa.getTasa().scale()).isEqualTo(8); // 8 decimales
        }
    }


    
    // TESTS DE MÉTODO inversa()

    @Nested
    @DisplayName("Tasa metodo inversa() - calculo de tasa inversa")
    class MetodoInversaTest{


        @Test
        @DisplayName("inversa de EUR->USD - debe calcular USD->EUR correctamente")
        void inversa_deEURaUSD_debeCalcularCorrectamente(){

            TasaCambio tasaDirecta = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.08")
            );

            TasaCambio tasaInversa = tasaDirecta.inversa();

            assertThat(tasaInversa.getMonedaOrigen()).isEqualTo(Moneda.USD);
            assertThat(tasaInversa.getMonedaDestino()).isEqualTo(Moneda.EUR);
            assertThat(tasaInversa.getTasa()).isEqualByComparingTo("0.92592593");
        }



        @Test
        @DisplayName("inversa() de USD→ARG - Cálculo preciso")
        void inversa_USDaARG_DebeCalcularPrecisamente() {
            // USD → ARG: 1 USD = 850 ARS (ejemplo)
            TasaCambio tasaDirecta = TasaCambio.nuevaTasaCambio(
                Moneda.USD, Moneda.ARG, new BigDecimal("850")
            );
            
            // ARG → USD: 1 ARS = 1/850 ≈ 0.00117647 USD
            TasaCambio tasaInversa = tasaDirecta.inversa();
            
            assertThat(tasaInversa.getMonedaOrigen()).isEqualTo(Moneda.ARG);
            assertThat(tasaInversa.getMonedaDestino()).isEqualTo(Moneda.USD);
            // 1/850 = 0.001176470588235294... redondeado a 8 decimales
            assertThat(tasaInversa.getTasa()).isEqualByComparingTo("0.00117647");
        }


        @Test
        @DisplayName("inversa() dos veces - Debe volver a la tasa original")
        void inversa_DosVeces_DebeVolverOriginal() {
            TasaCambio tasaOriginal = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.12345678")
            );
            
            TasaCambio inversa = tasaOriginal.inversa();
            TasaCambio inversaDeInversa = inversa.inversa();
            
            // Debería ser igual a la original (con redondeo de 8 decimales)
            assertThat(inversaDeInversa.getMonedaOrigen()).isEqualTo(tasaOriginal.getMonedaOrigen());
            assertThat(inversaDeInversa.getMonedaDestino()).isEqualTo(tasaOriginal.getMonedaDestino());
            assertThat(inversaDeInversa.getTasa()).isEqualByComparingTo(tasaOriginal.getTasa());
        }


        @Test
        @DisplayName("inversa() - Escala siempre 8 decimales")
        void inversa_EscalaSiempre8Decimales() {
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.1") // 1 decimal
            );
            
            TasaCambio inversa = tasa.inversa();
            
            // 1/1.1 = 0.9090909090909091... redondeado a 8 decimales
            assertThat(inversa.getTasa().scale()).isEqualTo(8);
            assertThat(inversa.getTasa()).isEqualByComparingTo("0.90909091");
        }


    }



    // TESTS DE MÉTODO aplicaPara()


    @Nested
    @DisplayName("METODO aplicaPara() - validacion de direccion")
    class MetodoAplicaParaTest{


        @Test
        @DisplayName("aplicaPara() mimsa direccion - debe devolver true")
        void aplicaPara_mismaDireccion_debeDevolverTrue(){

            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.08")
            );

            assertThat(tasa.aplicaPara(Moneda.EUR, Moneda.USD)).isTrue();
        }


        @Test
        @DisplayName("aplicaPara() dirección inversa - Debe devolver false")
        void aplicaPara_DireccionInversa_DebeDevolverFalse() {
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.08")
            );
            
            // USD→EUR es la dirección inversa, no aplica
            assertThat(tasa.aplicaPara(Moneda.USD, Moneda.EUR)).isFalse();
        }


        @Test
        @DisplayName("aplicaPara() monedas diferentes - Debe devolver false")
        void aplicaPara_MonedasDiferentes_DebeDevolverFalse() {
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.08")
            );
            
            // EUR→ARG es completamente diferente
            assertThat(tasa.aplicaPara(Moneda.EUR, Moneda.ARG)).isFalse();
            assertThat(tasa.aplicaPara(Moneda.USD, Moneda.ARG)).isFalse();
            assertThat(tasa.aplicaPara(Moneda.ARG, Moneda.EUR)).isFalse();
        }


        @ParameterizedTest
        @CsvSource({
            "EUR, USD, EUR, USD, true",   // Misma dirección
            "USD, EUR, USD, EUR, true",   // Misma dirección diferente par
            "EUR, USD, USD, EUR, false",  // Dirección inversa
            "EUR, USD, EUR, ARG, false",  // Destino diferente
            "EUR, USD, ARG, USD, false",  // Origen diferente
            "EUR, USD, ARG, EUR, false"   // Completamente diferente
        })
        @DisplayName("aplicaPara() multiples combinaciones")
        void aplicaPara_MultiplesCombinaciones(
            String tasaOrigen, String tasaDestino,
            String consultaOrigen, String consultaDestino,
            boolean resultadoEsperado
        ) {
            Moneda origen = Moneda.valueOf(tasaOrigen);
            Moneda destino = Moneda.valueOf(tasaDestino);
            Moneda consultaOrigenMoneda = Moneda.valueOf(consultaOrigen);
            Moneda consultaDestinoMoneda = Moneda.valueOf(consultaDestino);
            
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(origen, destino, new BigDecimal("1.0"));
            
            assertThat(tasa.aplicaPara(consultaOrigenMoneda, consultaDestinoMoneda))
            .isEqualTo(resultadoEsperado);
        }


    }



    // TESTS DE CASOS ESPECIALES/EDGE CASES


    @Nested
    @DisplayName("Casos especiales - Borde")
    class CasosEspeciales{


        @Test
        @DisplayName("Tasa muy pequeña pero positiva - Debe aceptarse")
        void tasaMuyPequenaPeroPositiva_DebeAceptarse() {
            // Tasa muy pequeña (ej: 1 ARS = 0.000001 USD)
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.ARG, Moneda.USD, new BigDecimal("0.000001")
            );
            
            assertThat(tasa.getTasa()).isPositive();
            assertThat(tasa.getTasa()).isEqualByComparingTo("0.00000100");
        }


        @Test
        @DisplayName("Tasa muy grande - Debe aceptarse")
        void tasaMuyGrande_DebeAceptarse() {
            // Tasa muy grande (ej: 1 USD = 1000 ARS en hiperinflación)
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.USD, Moneda.ARG, new BigDecimal("1000.12345678")
            );
            
            assertThat(tasa.getTasa()).isEqualByComparingTo("1000.12345678");
        }


        @Test
        @DisplayName("inversa() de tasa muy grande - Cálculo preciso")
        void inversa_TasaMuyGrande_CalculoPreciso() {
            // 1 USD = 1000 ARS
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.USD, Moneda.ARG, new BigDecimal("1000")
            );
            
            // 1 ARS = 0.001 USD
            TasaCambio inversa = tasa.inversa();
            
            assertThat(inversa.getTasa()).isEqualByComparingTo("0.00100000");
        }


        @Test
        @DisplayName("Tasa con división exacta - Redondeo HALF_EVEN")
        void tasaConDivisionExacta_RedondeoHalfEven() {
            // 1/3 = 0.33333333... (período)
            // HALF_EVEN redondea a 8 decimales
            TasaCambio tasa = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("3")
            );
            
            TasaCambio inversa = tasa.inversa(); // 1/3 = 0.33333333...
            
            // 0.3333333333333333... redondeado a 8 decimales = 0.33333333
            assertThat(inversa.getTasa()).isEqualByComparingTo("0.33333333");
        }


    }


    
}
