package com.banco.domain.model.valueobjects;


import java.util.Objects;
import java.util.regex.Pattern;

public final class CuentaId {

     // 🎯 FORMATO: Código país + 22 dígitos (similar IBAN argentino)
     private static final String FORMATO = "AR\\d{22}";
     // VALIDA QUE EL FORMATO SEA EL CORRECTO
     private static final Pattern VALIDAR_FORMATO = Pattern.compile(FORMATO);

     private final String valor;

     //🏗️ CONSTRUCTOR PRIVADO - Validación completa
     private CuentaId(String valor){

        // 🛡️ VALIDACIONES EN CONSTRUCTOR

        // 1 - no nulo
        if(valor == null){
           throw new IllegalArgumentException("No se permite nulos");
        }

        // 2 - Cumple el formato
        if(!VALIDAR_FORMATO.matcher(valor).matches()){
            throw new IllegalArgumentException(String.format("El formato no es el correcto. Debe ser %s",
             valor, FORMATO));
        }

        // 4- Dígito verificador
                if (!validarDigitoVerificador(valor)) {
            throw new IllegalArgumentException("Dígito verificador de cuenta inválido");
        }

        // 5- Banco valido
                if (!esBancoValido(valor)) {
            throw new IllegalArgumentException("Código de banco no válido");
        }


        // ✅ SI PASÓ TODAS LAS VALIDACIONES
        this.valor = valor;


     }

     // METODOS DE LA CLASE


     //🏭 MÉTODO FÁBRICA PRINCIPAL
     public static CuentaId newCuentaId(String valor){
        return new CuentaId(valor);
     } 

     // GETTERS
     public String getValor(){
        return valor;
     }

     public String getCodigoBanco(){
        //Ejemplo: "AR0290000000000000000000" → Banco "0290"
        return valor.substring(2,6);
     }

     public String getCodigoSucursal(){
        //Ejemplo: "AR0290123400000000000000" → Sucursal "1234"
        return valor.substring(6,10);
     }

     public String getTipoDeCuenta(){
        //Ejemplo: "00" → Cuenta en pesos - "01" → Caja de ahorro en dólares - "02" → Cuenta corriente en dólares
        return valor.substring(10,12);
     }

         public static String getFormatoEsperado() {
        return FORMATO;
    }

     /**
     *  OBTENER NÚMERO DE CUENTA LEGIBLE
     * Formato: Banco-Sucursal-Tipo-Número
     * Ejemplo: "0290-1234-00-0000001234"
     */
    public String getNumeroLegible(){
        return String.format("%s-%s-%s-%s", getCodigoBanco(),getCodigoSucursal(),
        getTipoDeCuenta(), valor.substring(12,22)); // ultimos 10 digitos
    }



     // METODOS COMPLEJOS

     /* 📌 REGLA DE NEGOCIO: Deducir Moneda desde el tipo
     * - Tipos 00-09 → Pesos Argentinos (ARS)
     * - Tipos 10-19 → Dólares (USD)
     * - Tipos 20-29 → Euros (EUR)
     */
    public Moneda deducirMoneda(){
        int tipo = Integer.parseInt(getTipoDeCuenta());

        if(tipo >= 0 && tipo <= 9){
            return Moneda.ARG;
        }else if(tipo >=10 && tipo <= 19){
            return Moneda.USD;
        }else if(tipo >= 20 && tipo <= 29){
            return Moneda.EUR;
        }else{
            throw new IllegalStateException("Tipo de cuenta no reconocida" + tipo);
        }
    }

    // * VERIFICAR SI ES CUENTA EN PESOS
    public boolean esCuentaEnPesos(){
        return deducirMoneda() == Moneda.ARG;
    }


      /**
     * 🧮 CALCULAR DÍGITO VERIFICADOR
     */
    private static String calcularDigitoVerificador(String valorSinDigito) {
        // Algoritmo simplificado para ejemplo
        int suma = 0;
        for (char c : valorSinDigito.toCharArray()) {
            if (Character.isDigit(c)) {
                suma += Character.getNumericValue(c);
            }
        }
        int digito = 98 - (suma % 97);
        return String.format("%02d", digito);
    }

    //  VALIDAR DÍGITO VERIFICADOR 
        private static boolean validarDigitoVerificador(String valor) {
        try {
            String sinDigito = valor.substring(0, 22); // Todo menos últimos 2 dígitos
            String digitoEsperado = valor.substring(22, 24);
            String digitoCalculado = calcularDigitoVerificador(sinDigito);
            
            return digitoEsperado.equals(digitoCalculado);
        } catch (Exception e) {
            return false;
        }
    }


        /*
     * 🏦 VALIDAR CÓDIGO DE BANCO
     * En sistema real, verificaría contra base de bancos autorizados
     */
    private static boolean esBancoValido(String valor) {
        String codigoBanco = valor.substring(2, 6);
        // Ejemplo: solo permitimos algunos bancos
        return codigoBanco.equals("0290") || // Banco de ejemplo
               codigoBanco.equals("0150") || // Otro banco
               codigoBanco.equals("0720");   // Otro banco
    }


    //  DETERMINAR TIPO DE CUENTA SEGÚN MONEDA
        private static String obtenerTipoCuentaPorMoneda(Moneda moneda) {
        switch (moneda) {
            case ARG: return "00"; // Cuenta pesos argentinos
            case USD: return "10"; // Caja ahorro dólares
            case EUR: return "20"; // Caja ahorro euros
            default: throw new IllegalArgumentException("Moneda no soportada: " + moneda);
        }
    }

         //🎲 GENERAR NÚMERO ALEATORIO DE CUENTA
    private static String generarNumeroAleatorio() {
        long numero = (long) (Math.random() * 10_000_000_000L);
        return String.format("%010d", numero);
    }



        /**
     * 🏭 MÉTODO FÁBRICA PARA CREAR NUEVAS CUENTAS
     * Genera cuenta con formato válido para nuevo cliente
     * 
     * @param codigoBanco Código de 4 dígitos del banco
     * @param codigoSucursal Código de 4 dígitos de sucursal
     * @param moneda Moneda de la cuenta (afecta el tipo de cuenta)
     * @return Nueva CuentaId válida
     */
    public static CuentaId generarNueva(int codigoBanco, int codigoSucursal, Moneda moneda) {
        // 🎯 ESTRUCTURA: AR + Banco(4) + Sucursal(4) + Tipo(2) + Numero(10) + DV(2)
        String bancoStr = String.format("%04d", codigoBanco);
        String sucursalStr = String.format("%04d", codigoSucursal);
        String tipoCuenta = obtenerTipoCuentaPorMoneda(moneda);
        String numero = generarNumeroAleatorio();
        String sinDigitoVerificador = "AR" + bancoStr + sucursalStr + tipoCuenta + numero;
        
        String digitoVerificador = calcularDigitoVerificador(sinDigitoVerificador);
        
        return new CuentaId(sinDigitoVerificador + digitoVerificador);
    }








        @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CuentaId other = (CuentaId) obj;
        return Objects.equals(valor, other.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    }
