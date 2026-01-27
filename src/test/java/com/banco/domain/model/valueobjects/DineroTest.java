package com.banco.domain.model.valueobjects;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class DineroTest {
    

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

}
