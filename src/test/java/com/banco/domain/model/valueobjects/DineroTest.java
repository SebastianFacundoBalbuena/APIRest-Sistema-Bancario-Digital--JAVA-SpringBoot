package com.banco.domain.model.valueobjects;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DineroTest {
    

    // TESTS DE CONSTRUCCIÓN Y VALIDACIÓN

    // @Nested es como crear carpetas dentro de tus tests para organizarlos mejor.
    @Nested
    @DisplayName("Constructor- validacion de parametros")
    class ConstructorTest{

        @Test
        void constructor_MontoPositivoMonedaValida_debeCrearse(){

            BigDecimal monto = new BigDecimal("150.75");
            Moneda moneda = Moneda.EUR;

            Dinero dinero = new Dinero(monto, moneda);

            assertThat(dinero.getMonto()).isEqualByComparingTo("150.75");
            assertThat(dinero.getMoneda()).isEqualTo(Moneda.EUR);
            assertThat(dinero.getMontoDouble()).isEqualTo(150.75);
        }


        @Test
        @DisplayName("constructor con monto cero - debe permitirse")
        void constructor_conMontoCero_debePermitirse(){

            Dinero dinero = new Dinero(BigDecimal.ZERO, Moneda.USD);

            assertThat(dinero.getMonto()).isZero();
            assertThat(dinero.esCero()).isTrue();
        }

        @Test
        @DisplayName("constructor con monto negativo - debe lanzar ilegalArgumentException")
        void constructor_montoNegativo_debeLanzarException(){

             BigDecimal montoNegativo = new BigDecimal("-50.00");

             // 1. assertThatThrownBy: "Verifica que este código LANCE una excepción"
             // 2. isInstanceOf: " que esa excepción sea del TIPO IllegalArgumentException"
             // 3. hasMessageContaining: "que el mensaje de error CONTENGA 'no puede ser negativo'"
             assertThatThrownBy(()-> new Dinero(montoNegativo, Moneda.EUR))
             .isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("no puede ser negativo");
        }

        @Test
        @DisplayName("constructor con monto null- debe lanzar exception")
        void constructor_conMontoNull_debeLanzarException(){

            assertThatThrownBy(()-> new Dinero(null, Moneda.EUR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no debe ser nulo");
        }

        @Test
        @DisplayName("constructor con moneda null- debe lanzar exception")
        void constructor_conMonedaNull_debeLanzarException(){

            assertThatThrownBy(()-> new Dinero(new BigDecimal("100"), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La moneda no puede ser nula");
        }

        @Test
        @DisplayName("constructor aplica escala de 10 decimales - verificar redondeo")
        void constructor_aplicaEscalaDe10Decimales(){

            BigDecimal monto = new BigDecimal("100.123456789012345");
            Dinero dinero = new Dinero(monto, Moneda.EUR);

            assertThat(dinero.getMonto())
            .isEqualByComparingTo("100.1234567890");
        }

        @Test
        @DisplayName("getMontoConEscalaMoneda() - Debe usar decimales de la moneda")
        void getMontoConEscalaMoneda_DebeUsarDecimalesMoneda() {
            // EUR tiene 2 decimales
            Dinero dineroEur = new Dinero(new BigDecimal("100.1234567890"), Moneda.EUR);
            assertThat(dineroEur.getMontoConEscalaMoneda())
                .isEqualByComparingTo("100.12");  
            
            // ARG también tiene 2 decimales para digital
            Dinero dineroArg = new Dinero(new BigDecimal("200.56789"), Moneda.ARG);
            assertThat(dineroArg.getMontoConEscalaMoneda())
                .isEqualByComparingTo("200.57");  
        }

    }


    // TEST METODOS DE FABRICA

    @Nested
    @DisplayName("Metodos de fabrica")
    class metodoFabricaTest{

        @Test
        @DisplayName("nuevo() con BigDecimal- mismo comportamiento que constructor")
        void nuevoConBigDecimal_debeCrearIgualQueConstructor(){

             Dinero desdeConstructor = new Dinero(new BigDecimal("200.50"), Moneda.USD);
              Dinero desdeFabrica = Dinero.nuevo(new BigDecimal("200.50"), Moneda.USD);

              assertThat(desdeFabrica).isEqualTo(desdeConstructor);
        }

        @Test
        @DisplayName("nuevoDouble() con double - Conversión precisa")
        void nuevoDoubleConDouble_DebeConvertirPrecisamente() {
            // 0.1 + 0.2 debería ser 0.3 (problema común con double)
            Dinero dinero = Dinero.nuevoDouble(0.1 + 0.2, Moneda.EUR);
            
            assertThat(dinero.getMonto()).isEqualByComparingTo("0.3");
        }

        @Test
        @DisplayName("nuevoString() con String válido - Debe parsear correctamente")
        void nuevoStringConStringValido_DebeParsear() {
            Dinero dinero = Dinero.nuevoString("1234.56789", Moneda.ARG);
            
            assertThat(dinero.getMonto()).isEqualByComparingTo("1234.56789");
            assertThat(dinero.getMoneda()).isEqualTo(Moneda.ARG);
        }

        @Test
        @DisplayName("nuevoString() con String invalido - Debe lanzar exception")
        void nuevoString_ConStringInvalido_debeLanzarException(){

            // 2. isInstanceOf: " que esa excepción sea del TIPO ( alguna exception class ) "
            assertThatThrownBy(()-> Dinero.nuevoString("no-es-un-numero", Moneda.ARG))
            .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("nuevoCero() - Debe crear dinero con monto cero")
        void nuevoCero_DebeCrearDineroCero() {
            Dinero ceroEur = Dinero.nuevoCero(Moneda.EUR);
            Dinero ceroUsd = Dinero.nuevoCero(Moneda.USD);
            
            assertThat(ceroEur.getMonto()).isZero();
            assertThat(ceroEur.getMoneda()).isEqualTo(Moneda.EUR);

            
            assertThat(ceroUsd.getMonto()).isZero();
            assertThat(ceroUsd.getMoneda()).isEqualTo(Moneda.USD);
        }

    }


    // TEST OPERACIONES MATEMATICAS

    @Nested
    @DisplayName("OPERACIONES MATEMATICAS")
    class OperacionesMatematicasTest{

        @Test
        @DisplayName("sumar() misma moneda- debe sumar correctamente")
        void sumarMismaMoneda_debeSumearCorrectamente(){

             Dinero cienEur = new Dinero(new BigDecimal("100"), Moneda.EUR);
             Dinero cincuentaEur = new Dinero(new BigDecimal("50"), Moneda.EUR);

             Dinero resultado = cienEur.sumar(cincuentaEur);

             assertThat(resultado.getMontoConEscalaMoneda()).isEqualTo("150.00");
             assertThat(resultado.getMoneda()).isEqualTo(Moneda.EUR);
        }

        @Test
        @DisplayName("sumar() moneda incompatible - debe lanzar exception")
        void sumarMonedaIncompatible_debeLanzarException(){

            Dinero cienEur = Dinero.nuevo(new BigDecimal(100), Moneda.EUR);
            Dinero cienUsd = Dinero.nuevo(new BigDecimal(100), Moneda.USD);

            assertThatThrownBy(() -> cienEur.sumar(cienUsd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Moneda no compatible");

        }

        @Test
        @DisplayName("restar() resultado positivo - debe restar correctamente")
        void restarResultadoPositivo_DebeRestarCorrectamente(){

            Dinero cienEur = Dinero.nuevo(new BigDecimal(100), Moneda.EUR);
            Dinero cincEur = Dinero.nuevo(new BigDecimal(50), Moneda.EUR);

            Dinero resultado = cienEur.restar(cincEur);

            assertThat(resultado.getMontoConEscalaMoneda()).isEqualTo("50.00");
            assertThat(resultado.getMoneda()).isEqualByComparingTo(Moneda.EUR);
        }

        @Test
        @DisplayName("restar() resultado negativo - Debe lanzar IllegalStateException")
        void restarResultadoNegativo_DebeLanzarExcepcion() {
            Dinero cincuenta = new Dinero(new BigDecimal("50"), Moneda.EUR);
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            
            assertThatThrownBy(() -> cincuenta.restar(cien))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Fondos insuficientes");
        }

        @Test
        @DisplayName("multiplicar() por BigDecimal positivo - Debe multiplicar")
        void multiplicarPorBigDecimalPositivo_DebeMultiplicar() {
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            BigDecimal multiplicador = new BigDecimal("1.5");
            
            Dinero resultado = cien.multiplicar(multiplicador);
            
            assertThat(resultado.getMonto()).isEqualByComparingTo("150.00");
        }

        @Test
        @DisplayName("multiplicar() por BigDecimal negativo - Debe lanzar excepción")
        void multiplicarPorBigDecimalNegativo_DebeLanzarExcepcion() {
            Dinero dinero = new Dinero(new BigDecimal("100"), Moneda.USD);
            BigDecimal multiplicadorNegativo = new BigDecimal("-0.5");
            
            assertThatThrownBy(() -> dinero.multiplicar(multiplicadorNegativo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser negativo");
        }

         @Test
        @DisplayName("multiplicar() por double - Debe funcionar igual")
        void multiplicarPorDouble_DebeFuncionar() {
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            
            Dinero resultado = cien.multiplicar(1.25);
            
            assertThat(resultado.getMonto()).isEqualByComparingTo("125.00");
        }

        @Test
        @DisplayName("dividir() por BigDecimal positivo - Debe dividir")
        void dividirPorBigDecimalPositivo_DebeDividir() {
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            BigDecimal divisor = new BigDecimal("4");
            
            Dinero resultado = cien.dividir(divisor);
            
            assertThat(resultado.getMonto()).isEqualByComparingTo("25.00");
        }

        @Test
        @DisplayName("dividir() por bigDecimal cero o negativo - debe lanzar exception")
        void dividirPorCeroOnegativo_debeLanzarException(){

            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            BigDecimal cero = new BigDecimal("0");
            BigDecimal negativo = new BigDecimal("-2");

            assertThatThrownBy(()-> cien.dividir(cero))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");

            assertThatThrownBy(()-> cien.dividir(negativo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("debe ser positivo");
        }

        @Test
        @DisplayName("dividir() por double - Debe funcionar igual")
        void dividirPorDouble_DebeFuncionar() {
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.USD);
            
            Dinero resultado = cien.dividir(2.5);
            
            assertThat(resultado.getMonto()).isEqualByComparingTo("40.00");
        }

    }



    // TEST DE COMPARACION

    @Nested
    @DisplayName("Comparaciones - relaciones entre montos")
    class ComparacionesEntreMontosTest{

        @Test
        @DisplayName("esMyaorQue() misma moneda- comparacion correcta")
        void esMayorQue_comparacionCorrecta(){

            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            Dinero cincuenta = new Dinero(new BigDecimal("50"), Moneda.EUR);

            assertThat(cien.esMayorQue(cincuenta)).isTrue();
            assertThat(cincuenta.esMayorQue(cien)).isFalse();
        }

        @Test
        @DisplayName("esMayorQue() monedas diferentes - Debe lanzar excepción")
        void esMayorQueMonedasDiferentes_DebeLanzarExcepcion() {
            Dinero eur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            Dinero usd = new Dinero(new BigDecimal("100"), Moneda.USD);
            
            assertThatThrownBy(() -> eur.esMayorQue(usd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No se pueden comparar");
        }

        @Test
        @DisplayName("esMayorOIgualQue() - Incluye igualdad")
        void esMayorOIgualQue_DebeIncluirIgualdad() {
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.USD);
            Dinero otroCien = new Dinero(new BigDecimal("100"), Moneda.USD);
            Dinero cincuenta = new Dinero(new BigDecimal("50"), Moneda.USD);
            
            assertThat(cien.esMayorOIgualQue(otroCien)).isTrue();  
            assertThat(cien.esMayorOIgualQue(cincuenta)).isTrue(); 
        }

        @Test
        @DisplayName("esMenorQue() y esMenorOIgualQue() - Comparaciones inversas")
        void esMenorQueEsMenorOIgualQue_ComparacionesInversas() {
            Dinero treinta = new Dinero(new BigDecimal("30"), Moneda.EUR);
            Dinero cincuenta = new Dinero(new BigDecimal("50"), Moneda.EUR);
            
            assertThat(treinta.esMenorQue(cincuenta)).isTrue();
            assertThat(treinta.esMenorOIgualQue(cincuenta)).isTrue();
            assertThat(treinta.esMenorOIgualQue(treinta)).isTrue();  
        }

        @Test
        @DisplayName("esCero() - Identifica montos cero")
        void esCero_IdentificaMontosCero() {
            Dinero cero = new Dinero(BigDecimal.ZERO, Moneda.EUR);
            Dinero positivo = new Dinero(new BigDecimal("0.01"), Moneda.EUR);
            
            assertThat(cero.esCero()).isTrue();
            assertThat(positivo.esCero()).isFalse();
        }

        @Test
        @DisplayName("esPositivo() - Identifica montos mayores que cero")
        void esPositivo_IdentificaMontosPositivos() {
            Dinero cero = new Dinero(BigDecimal.ZERO, Moneda.USD);
            Dinero positivo = new Dinero(new BigDecimal("0.01"), Moneda.USD);
            Dinero positivoGrande = new Dinero(new BigDecimal("1000"), Moneda.USD);
            
            assertThat(cero.esPositivo()).isFalse();
            assertThat(positivo.esPositivo()).isTrue();
            assertThat(positivoGrande.esPositivo()).isTrue();
        }

    }



    // TESTS DE CONVERSIÓN DE MONEDA

    @Nested
    @DisplayName("Conversion de moneda- con tasa de cambio")
    class ConversionMonedaTest{

        @Test
        @DisplayName("convertir() con tasacambio valida - debe convertir correctamente")
        void convertirConTasaCambioValida_DebeConvertir() {
        // Arrange: EUR -> USD con tasa 1.08
            Dinero cienEur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            TasaCambio tasaEurUsd = TasaCambio.nuevaTasaCambio(
            Moneda.EUR, Moneda.USD, new BigDecimal("1.08")
            );
            
            // Act
            Dinero resultado = cienEur.convertir(tasaEurUsd);
            
            // Assert: 100 EUR * 1.08 = 108 USD
            assertThat(resultado.getMonto()).isEqualByComparingTo("108");
            assertThat(resultado.getMoneda()).isEqualTo(Moneda.USD);
        }

        @Test
        @DisplayName("convertirA() con validación explícita - Más seguro")
        void convertirAConValidacionExplicita() {
            Dinero cienEur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            TasaCambio tasaEurUsd = TasaCambio.nuevaTasaCambio(
                Moneda.EUR, Moneda.USD, new BigDecimal("1.08")
            );
            
            Dinero resultado = cienEur.convertirA(Moneda.USD, tasaEurUsd);
            
            assertThat(resultado.getMoneda()).isEqualTo(Moneda.USD);
            assertThat(resultado.getMonto()).isEqualByComparingTo("108");
        }

        @Test
        @DisplayName("convertirA() con tasa dirección incorrecta - Debe lanzar excepción")
        void convertirATasaDireccionIncorrecta_DebeLanzarExcepcion() {
            Dinero cienEur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            // Tasa en dirección opuesta
            TasaCambio tasaUsdEur = TasaCambio.nuevaTasaCambio(
                Moneda.USD, Moneda.EUR, new BigDecimal("0.93")
            );
            
            assertThatThrownBy(() -> 
                cienEur.convertirA(Moneda.USD, tasaUsdEur)
            )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tasa de cambio incorrecta");
        }

        @Test
        @DisplayName("necesitaConversion() misma moneda - Debe devolver false")
        void necesitaConversionMismaMoneda_DebeDevolverFalse() {
            Dinero dineroEur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            
            assertThat(dineroEur.necesitaConversion(Moneda.EUR)).isFalse();
        }

        @Test
        @DisplayName("necesitaConversion() diferente moneda - Debe devolver true")
        void necesitaConversionDiferenteMoneda_DebeDevolverTrue() {
            Dinero dineroEur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            
            assertThat(dineroEur.necesitaConversion(Moneda.USD)).isTrue();
            assertThat(dineroEur.necesitaConversion(Moneda.ARG)).isTrue();
        }

        @Test
        @DisplayName("convertir() ARG -> EUR con redondeo - Verificar comportamiento ARG")
        void convertirArgAEur_RedondeoEspecial() {
            // ARG (Argentina) tiene redondeoEfectivo = true
            // Esto significa que en conversiones, se redondea a entero
            Dinero milArg = new Dinero(new BigDecimal("1000"), Moneda.ARG);
            TasaCambio tasaArgEur = TasaCambio.nuevaTasaCambio(
                Moneda.ARG, Moneda.EUR, new BigDecimal("0.00108")  // 1 ARG = 0.00108 EUR
            );
            
            Dinero resultado = milArg.convertir(tasaArgEur);
            
            // 1000 ARG * 0.00108 = 1.08 EUR
            // Pero ARG redondea a entero en conversiones: 1.08 → 1
            assertThat(resultado.getMonto()).isEqualByComparingTo("1.08");
            assertThat(resultado.getMoneda()).isEqualTo(Moneda.EUR);
        }

        @Test
        @DisplayName("equals() mismo monto diferente escala - Deben ser iguales (usa escala moneda)")
        void equalsMismoMontoDiferenteEscala_DebenSerIguales() {
            // 100.50 con diferentes representaciones internas
            Dinero dinero1 = new Dinero(new BigDecimal("100.5000000000"), Moneda.EUR);  // 10 decimales
            Dinero dinero2 = new Dinero(new BigDecimal("100.50"), Moneda.EUR);          // 2 decimales
            
            // Deben ser iguales porque getMontoConEscalaMoneda() los normaliza
            assertThat(dinero1).isEqualTo(dinero2);
        }

        @Test
        @DisplayName("equals() diferente monto - No deben ser iguales")
        void equalsDiferenteMonto_NoDebenSerIguales() {
            Dinero cien = new Dinero(new BigDecimal("100"), Moneda.EUR);
            Dinero doscientos = new Dinero(new BigDecimal("200"), Moneda.EUR);
            
            assertThat(cien).isNotEqualTo(doscientos);
        }

        @Test
        @DisplayName("equals() diferente moneda - No deben ser iguales")
        void equalsDiferenteMoneda_NoDebenSerIguales() {
            Dinero eur = new Dinero(new BigDecimal("100"), Moneda.EUR);
            Dinero usd = new Dinero(new BigDecimal("100"), Moneda.USD);
            
            assertThat(eur).isNotEqualTo(usd);
        }

        @Test
        @DisplayName("equals() con null - Debe devolver false")
        void equalsConNull_DebeDevolverFalse() {
            Dinero dinero = new Dinero(new BigDecimal("100"), Moneda.EUR);
            
            assertThat(dinero.equals(null)).isFalse();
        }

        @Test
        @DisplayName("equals() con objeto de diferente clase - Debe devolver false")
        void equalsConObjetoDiferenteClase_DebeDevolverFalse() {
            Dinero dinero = new Dinero(new BigDecimal("100"), Moneda.EUR);
            String noEsDinero = "100 EUR";
            
            assertThat(dinero.equals(noEsDinero)).isFalse();
        }
        
        @Test
        @DisplayName("hashCode() consistencia - Iguales objetos, igual hash")
        void hashCodeConsistencia_IgualesObjetosIgualHash() {
            Dinero dinero1 = new Dinero(new BigDecimal("75.25"), Moneda.USD);
            Dinero dinero2 = new Dinero(new BigDecimal("75.25"), Moneda.USD);
            
            assertThat(dinero1.hashCode()).isEqualTo(dinero2.hashCode());
        }

    }


    // TESTS DE INMUTABILIDAD

    @Nested
    @DisplayName("Inmutabilidad - ninguna operacion modifica estado")
    class InmutabilidadTest{

        @Test
        @DisplayName("Operaciones retornan nuevo objeto - Original inmutable")
        void operacionesRetornanNuevoObjeto_OriginalInmutable(){

            Dinero original = new Dinero(new BigDecimal("100"), Moneda.EUR);
            Dinero aSumar = new Dinero(new BigDecimal("50"), Moneda.EUR);

            Dinero resultado = original.sumar(aSumar);

            //original no cambia
            assertThat(original.getMonto()).isEqualByComparingTo("100");
            assertThat(original.getMoneda()).isEqualByComparingTo(Moneda.EUR);

            // resultado es nuevo objeto
            assertThat(resultado.getMonto()).isEqualByComparingTo("150.00");
            assertThat(resultado.getMoneda()).isEqualByComparingTo(Moneda.EUR);
        }

        @Test
        @DisplayName("Múltiples operaciones encadenadas - Cada una crea nuevo objeto")
        void multiplesOperacionesEncadenadas_CadaUnaCreaNuevoObjeto() {
            Dinero base = new Dinero(new BigDecimal("1000"), Moneda.USD);
            
            Dinero resultado = base
                .sumar(new Dinero(new BigDecimal("500"), Moneda.USD))
                .restar(new Dinero(new BigDecimal("300"), Moneda.USD))
                .multiplicar(new BigDecimal("1.1"));
            
            // (1000 + 500 - 300) * 1.1 = 1200 * 1.1 = 1320
            assertThat(resultado.getMonto()).isEqualByComparingTo("1320");
            assertThat(base.getMonto()).isEqualByComparingTo("1000");  // Base no cambió
        }

    }


    // TESTS DE BORDE/CASOS ESPECIALES

    @Nested
    @DisplayName("Casos especiales")
    class casosEspecialesTest{

        @Test
        @DisplayName("Monto extremadamente grande - Debe manejarse")
        void montoExtremadamenteGrande_DebeManejarse() {
            BigDecimal montoGrande = new BigDecimal("999999999999999.99");
            Dinero dinero = new Dinero(montoGrande, Moneda.EUR);
            
            assertThat(dinero.getMonto()).isEqualByComparingTo(montoGrande);
            assertThat(dinero.esPositivo()).isTrue();
        }

        @Test
        @DisplayName("Monto con muchos decimales - Redondeo correcto")
        void montoConMuchosDecimales_RedondeoCorrecto() {
            BigDecimal montoDecimales = new BigDecimal("123.4567890123456789");
            Dinero dinero = new Dinero(montoDecimales, Moneda.EUR);
            
            // Se redondea a 10 decimales (ESCALA_CALCULO = 10)
            assertThat(dinero.getMonto()).isEqualByComparingTo("123.4567890123");
            
            // Para presentación, se redondea a decimales de la moneda (2 para EUR)
            assertThat(dinero.getMontoConEscalaMoneda()).isEqualByComparingTo("123.46");
        }

        @Test
        @DisplayName("Monto muy pequeño pero positivo - Debe ser positivo")
        void montoMuyPequenoPeroPositivo_DebeSerPositivo() {
            
            BigDecimal montoMinimo = new BigDecimal("0.0000000001");
            Dinero minimo = Dinero.nuevo(montoMinimo, Moneda.USD);
            
            assertThat(minimo.esPositivo()).isTrue();
            assertThat(minimo.esCero()).isFalse();
        }

    }

}
