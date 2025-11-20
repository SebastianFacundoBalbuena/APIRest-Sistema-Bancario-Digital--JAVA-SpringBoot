package com.banco.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


public class Dinero {

    // ATRIBUTOS 
    private final  BigDecimal monto;
    private final Moneda moneda;

    // 🎯 CONFIGURACIÓN DE ESCALA PARA CÁLCULOS
    private static final int ESCALA_CALCULO = 10;
    // METODO PARA REDONDEAR MONTOS - EJ: 1.50 -> 2.0
    private static final RoundingMode REDONDEO = RoundingMode.HALF_EVEN;

        
    // CONSTRUCTOR
    
        private Dinero(BigDecimal monto, Moneda moneda) {

            // 1- no nulo
            if(monto == null) throw new IllegalArgumentException("El monto no debe ser nulo");
            if(moneda == null) throw new IllegalArgumentException("La moneda no puede ser nula");

            // 2- verificamos que monto no sea negativo
            // Devuelve un numero entero y lo comparamos con 0
            if(monto.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El monto no puede ser negativo" + monto);

              
        // ✅ ASIGNACIÓN CON ESCALA CONSISTENTE
        // al "monto" le agrega 10 decimales, si es necesario REDONDEA 
        this.monto = monto.setScale(ESCALA_CALCULO, REDONDEO);
        this.moneda = moneda;

    }


       // METODOS DE GETTERS

    public BigDecimal getMonto(){
        return monto;
    }

    public Moneda getMoneda(){
        return moneda;
    }

    public Double getMontoDouble(){
        return monto.doubleValue();
    }

    //OBTENER MONTO CON ESCALA DE LA MONEDA
    public BigDecimal getMontoConEscalaMoneda(){
        return monto.setScale(moneda.getDecimales(), REDONDEO);
    }

    //EQUALS: Dos Dinero son iguales si mismo monto y misma moneda
        @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
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

        public static Dinero nuevo(BigDecimal monto, Moneda moneda){
            return new Dinero(monto, moneda);
        }
    
        // Desde double (para comodidad)
        public static Dinero nuevoDouble(double monto, Moneda moneda){
            // 🎯 Conversión segura de double a BigDecimal con ValueOf
            BigDecimal montoBigDecimal = BigDecimal.valueOf(monto);
            return new Dinero(montoBigDecimal, moneda);
        }

        // Desde String (para parsing seguro)
        public static Dinero nuevoString(String monto, Moneda moneda){
            // 🎯 Conversión segura de String a BigDecimal con ValueOf
            BigDecimal montoBigDecimal = new BigDecimal(monto);
            return new Dinero(montoBigDecimal, moneda);
        }

        //Cero moneda específica
        public static Dinero nuevoCero(Moneda moneda){
            return new Dinero(BigDecimal.ZERO, moneda);
        }

}
