package com.banco.domain.model.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;



public final class ClienteId {

    // Todos los IDs deben ser CLI- seguido de 8 números.
    private static final String FORMATO = "CLI-\\d{8}";
    //FILTRO pattern que analisa si el formato es el correcto
    private static final Pattern VALIDAR_PATRON = Pattern.compile(FORMATO);

      // 🔒 ATRIBUTO INMUTABLE (final + private)
    private final String valor;

    // CONSTRUCTOR PRIVADO - ÚNICA FORMA DE CREAR INSTANCIAS
    private ClienteId(String valor){

        // 🛡️ PRIMERO: Validar que no sea nulo
        if(valor == null){
           throw new IllegalArgumentException("El Id del cliente no puede ser nulo");
        }
         // 🛡️ SEGUNDO: Validar formato con Pattern
         if(!VALIDAR_PATRON.matcher(valor).matches()){
              throw new IllegalArgumentException(String.format(
                "Formato de id de cliente invalido. Debe ser: %s", valor, FORMATO));
         }

         //SI PASÓ LAS VALIDACIONES: Asignar el valor
         this.valor = valor;
    }


       // METODOS DE LA CLASE

            
          //Crear nuevo ClienteId
         public static ClienteId newCliente(String valor){
            return new ClienteId(valor);
         }

         //🎲 MÉTODO FÁBRICA PARA GENERAR IDs ALEATORIOS
         //Nuevo ClienteId con formato válido y número aleatorio
         public static ClienteId generarNuevoId(){
            //  Generar 8 dígitos aleatorios
            int numeroAleatorio = (int)(Math.random() *100_000_000);
              String valor = String.format("CLI-%08d", numeroAleatorio);
              return new ClienteId(valor);
         }

         //El valor string del ID (inmutable)
         public String getValor(){
            return valor;
         }

         //Obtener solo la parte numérica (útil para algunos reportes)
         public String getParteNumerica(){
            return valor.substring(4); // Quita "CLI-"
         }

             // 💡 MÉTODOS DE NEGOCIO (comportamiento rico)
             // * 📌 REGLA DE NEGOCIO: Los primeros 2 dígitos indican sucursal
             public int obtenerSucursal(){
                String sucursal = valor.substring(4,6); // "CLI-01020304" → "01"
                return Integer.parseInt(sucursal);
             }

             //Obtener "año de alta" teórico basado en el ID
             //* 📌 REGLA DE NEGOCIO: Dígitos 3-4 indican año (últimos 2 dígitos)
             public int obtenerAnioAlta(){
                String digitosAnio = valor.substring(6,8); // "CLI-01234567" → "34" → 2034
                return 2000 + Integer.parseInt(digitosAnio);
             }

             //Comparar si es el mismo objet en memoria o contiene el mismo valor
             @Override
             public boolean equals(Object obj){
                if (this == obj) return true; // Misma referencia → mismo objeto
                if (obj == null || getClass() != obj.getClass()) return false; // Tipos diferentes

                ClienteId other = (ClienteId) obj;
                return Objects.equals(valor, other.valor); // Comparación por valor
             } 
             //     * 📝 HASHCODE: Consistente con equals() obligatorio por -contrato con equals-
             @Override
             public int hashCode(){
                return Objects.hash(valor);
             }

             //   * 🧪 VALIDAR SIN CREAR INSTANCIA
            public static boolean esValido(String valor) {
            return valor != null && VALIDAR_PATRON.matcher(valor).matches();
            }
            // OBTENER EL FORMATO ESPERADO
            public static String getFormatoEsperado(){
                return FORMATO;
            }
    }

