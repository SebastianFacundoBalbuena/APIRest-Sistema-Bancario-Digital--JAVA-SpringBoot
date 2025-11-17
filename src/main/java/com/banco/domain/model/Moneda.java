package com.banco.domain.model;

// * VALUE OBJECT: Moneda

public enum Moneda {

    // - false: EUR, USD → siempre usan decimales (efectivo y digital)
    //- true: ARS → digital con decimales, físico sin decimales  
    EUR("Euro","€", 2, false),
    USD("Dolar", "$",2, false),
    ARG("Peso Argentino","$",2, true);

    // ATRIBUTOS
    private final String nombre;
    private final String simbolo;
    private final int decimales;
    private final boolean redondeoEfectivo;

    Moneda(String nombre, String simbolo, int decimales, boolean redondeoEfectivo){
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.decimales = decimales;
        this.redondeoEfectivo = redondeoEfectivo;
    }

        // 📖 MÉTODOS DE ACCESO (solo lecturas → inmutabilidad)

    public String getNombre() {
        return nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public int getDecimales() {
        return decimales;
    }

    //Indica si la moneda requiere redondeo para efectivo
    public boolean requiereRedondeoEfectivo(){
        return redondeoEfectivo;
    }

    // 💡 MÉTODOS DE NEGOCIO (comportamiento rico)

    //     * Verifica si la moneda permite operaciones con decimales
    public boolean permiteDecimales(){
        return decimales > 0;
    }

    //🎯 REGLA DE NEGOCIO CRÍTICA: Compatibilidad entre monedas
     // Solo permitimos operaciones entre la misma moneda
     public boolean esCompatibleCon(Moneda otraMoneda){
        return this == otraMoneda;
     }

     //🎨 FORMATEO PARA TRANSACCIONES DIGITALES
     public String formatear(Double cantidad){
        if(permiteDecimales()){
            return String.format("%s %.2f %s", simbolo, cantidad, nombre);
        }
        else{
            return String.format("%s %.0f %s", simbolo, cantidad, nombre);
        }
    }


        //🆕 FORMATEO ESPECIAL PARA TRANSACCIONES FÍSICAS EN ARGENTINA
        public String formatearParaEfectivo(double cantidad){
            if(redondeoEfectivo){
                // 🇦🇷 ARGENTINA: Redondea para billetes/monedas físicas
                double redondeo = Math.round(cantidad);
                return String.format("%s %.0f %s (efectivo)", simbolo, redondeo, nombre);
            }
            else{
                 // 🇪🇺🇺🇸 EUROPA/EEUU: Siempre con decimales
                return formatear(cantidad);
                
            }
        }
        
            //🧮 REDONDEO PARA OPERACIONES FÍSICAS
            public double redondearEfectivo(double cantidad){
                if(redondeoEfectivo){
                    return Math.round(cantidad); // Argentina: redondea
                }
                else{
                    return cantidad;  // Otros: mantiene decimales
                }
            }

            //✅ VALIDACIÓN DE MONTO VÁLIDO
            public boolean esValido(double monto){

                if(monto < 0){
                    return false; // no permitimos montos negativos
                }
                if(redondeoEfectivo){
                     // En Argentina, para efectivo validamos que sea "redondeable"
                     return true;
                }

                return true; // Para otras monedas, cualquier monto positivo es válido

            }
     }
    

    
