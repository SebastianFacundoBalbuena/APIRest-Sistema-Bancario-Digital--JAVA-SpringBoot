package com.banco.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Dinero {

    // ATRIBUTOS
    private final BigDecimal monto;
    private final Moneda moneda;

    // 🎯 CONFIGURACIÓN DE ESCALA PARA CÁLCULOS
    private static final int ESCALA_CALCULO = 10;
    // METODO PARA REDONDEAR MONTOS - EJ: 1.50 -> 2.0
    private static final RoundingMode REDONDEO = RoundingMode.HALF_EVEN;

    // CONSTRUCTOR

    public Dinero(BigDecimal monto, Moneda moneda) {

        // 1- no nulo
        if (monto == null)
            throw new IllegalArgumentException("El monto no debe ser nulo");
        if (moneda == null)
            throw new IllegalArgumentException("La moneda no puede ser nula");

        // 2- verificamos que monto no sea negativo
        // Devuelve un numero entero y lo comparamos con 0
        if (monto.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El monto no puede ser negativo" + monto);

        // ✅ ASIGNACIÓN CON ESCALA CONSISTENTE
        // al "monto" le agrega 10 decimales, si es necesario REDONDEA
        this.monto = monto.setScale(ESCALA_CALCULO, REDONDEO);
        this.moneda = moneda;

    }

    // METODOS DE GETTERS

    public BigDecimal getMonto() {
        return monto;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public Double getMontoDouble() {
        return monto.doubleValue();
    }

    // OBTENER MONTO CON ESCALA DE LA MONEDA
    public BigDecimal getMontoConEscalaMoneda() {
        return monto.setScale(moneda.getDecimales(), REDONDEO);
    }

    // EQUALS: Dos Dinero son iguales si mismo monto y misma moneda
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Dinero other = (Dinero) obj;

        // 🎯 Compara montos con escala de moneda y misma moneda
        return this.getMontoConEscalaMoneda().equals(other.getMontoConEscalaMoneda()) &&
                this.moneda == other.moneda;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMontoConEscalaMoneda(), moneda);
    }





    // 🏭 MÉTODO FÁBRICA PRINCIPAL - Desde BigDecimal

    public static Dinero nuevo(BigDecimal monto, Moneda moneda) {
        return new Dinero(monto, moneda);
    }

    // Desde double (para comodidad)
    public static Dinero nuevoDouble(double monto, Moneda moneda) {
        // 🎯 Conversión segura de double a BigDecimal con ValueOf
        BigDecimal montoBigDecimal = BigDecimal.valueOf(monto);
        return new Dinero(montoBigDecimal, moneda);
    }

    // Desde String (para parsing seguro)
    public static Dinero nuevoString(String monto, Moneda moneda) {
        // 🎯 Conversión segura de String a BigDecimal con ValueOf
        BigDecimal montoBigDecimal = new BigDecimal(monto);
        return new Dinero(montoBigDecimal, moneda);
    }

    // Cero moneda específica
    public static Dinero nuevoCero(Moneda moneda) {
        return new Dinero(BigDecimal.ZERO, moneda);
    }






    // METODOS - OPERACIONES MATEMATICAS SEGURAS

    public Dinero sumar(Dinero otro) {
        // son compatibles?
        if (!otro.moneda.esCompatibleCon(moneda))
            throw new IllegalArgumentException(
                    "Moneda no compatible");

        BigDecimal resultado = this.monto.add(otro.monto);
        return new Dinero(resultado, this.moneda);
    }

    public Dinero restar(Dinero otro) {
        // es compatible?
        if (!otro.moneda.esCompatibleCon(moneda))
            throw new IllegalArgumentException(
                    "Moneda no compatible");

        BigDecimal resultado = this.monto.subtract(otro.monto);
        // Devuelve un numero entero y lo comparamos con 0
        if (resultado.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalStateException(
                    "Fondos insuficientes:" + this + "-" + otro + "=" + resultado);

        return new Dinero(resultado, this.moneda);
    }

    public Dinero multiplicar(BigDecimal multiplicador) {
        // Multiplicador no negativo
        if (multiplicador.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El multiplicador no puede ser negativo: " + multiplicador);
        }

        BigDecimal resultado = this.monto.multiply(multiplicador);
        return new Dinero(resultado, this.moneda);
    }

    // MULTIPLICAR DON DOUBLE
    public Dinero multiplicar(double multiplicador) {
        return multiplicar(BigDecimal.valueOf(multiplicador));
    }

    public Dinero dividir(BigDecimal divisor) {
        // Divisor positivo
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El divisor debe ser positivo: " + divisor);
        }

        // OPERACIÓN CON ESCALA Y REDONDEO
        BigDecimal resultado = this.monto.divide(divisor, ESCALA_CALCULO, REDONDEO);
        return new Dinero(resultado, this.moneda);
    }

    // Dividir double
    public Dinero dividir(double divisor) {
        return dividir(BigDecimal.valueOf(divisor));
    }






    // OPERACIONES DE COMPARACIÓN
    private void validarMismasMoneda(Dinero otro) {
        if (!this.moneda.esCompatibleCon(otro.moneda)) {
            throw new IllegalArgumentException(
                    "No se pueden comparar " + this.moneda + " con " + otro.moneda);
        }
    }

    public boolean esMayorQue(Dinero otro) {
        validarMismasMoneda(otro);
        return this.monto.compareTo(otro.monto) > 0;
    }

    public boolean esMayorOIgualQue(Dinero otro) {
        validarMismasMoneda(otro);
        return this.monto.compareTo(otro.monto) >= 0;
    }

    public boolean esMenorQue(Dinero otro) {
        validarMismasMoneda(otro);
        return this.monto.compareTo(otro.monto) < 0;
    }

    public boolean esMenorOIgualQue(Dinero otro) {
        validarMismasMoneda(otro);
        return this.monto.compareTo(otro.monto) <= 0;
    }

    public boolean esCero() {
        return this.monto.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean esPositivo() {
        return this.monto.compareTo(BigDecimal.ZERO) > 0;
    }




    // METODO DE CONVERSION

    // REDONDEO
        private BigDecimal redondearSegunMoneda(BigDecimal monto, Moneda moneda) {
        if (moneda.requiereRedondeoEfectivo()) {
            // 🇦🇷 ARGENTINA: Redondeo al entero más cercano para conversiones
            return monto.setScale(0, REDONDEO);
        } else {
            //  OTRAS MONEDAS: Usar sus decimales configurados
            return monto.setScale(moneda.getDecimales(),REDONDEO);
        }
    }



        public Dinero convertir(TasaCambio tasa) {
        //  VALIDACIÓN: La tasa debe aplicar para esta conversión
        if (!tasa.aplicaPara(this.moneda, tasa.getMonedaDestino())) {
            throw new IllegalArgumentException(
                "Tasa de cambio no aplica para conversión: " + this.moneda + 
                "→" + tasa.getMonedaDestino() + ". Tasa es: " + tasa
            );
        }
        
        // CÁLCULO DE CONVERSIÓN
        BigDecimal montoConvertido = this.monto.multiply(tasa.getTasa());
        
        // REDONDEO SEGÚN MONEDA DESTINO
        Moneda monedaDestino = tasa.getMonedaDestino();
        BigDecimal montoRedondeado = redondearSegunMoneda(montoConvertido, monedaDestino);
        
        return new Dinero(montoRedondeado, monedaDestino);
    }


        public Dinero convertirA(Moneda monedaDestino, TasaCambio tasa) {
        // 🛡️ VALIDACIÓN EXPLÍCITA de dirección
        if (tasa.getMonedaOrigen() != this.moneda || tasa.getMonedaDestino() != monedaDestino) {
            throw new IllegalArgumentException(
                "Tasa de cambio incorrecta. Se esperaba " + this.moneda + "→" + monedaDestino +
                " pero se recibió: " + tasa
            );
        }
        
        return convertir(tasa);
    }

        public boolean necesitaConversion(Moneda otraMoneda) {
        return !this.moneda.esCompatibleCon(otraMoneda);
    }
}
