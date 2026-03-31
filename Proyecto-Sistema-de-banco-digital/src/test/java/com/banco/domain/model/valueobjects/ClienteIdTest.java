package com.banco.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClienteIdTest {


    //TEST DE CONSTRUCTOR Y VALIDACIONES
    
    @Nested
    @DisplayName("Construccion y validacionens")
    class ConstuctorTest{



        @Test
        @DisplayName("newCliente() con formato valido - debe funcionar")
        void newClienteConFormatoValido_debeFuncionar(){

            ClienteId id = ClienteId.newCliente("CLI-12345678");

            assertThat(id.getValor()).isEqualTo("CLI-12345678");
            assertThat(id.getParteNumerica()).isEqualTo("12345678");
        }


        @Test
        @DisplayName("newCliente() con diferentes formatos validos - debe funcionar")
        void newClienteConFormatosDiferentesValidos_debeFuncionar(){

            ClienteId cliente1 = ClienteId.newCliente("CLI-00000001");
            ClienteId cliente2 = ClienteId.newCliente("CLI-99999999");
            ClienteId cliente3 = ClienteId.newCliente("CLI-01020304");

            assertThat(cliente1.getValor()).isEqualTo("CLI-00000001");
            assertThat(cliente2.getValor()).isEqualTo("CLI-99999999");
            assertThat(cliente3.getValor()).isEqualTo("CLI-01020304");
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "CLI-123",           // Muy corto
            "CLI-123456789",     // Muy largo (9 dígitos)
            "CLI-ABCDEFGH",      // Letras en lugar de números
            "cli-12345678",      // Minúsculas
            "CLI12345678",       // Sin guión
            "ABC-12345678",      // Prefijo incorrecto
            "",                  // Vacío
            "   ",               // Espacios
            "CLI-12A45678",      // Letra en medio
            "CLI-12 45678",      // Espacio en medio
        })
        @DisplayName("newCliente() con formatos invalidos -  debe lanzar exception")
        void newClienteConFormatosInvalidos_debeLanzarException(String valor){

            assertThatThrownBy(()-> ClienteId.newCliente(valor))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Formato de id de cliente invalido");
        }


        @Test
        @DisplayName("newCliente() con null - Debe lanzar excepción")
        void newCliente_ConNull_DebeLanzarExcepcion() {
            assertThatThrownBy(() -> ClienteId.newCliente(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no puede ser nulo");
        }

    }


    //TEST METODO FABRICA

    @Nested
    @DisplayName("Test metodo fabrica")
    class MetodoFabricaTest{


        @Test
        @DisplayName("generarNuevoId() debe crear id con formato valido")
        void generarNuevoId_debeCrearConFormatoValido(){

            ClienteId cliente = ClienteId.generarNuevoId();

            assertThat(cliente.getValor()).startsWith("CLI-");
            assertThat(cliente.getValor()).hasSize(12); //12 digitos
            assertThat(cliente.getParteNumerica()).hasSize(8);
            assertThat(cliente.getParteNumerica()).containsOnlyDigits(); // verifica la porte numerica (son numeros)
        }


        @Test
        @DisplayName("generarNuevoId() múltiples veces - Todos deben ser válidos y diferentes")
        void generarNuevoId_MultiplesVeces_TodosValidosYDiferentes() {
            
            ClienteId id1 = ClienteId.generarNuevoId();
            ClienteId id2 = ClienteId.generarNuevoId();
            ClienteId id3 = ClienteId.generarNuevoId();
            
            
            assertThat(ClienteId.esValido(id1.getValor())).isTrue();
            assertThat(ClienteId.esValido(id2.getValor())).isTrue();
            assertThat(ClienteId.esValido(id3.getValor())).isTrue();
            
            // Muy probablemente sean diferentes (aunque hay chance de colisión)
            // Solo verificamos que al menos no sean IGUALES los 3
            assertThat(id1.getValor().equals(id2.getValor()) && id2.getValor().equals(id3.getValor()))
            .as("Los tres IDs generados no deberían ser iguales (muy improbable)")
            .isFalse();
        }



        @Test
        @DisplayName("generarNuevoId() - Formateo correcto con ceros a la izquierda")
        void generarNuevoId_FormateoCorrectoCerosIzquierda() {
            // Ejecutamos varias veces para aumentar probabilidad de ver ceros
            for (int i = 0; i < 100; i++) {
                ClienteId id = ClienteId.generarNuevoId();
                String parteNumerica = id.getParteNumerica();
                
                
                assertThat(parteNumerica).hasSize(8);
                assertThat(parteNumerica).containsOnlyDigits();
                
                // El formato debe ser CLI-XXXXXXXX
                assertThat(id.getValor()).matches("CLI-\\d{8}");
            }
        }


    }



    //TEST METODOS DE NEGOCIO


    @Nested
    @DisplayName("Metodos de negocio - reglas de negocio")
    class MetodosNegociosTest{


        @ParameterizedTest
        @CsvSource({
            "CLI-01020304, 1",    // Sucursal 01
            "CLI-23040506, 23",   // Sucursal 23  
            "CLI-45060708, 45",   // Sucursal 45
            "CLI-67080910, 67",   // Sucursal 67
            "CLI-89010203, 89",   // Sucursal 89
            "CLI-00000000, 0",    // Sucursal 00 (permisible)
            "CLI-99999999, 99"    // Sucursal 99
        })
        @DisplayName("obtenerSucursal() - Debe extraer primeros 2 dígitos como sucursal")
        void obtenerSucursal_DebeExtraerPrimeros2Digitos(String idStr, int sucursalEsperada) {
            ClienteId id = ClienteId.newCliente(idStr);
            
            assertThat(id.obtenerSucursal()).isEqualTo(sucursalEsperada);
        }


        @ParameterizedTest
        @CsvSource({
            "CLI-01020304, 2002",  // digitos 03-04 = 02 + 2000  = 2002
            "CLI-01234567, 2023",  // digitos 3-4 = 23 + 2000  = 2023
            "CLI-01990000, 2099",  // digitos 3-4 = 00 + 2000 = 2000
            "CLI-01999999, 2099",  // digitos 3-4 = 99 +  2000 = 2099
            "CLI-01000100, 2000",  // digitos 3-4 = 01 +  2000 = 2001
        })
        @DisplayName("obtenerAnioAlta() - Debe calcular año basado en dígitos 3-4")
        void obtenerAnioAlta_DebeCalcularAnioBasadoDigitos34(String idStr, int anioEsperado) {
            ClienteId id = ClienteId.newCliente(idStr);
            
            assertThat(id.obtenerAnioAlta()).isEqualTo(anioEsperado);
        }


        @Test
        @DisplayName("getParteNumerica() - Debe quitar prefijo 'CLI-'")
        void getParteNumerica_DebeQuitarPrefijoCLI() {
            ClienteId id = ClienteId.newCliente("CLI-87654321");
            
            assertThat(id.getParteNumerica()).isEqualTo("87654321");
            assertThat(id.getParteNumerica()).doesNotContain("CLI");
            assertThat(id.getParteNumerica()).doesNotContain("-");
        }


    }



    // TESTS DE MÉTODOS ESTÁTICOS

    @Nested
    @DisplayName("Metodos staticos - utilidades")
    class MetodoStaticoTest{


        @Test
        @DisplayName("esValido() con ID válido - Debe devolver true")
        void esValido_IdValido_DebeDevolverTrue() {
            assertThat(ClienteId.esValido("CLI-12345678")).isTrue();
            assertThat(ClienteId.esValido("CLI-00000000")).isTrue();
            assertThat(ClienteId.esValido("CLI-99999999")).isTrue();
        }


        @ParameterizedTest
        @ValueSource(strings = {
            "CLI-123",
            "CLI-ABCDEFGH", 
            "cli-12345678",
            "ABC-12345678",
            "",
            "   ",
            "CLI-12A45678"
        })
        @DisplayName("esValido() con ID inválido - Debe devolver false")
        void esValido_IdInvalido_DebeDevolverFalse(String idInvalido) {
            assertThat(ClienteId.esValido(idInvalido)).isFalse();
        }


        @Test
        @DisplayName("esValido() con null - Debe devolver false")
        void esValido_ConNull_DebeDevolverFalse() {
            assertThat(ClienteId.esValido(null)).isFalse();
        }


        @Test
        @DisplayName("getFormatoEsperado() - Debe devolver patrón correcto")
        void getFormatoEsperado_DebeDevolverPatronCorrecto() {
            String formato = ClienteId.getFormatoEsperado();
            
            assertThat(formato).isEqualTo("CLI-\\d{8}");
            assertThat(formato).contains("CLI-");
            assertThat(formato).contains("\\d{8}");
        }


    }



    // TESTS DE CASOS ESPECIALES


    @Nested
    @DisplayName("TEST CASOS ESPECIALES - validacion regla de negocio")
    class CasosEspecialesTest{



        @Test
        @DisplayName("Sucursal 00 - Debe ser válida (sucursal principal)")
        void sucursal00_DebeSerValida() {
            // Sucursal 00 podría representar la sucursal principal/casa matriz
            ClienteId id = ClienteId.newCliente("CLI-00020304");
            
            assertThat(id.obtenerSucursal()).isEqualTo(0);
            assertThat(id.obtenerAnioAlta()).isEqualTo(2002);
        }


        @Test
        @DisplayName("Sucursal 99 - Debe ser válida (última sucursal)")
        void sucursal99_DebeSerValida() {
            // Sucursal 99 podría ser la última sucursal asignable
            ClienteId id = ClienteId.newCliente("CLI-99123456");
            
            assertThat(id.obtenerSucursal()).isEqualTo(99);
        }


        @Test
        @DisplayName("Año 2000 - Debe calcularse correctamente")
        void anio2000_DebeCalcularseCorrectamente() {
            // Dígitos 00 deberían dar año 2000
            ClienteId id = ClienteId.newCliente("CLI-01000000");
            
            assertThat(id.obtenerAnioAlta()).isEqualTo(2000);
        }


        @Test
        @DisplayName("Año 2099 - Debe calcularse correctamente")
        void anio2099_DebeCalcularseCorrectamente() {
            // Dígitos 99 deberían dar año 2099
            ClienteId id = ClienteId.newCliente("CLI-01990000");
            
            assertThat(id.obtenerAnioAlta()).isEqualTo(2099);
        }


        @Test
        @DisplayName("ID con todos ceros - Debe ser válido")
        void idConTodosCeros_DebeSerValido() {
            // CLI-00000000 debería ser válido según el patrón
            ClienteId id = ClienteId.newCliente("CLI-00000000");
            
            assertThat(id.getValor()).isEqualTo("CLI-00000000");
            assertThat(id.obtenerSucursal()).isEqualTo(0);
            assertThat(id.obtenerAnioAlta()).isEqualTo(2000);
        }


        @Test
        @DisplayName("ID con todos nueves - Debe ser válido")
        void idConTodosNueves_DebeSerValido() {
            // CLI-99999999 debería ser válido según el patrón
            ClienteId id = ClienteId.newCliente("CLI-99999999");
            
            assertThat(id.getValor()).isEqualTo("CLI-99999999");
            assertThat(id.obtenerSucursal()).isEqualTo(99);
            assertThat(id.obtenerAnioAlta()).isEqualTo(2099);
        }


    }


}
