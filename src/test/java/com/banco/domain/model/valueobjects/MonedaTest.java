package com.banco.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MonedaTest {


    @Test
    @DisplayName("Valores del enum - Deben existir EUR, USD, ARG")
    void valoresEnum_DebenExistirEUR_USD_ARG() {
        Moneda[] monedas = Moneda.values();
        
        assertThat(monedas).hasSize(3);
        assertThat(monedas).containsExactly(
            Moneda.EUR, 
            Moneda.USD, 
            Moneda.ARG
        );
    }


    @Test
    @DisplayName("EUR - Propiedades correctas")
    void eur_PropiedadesCorrectas() {
        assertThat(Moneda.EUR.getNombre()).isEqualTo("Euro");
        assertThat(Moneda.EUR.getSimbolo()).isEqualTo("€");
        assertThat(Moneda.EUR.getDecimales()).isEqualTo(2);
        assertThat(Moneda.EUR.permiteDecimales()).isTrue();
        assertThat(Moneda.EUR.requiereRedondeoEfectivo()).isFalse();
    }


    @Test
    @DisplayName("USD - Propiedades correctas")
    void usd_PropiedadesCorrectas() {
        assertThat(Moneda.USD.getNombre()).isEqualTo("Dolar");
        assertThat(Moneda.USD.getSimbolo()).isEqualTo("$");
        assertThat(Moneda.USD.getDecimales()).isEqualTo(2);
        assertThat(Moneda.USD.permiteDecimales()).isTrue();
        assertThat(Moneda.USD.requiereRedondeoEfectivo()).isFalse();
    }


    @Test
    @DisplayName("ARG - Propiedades correctas (redondeo efectivo)")
    void arg_PropiedadesCorrectas() {
        assertThat(Moneda.ARG.getNombre()).isEqualTo("Peso Argentino");
        assertThat(Moneda.ARG.getSimbolo()).isEqualTo("$");
        assertThat(Moneda.ARG.getDecimales()).isEqualTo(2);
        assertThat(Moneda.ARG.permiteDecimales()).isTrue();
        assertThat(Moneda.ARG.requiereRedondeoEfectivo()).isTrue(); // ¡Único con true!
    }



    // TESTS DE fromCodigo()


    @Test
    @DisplayName("fromCodigo() con códigos válidos - Debe funcionar")
    void fromCodigo_CodigosValidos_DebeFuncionar() {
        assertThat(Moneda.fromCodigo("EUR")).isEqualTo(Moneda.EUR);
        assertThat(Moneda.fromCodigo("USD")).isEqualTo(Moneda.USD);
        assertThat(Moneda.fromCodigo("ARG")).isEqualTo(Moneda.ARG);
        assertThat(Moneda.fromCodigo("ARS")).isEqualTo(Moneda.ARG); // Alternativo
    }


    @Test
    @DisplayName("fromCodigo() case insensitive - Debe funcionar")
    void fromCodigo_CaseInsensitive_DebeFuncionar() {
        assertThat(Moneda.fromCodigo("eur")).isEqualTo(Moneda.EUR);
        assertThat(Moneda.fromCodigo("usd")).isEqualTo(Moneda.USD);
        assertThat(Moneda.fromCodigo("arg")).isEqualTo(Moneda.ARG);
        assertThat(Moneda.fromCodigo("ars")).isEqualTo(Moneda.ARG);
    }


    @ParameterizedTest
    @NullAndEmptySource //Ejecuta el test 2 veces: con null y con ""
    @ValueSource(strings = {" ", "  ", "\t", "\n"}) // ejecuta estos tambien
    @DisplayName("fromCodigo() con código vacío o null - Debe lanzar excepción")
    void fromCodigo_CodigoVacioONull_DebeLanzarExcepcion(String codigoInvalido) {
        assertThatThrownBy(() -> Moneda.fromCodigo(codigoInvalido))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no puede estar vacío");
    }


    @Test
    @DisplayName("fromCodigo() con código inválido - Debe lanzar excepción")
    void fromCodigo_CodigoInvalido_DebeLanzarExcepcion() {
        assertThatThrownBy(() -> Moneda.fromCodigo("INVALIDO"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Código de moneda no válido");
    }



    // TESTS DE esCompatibleCon() - CRÍTICO

    @Test
    @DisplayName("esCompatibleCon() misma moneda - Debe ser true")
    void esCompatibleCon_MismaMoneda_DebeSerTrue() {
        assertThat(Moneda.EUR.esCompatibleCon(Moneda.EUR)).isTrue();
        assertThat(Moneda.USD.esCompatibleCon(Moneda.USD)).isTrue();
        assertThat(Moneda.ARG.esCompatibleCon(Moneda.ARG)).isTrue();
    }
    
    @Test
    @DisplayName("esCompatibleCon() diferente moneda - Debe ser false")
    void esCompatibleCon_DiferenteMoneda_DebeSerFalse() {
        assertThat(Moneda.EUR.esCompatibleCon(Moneda.USD)).isFalse();
        assertThat(Moneda.EUR.esCompatibleCon(Moneda.ARG)).isFalse();
        assertThat(Moneda.USD.esCompatibleCon(Moneda.EUR)).isFalse();
        assertThat(Moneda.USD.esCompatibleCon(Moneda.ARG)).isFalse();
        assertThat(Moneda.ARG.esCompatibleCon(Moneda.EUR)).isFalse();
        assertThat(Moneda.ARG.esCompatibleCon(Moneda.USD)).isFalse();
    }




    // TESTS DE redondearEfectivo() - DIFERENCIA ARG vs OTROS

    @Test
    @DisplayName("redondearEfectivo() ARG - Debe redondear a entero")
    void redondearEfectivo_ARG_DebeRedondearEntero() {
        // ARG tiene redondeoEfectivo = true
        assertThat(Moneda.ARG.redondearEfectivo(100.49)).isEqualTo(100.0);
        assertThat(Moneda.ARG.redondearEfectivo(100.50)).isEqualTo(101.0);
        assertThat(Moneda.ARG.redondearEfectivo(100.51)).isEqualTo(101.0);
        assertThat(Moneda.ARG.redondearEfectivo(123.99)).isEqualTo(124.0);
    }


    @Test
    @DisplayName("redondearEfectivo() EUR/USD - NO debe redondear (mantiene decimales)")
    void redondearEfectivo_EUR_USD_NoDebeRedondear() {
        // EUR y USD tienen redondeoEfectivo = false
        assertThat(Moneda.EUR.redondearEfectivo(100.49)).isEqualTo(100.49);
        assertThat(Moneda.EUR.redondearEfectivo(100.50)).isEqualTo(100.50);
        assertThat(Moneda.USD.redondearEfectivo(123.99)).isEqualTo(123.99);
        assertThat(Moneda.USD.redondearEfectivo(99.01)).isEqualTo(99.01);
    }



    // TESTS DE FORMATEO BÁSICO

    @Test
    @DisplayName("formatear() - Formato básico con decimales")
    void formatear_FormatoBasicoConDecimales() {
        // Todas permiten decimales (decimales > 0)
        assertThat(Moneda.EUR.formatear(100.50)).contains("€ 100,50 Euros");
        assertThat(Moneda.USD.formatear(100.50)).contains("$ 100,50 Dolar");
        assertThat(Moneda.ARG.formatear(100.50)).contains(" 100,50 Peso Argentino");

    }



    @Test
    @DisplayName("formatearParaEfectivo() ARG - Debe mostrar '(efectivo)' y redondear")
    void formatearParaEfectivo_ARG_DebeMostrarEfectivoYRedondear() {
        String formato = Moneda.ARG.formatearParaEfectivo(100.49);
        
        assertThat(formato).contains("(efectivo)");
        assertThat(formato).contains("100"); // Redondeado a entero
        assertThat(formato).doesNotContain(".49"); // Sin decimales
    }



    @Test
    @DisplayName("formatearParaEfectivo() EUR/USD - Mismo que formatear normal")
    void formatearParaEfectivo_EUR_USD_MismoQueFormatearNormal() {
        String formatoEur = Moneda.EUR.formatearParaEfectivo(100.49);
        String formatoUsd = Moneda.USD.formatearParaEfectivo(99.99);
        
        // No debe contener "(efectivo)" porque no requieren redondeo
        assertThat(formatoEur).doesNotContain("(efectivo)");
        assertThat(formatoUsd).doesNotContain("(efectivo)");
        
        // Debe mantener decimales
        assertThat(formatoEur).contains("100,49");
        assertThat(formatoUsd).contains("99,99");
    }



    // TESTS DE VALIDACIÓN ESENCIAL

    @Test
    @DisplayName("esValido() con monto positivo - Debe ser true")
    void esValido_MontoPositivo_DebeSerTrue() {
        assertThat(Moneda.EUR.esValido(100.50)).isTrue();
        assertThat(Moneda.USD.esValido(0.01)).isTrue();
        assertThat(Moneda.ARG.esValido(999999.99)).isTrue();
    }
    
    @Test
    @DisplayName("esValido() con monto negativo - Debe ser false")
    void esValido_MontoNegativo_DebeSerFalse() {
        assertThat(Moneda.EUR.esValido(-0.01)).isFalse();
        assertThat(Moneda.USD.esValido(-100.00)).isFalse();
        assertThat(Moneda.ARG.esValido(-1.00)).isFalse();
    }
    
    @Test
    @DisplayName("esValido() con monto cero - Debe ser true (cuentas vacías permitidas)")
    void esValido_MontoCero_DebeSerTrue() {
        assertThat(Moneda.EUR.esValido(0.00)).isTrue();
        assertThat(Moneda.USD.esValido(0.0)).isTrue();
        assertThat(Moneda.ARG.esValido(0)).isTrue();
    }
    
}
