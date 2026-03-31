package com.banco.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

 class CuentaIdTest {


    // TEST DE CONSTRUCTOR

    @Nested
    @DisplayName("constructor y validacion de formato")
    class ConstructorTest{

        @Test
        @DisplayName("constructor - deberia crear con formato valido")
        void constructor_crearConFormatoValidoCorrectamente(){

            String idValido = "ARG0170000000000000000000";
            CuentaId cuenta = CuentaId.newCuentaId(idValido);

            assertThat(cuenta.getValor()).isEqualTo(idValido);

        }

        @Test
        @DisplayName("Crear con formato valido de otro banco - debe pasar")
        void crearConFormatoValidoOtroBanco_debePasar(){

            String idValido = "ARG0150000000000000000000";
            CuentaId cuenta = CuentaId.newCuentaId(idValido);

            assertThat(cuenta.getValor()).isEqualTo(idValido);
            assertThat(cuenta.getCodigoBanco()).isEqualTo("015");

        }


        //@ParameterizedTest = "Este test se va a ejecutar VARIAS veces"
        //@ValueSource(strings = {...}) = "Con cada uno de estos valores"
        //En el parametro del test que coloquemos tomara sus valores 1x1
        @ParameterizedTest
        @ValueSource( strings = {
            "ARG",                         //corto
            "ARG017",                      //incompleto
            "ARG017000000000000000000",    //24 charts
            "ARG01700000000000000000000",  //26 charts
            "XXX0170000000000000000000",   //prefijo incorrecto
            "arg0170000000000000000000",   //minuscula
            "ARG01A0000000000000000000",   //caracter no numerico
            "ARG0110000000000000000000"    //codigo banco incorrecto
        })
        @DisplayName("Crear con formato invalido- debe lanzar exception")
        void crearConFormatoInvalido_debeLanzarException(String formatoInvalido){

            assertThatThrownBy(()-> CuentaId.newCuentaId(formatoInvalido))
            .isInstanceOf(IllegalArgumentException.class);

        }

        @Test
        @DisplayName("crear con null - debe lanzar exception")
        void crearConNull_DebeLanzarException(){


            assertThatThrownBy(()-> CuentaId.newCuentaId(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No se permite nulos");
        }
  
    
    }

    
    // TEST DE METODOS DE ACCESO

    @Nested
    @DisplayName("Metodos de acceso -extraccion de componentes")
    class MetodosAccessoTest{


        @Test
        @DisplayName("getCodigoBanco() - debe extraer el codigo correctamente")
        void extraerCodigoBanco_DebeExtraerloCorrectamente(){

             CuentaId cuentaId = CuentaId.newCuentaId("ARG0171234567890123456789");

             assertThat(cuentaId.getCodigoBanco()).isEqualTo("017");
        }

        @Test
        @DisplayName("getCodigoSucursal() - Debe extraer sucursal correcta")
        void getCodigoSucursal_DebeExtraerSucursalCorrecta() {
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0171234567890123456789");
            
            //substring(6, 9) para sucursal
            // "ARG0171234567890123456789"
            // Pos 6-9 = "123"
            assertThat(cuentaId.getCodigoSucursal()).isEqualTo("1234");
        }

        @Test
        @DisplayName("getTipoDeCuenta() - Debe extraer tipo correcto")
        void getTipoDeCuenta_DebeExtraerTipoCorrecto() {
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0171234007890123456789");
            
            // substring(9, 11) = pos 9-10 = "45"
            assertThat(cuentaId.getTipoDeCuenta()).isEqualTo("00");
        }

        @Test
        @DisplayName("getNumeroLegible() - Debe formatear correctamente")
        void getNumeroLegible_DebeFormatearCorrectamente() {
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0171234001234567890123");
            
            // Formato esperado: "017-1234-00-1234567890"
            // getCodigoBanco(): "017"
            // getCodigoSucursal(): "1234" 
            // getTipoDeCuenta(): "00"
            // últimos 10 dígitos: "1234567890"
            String legible = cuentaId.getNumeroLegible();
            
            assertThat(legible).contains("017");
            assertThat(legible).contains("00");
            assertThat(legible).containsPattern("\\d{10}$"); // Termina con 10 dígitos
        }
    }



    // TESTS DE LÓGICA DE NEGOCIO

    @Nested
    @DisplayName("Logica de negocio")
    class LogicaNegocioTest{


        //@CsvSource: Múltiples parámetros → test(String 1er valor, 2do valor)
        // similar a @ValueSource: Solo un parámetro → test(String valor)
        //En el parametro del test que coloquemos tomara sus valores 1x1
        @ParameterizedTest
        @CsvSource({
            "ARG0171234001234567890123, ARG",
            "ARG0171234101234567890123, USD",
            "ARG0171234201234567890123, EUR",
            "ARG0171234051234567890123, ARG",
            "ARG0171234151234567890123, USD",
            "ARG0171234251234567890123, EUR"

        })
        @DisplayName("Deducir moneda correctamente x tipo")
        void deducirMonedaTipo_DebeDeducirCorrectamente(String cuentaString, Moneda monedaEsperada){

            CuentaId cuenta = CuentaId.newCuentaId(cuentaString);

            assertThat(cuenta.deducirMoneda()).isEqualTo(monedaEsperada);

        }


        @Test
        @DisplayName("deducirMoneda() con tipo fuera de rango - Debe lanzar excepción")
        void deducirMoneda_TipoFueraDeRango_DebeLanzarExcepcion() {
            // Tipo 30 está fuera del rango 0-29
            CuentaId cuentaId = CuentaId.newCuentaId("ARG0171234301234567890123");
            
            assertThatThrownBy(() -> cuentaId.deducirMoneda())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Tipo de cuenta no reconocida");
        }


        @Test
        @DisplayName("esCuentaEnPesos() con cuenta ARG - Debe devolver true")
        void esCuentaEnPesos_CuentaARG_DebeDevolverTrue() {
            // Tipo 00-09 son cuentas en pesos
            CuentaId cuentaPesos = CuentaId.newCuentaId("ARG0171234001234567890123");
            
            assertThat(cuentaPesos.esCuentaEnPesos()).isTrue();
        }

        
        @Test
        @DisplayName("esCuentaEnPesos() con cuenta USD - Debe devolver false")
        void esCuentaEnPesos_CuentaUSD_DebeDevolverFalse() {
            // Tipo 10-19 son cuentas en USD
            CuentaId cuentaDolares = CuentaId.newCuentaId("ARG0171234101234567890123");
            
            assertThat(cuentaDolares.esCuentaEnPesos()).isFalse();
        }


        @Test
        @DisplayName("esCuentaEnPesos() con cuenta EUR - Debe devolver false")
        void esCuentaEnPesos_CuentaEUR_DebeDevolverFalse() {
            // Tipo 20-29 son cuentas en EUR
            CuentaId cuentaEuros = CuentaId.newCuentaId("ARG0171234201234567890123");
            
            assertThat(cuentaEuros.esCuentaEnPesos()).isFalse();
        }

    }


    // TESTS DE MÉTODO FÁBRICA generarNueva()

    @Nested
    @DisplayName("Metodos de fabrica")
    class MetodosFabricaTest{

        
        @Test
        @DisplayName("generarNueva() con parámetros válidos - Debe crear cuenta válida")
        void generarNueva_ParametrosValidos_DebeCrearCuentaValida() {
            CuentaId nuevaCuenta = CuentaId.generarNueva(017, 1234, Moneda.ARG);
            
            assertThat(nuevaCuenta).isNotNull();
            assertThat(nuevaCuenta.getTipoDeCuenta()).isEqualTo("00");
            assertThat(nuevaCuenta.getValor()).startsWith("ARG"); // startWith = empieza con...
        }


        @Test
        @DisplayName("generarNueva() con moneda USD - Tipo debe ser 10")
        void generarNueva_MonedaUSD_TipoDebeSer10() {

            CuentaId cuenta = CuentaId.generarNueva(017, 1234, Moneda.USD);
            
            assertThat(cuenta.getTipoDeCuenta()).isEqualTo("10");
            assertThat(cuenta.deducirMoneda()).isEqualTo(Moneda.USD);
        }


        @Test
        @DisplayName("generarNueva() con moneda EUR - Tipo debe ser 20")
        void generarNueva_MonedaEUR_TipoDebeSer20() {
            CuentaId cuenta = CuentaId.generarNueva(017, 1234, Moneda.EUR);
            
            assertThat(cuenta.getTipoDeCuenta()).isEqualTo("20");
            assertThat(cuenta.deducirMoneda()).isEqualTo(Moneda.EUR);
        }


        @Test
        @DisplayName("generarNueva() - Código banco debe formatearse con 4 dígitos")
        void generarNueva_CodigoBancoDebeFormatearse4Digitos() {
            CuentaId cuenta = CuentaId.generarNueva(17, 1234, Moneda.ARG);
            
            // 7 debe convertirse a "0007"
            assertThat(cuenta.getCodigoBanco()).isEqualTo("017");
        }


        @Test
        @DisplayName("generarNueva() - Sucursal debe formatearse con 4 dígitos")
        void generarNueva_SucursalDebeFormatearse4Digitos() {
            CuentaId cuenta = CuentaId.generarNueva(17, 89, Moneda.ARG);
            
            // 89 debe convertirse a "0089"
            assertThat(cuenta.getCodigoSucursal()).isEqualTo("0089");
        }

    }
    
}
