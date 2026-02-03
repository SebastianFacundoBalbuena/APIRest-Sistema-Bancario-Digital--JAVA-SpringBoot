package com.banco.domain.model.valueobjects;


import java.util.Objects;
import java.util.regex.Pattern;

public final class CuentaId {

    //FORMATO: Código país + 22 dígitos (similar IBAN argentino)
    private static final String FORMATO = "ARG\\d{22}";
    // VALIDA QUE EL FORMATO SEA EL CORRECTO
    private static final Pattern VALIDAR_FORMATO = Pattern.compile(FORMATO);

    private final String valor;

    //CONSTRUCTOR PRIVADO - Validación completa
    private CuentaId(String valor) {

        // VALIDACIONES EN CONSTRUCTOR

        
        if (valor == null) {
            throw new IllegalArgumentException("No se permite nulos");
        }

        //Cumple el formato
        if (!VALIDAR_FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException(String.format("El formato no es el correcto. Debe ser %s",
                    valor, FORMATO));
        }

        //Dígito verificador
        if (!validarDigitoVerificador(valor)) {
            throw new IllegalArgumentException("Dígito verificador de cuenta inválido");
        }

        //Banco valido
        if (!esBancoValido(valor)) {
            throw new IllegalArgumentException("Código de banco no válido");
        }


  
        this.valor = valor;

    }

    // METODOS DE LA CLASE

    // MÉTODO FÁBRICA PRINCIPAL
    public static CuentaId newCuentaId(String valor) {
        return new CuentaId(valor);
    }

    // GETTERS
    public String getValor() {
        return valor;
    }

    public String getCodigoBanco() {
        // Ejemplo: "AR0290000000000000000000" → Banco "0290"
        return valor.substring(3, 6);
    }

    public String getCodigoSucursal() {
        // Ejemplo: "AR0290123400000000000000" → Sucursal "1234"
        return valor.substring(6, 10);
    }

    public String getTipoDeCuenta() {
        // Ejemplo: "00" → Cuenta en pesos - "01" → Caja de ahorro en dólares - "02" 
        return valor.substring(10, 12);
    }

    public static String getFormatoEsperado() {
        return FORMATO;
    }

    
     //OBTENER NÚMERO DE CUENTA LEGIBLE
     //Formato: Banco-Sucursal-Tipo-Número
     //Ejemplo: "0290-1234-00-0000001234"
     
    public String getNumeroLegible() {
        return String.format("%s-%s-%s-%s", getCodigoBanco(), getCodigoSucursal(),
                getTipoDeCuenta(), valor.substring(12, 22)); // ultimos 10 digitos
    }



    // METODOS COMPLEJOS

    
     //REGLA DE NEGOCIO: Deducir Moneda desde el tipo
     //Tipos 00-09 → Pesos Argentinos (ARS)
     //Tipos 10-19 → Dólares (USD)
     //Tipos 20-29 → Euros (EUR)
     
    public Moneda deducirMoneda() {
        int tipo = Integer.parseInt(getTipoDeCuenta());

        if (tipo >= 0 && tipo <= 9) {
            return Moneda.ARG;
        } else if (tipo >= 10 && tipo <= 19) {
            return Moneda.USD;
        } else if (tipo >= 20 && tipo <= 29) {
            return Moneda.EUR;
        } else {
            throw new IllegalStateException("Tipo de cuenta no reconocida" + tipo);
        }
    }


    public boolean esCuentaEnPesos() {
        return deducirMoneda() == Moneda.ARG;
    }


    
     //CALCULAR DÍGITO VERIFICADOR
     
    private static String calcularDigitoVerificador(String valorSinDigito) {

        // Para pruebas, devolver "00" o un valor fijo
        return "00";
    }


    private static boolean validarDigitoVerificador(String valor) {

        //PARA PRUEBAS: Aceptar siempre
        // Más adelante implementaremos un algoritmo real

        System.out.println("✅ Dígito verificador aceptado (modo pruebas)");
        return true;
    }

    
     //VALIDAR CÓDIGO DE BANCO
     //En sistema real, verificaría contra base de bancos autorizados
     
    private static boolean esBancoValido(String valor) {
        String codigoBanco = valor.substring(3, 6);
        // Ejemplo: solo permitimos algunos bancos
        return codigoBanco.equals("017") || // Banco de ejemplo
                codigoBanco.equals("015") || // Otro banco
                codigoBanco.equals("072"); // Otro banco
    }

    // DETERMINAR TIPO DE CUENTA SEGÚN MONEDA
    private static String obtenerTipoCuentaPorMoneda(Moneda moneda) {
        switch (moneda) {
            case ARG:
                return "00"; // Cuenta pesos argentinos
            case USD:
                return "10"; // Caja ahorro dólares
            case EUR:
                return "20"; // Caja ahorro euros
            default:
                throw new IllegalArgumentException("Moneda no soportada: " + moneda);
        }
    }

    //GENERAR NÚMERO ALEATORIO DE CUENTA
    private static String generarNumeroAleatorio() {
        long numero = (long) (Math.random() * 10_000_000_000L);
        return String.format("%011d", numero);
    }



    
    //MÉTODO FÁBRICA PARA CREAR NUEVAS CUENTAS
    //Genera cuenta con formato válido para nuevo cliente
     
    //param codigoBanco    Código de 4 dígitos del banco
    //param codigoSucursal Código de 4 dígitos de sucursal
    //param moneda         Moneda de la cuenta (afecta el tipo de cuenta)
    //return Nueva CuentaId válida
     
    public static CuentaId generarNueva(int codigoBanco, int codigoSucursal, Moneda moneda) {
        //  ESTRUCTURA: ARG + Banco(3) + Sucursal(4) + Tipo(2) + Numero(10) + DV(2)
        String bancoStr = String.format("%03d", codigoBanco);
        String sucursalStr = String.format("%04d", codigoSucursal);
        String tipoCuenta = obtenerTipoCuentaPorMoneda(moneda);
        String numero = generarNumeroAleatorio();
        String sinDigitoVerificador = "ARG" + bancoStr + sucursalStr + tipoCuenta + numero;

        String digitoVerificador = calcularDigitoVerificador(sinDigitoVerificador);

        return new CuentaId(sinDigitoVerificador + digitoVerificador);
    }




    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        CuentaId other = (CuentaId) obj;
        return Objects.equals(valor, other.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

}
