package com.banco.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class TasaCambio {

    private final Moneda monedaOrigen;
    private final Moneda monedaDestino;
    private final BigDecimal tasa;

    // 🎯 CONFIGURACIÓN PARA CÁLCULOS PRECISOS
    private static final int ESCALA_TASAS = 8;
    private static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_EVEN;


    //CONSTRUCTOR PRIVADO
        private TasaCambio(Moneda monedaOrigen, Moneda monedaDestino, BigDecimal tasa) {
        // 🛡️ VALIDACIONES
        if (monedaOrigen == null || monedaDestino == null) {
            throw new IllegalArgumentException("Las monedas no pueden ser nulas");
        }
        
        if (tasa == null || tasa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tasa debe ser un número positivo: " + tasa);
        }
        
        // 🎯 No permitir tasa de misma moneda (sería 1.0 siempre)
        if (monedaOrigen == monedaDestino) {
            throw new IllegalArgumentException("No se necesita tasa para la misma moneda");
        }
        
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.tasa = tasa.setScale(ESCALA_TASAS, MODO_REDONDEO);
    }



        // METODOS DE ACCESO
    public Moneda getMonedaOrigen(){
        return monedaOrigen;
    };

    public Moneda getMonedaDestino(){
        return monedaDestino;
    }

    public BigDecimal getTasa(){
        return tasa;
    }




    //   MÉTODO FÁBRICA PRINCIPAL

        public static TasaCambio nuevaTasaCambio(Moneda monedaOrigen, Moneda monedaDestino, BigDecimal tasa) {
        return new TasaCambio(monedaOrigen, monedaDestino, tasa);
    }

    // con double
        public static TasaCambio nuevaTasaCambio(Moneda monedaOrigen, Moneda monedaDestino, double tasa) {
        return new TasaCambio(monedaOrigen, monedaDestino, BigDecimal.valueOf(tasa));
    }



       public TasaCambio inversa() {
        BigDecimal tasaInversa = BigDecimal.ONE.divide(tasa, ESCALA_TASAS, MODO_REDONDEO);
        return new TasaCambio(monedaDestino, monedaOrigen, tasaInversa);
    }

    //  VALIDAR SI APLICA PARA CONVERSIÓN
        public boolean aplicaPara(Moneda origen, Moneda destino) {
        return this.monedaOrigen == origen && this.monedaDestino == destino;
    }




        @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TasaCambio that = (TasaCambio) obj;
        return monedaOrigen == that.monedaOrigen &&
               monedaDestino == that.monedaDestino &&
               tasa.equals(that.tasa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monedaOrigen, monedaDestino, tasa);
    }

}
