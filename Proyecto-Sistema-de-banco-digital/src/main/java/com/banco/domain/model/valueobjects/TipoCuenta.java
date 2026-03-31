package com.banco.domain.model.valueobjects;


// Define los diferentes productos bancarios que ofrecemos.
// Cada tipo puede tener reglas diferentes (comisiones, intereses, etc.)
public enum TipoCuenta {

    AHORRO("Ahorro", 0.5,0.0),  // 📈 Interés anual 0.5%, sin comisión
    CORRIENTE("Corriente", 0.0, 10.0),  // 💼 Sin interés, comisión mensual $10
    SUELDO("Sueldo", 0.0, 0.0),  // 💰 Cuenta para depósito de sueldo
    JUVENIL("Juvenil", 1.0, 0.0),  // 👦 Cuenta para jóvenes con mejor interés
    PLAZO_FIJO("Plazo fijo", 25.0, 0.0);  // 📊 Alta rentabilidad

    // ATRIBUTOS QUE TENDRA LA LISTA DE ARRIBA POR ORDEN
    private final String descripcion;
    private final Double tasaInteresAnual; // en porcentaje
    private final Double comisionMensual;  // en pesos

    
     TipoCuenta(String descripcion, Double tasaInteresAnual, Double comisionMensual) {
        this.descripcion = descripcion;
        this.tasaInteresAnual = tasaInteresAnual;
        this.comisionMensual = comisionMensual;
    }


     public String getDescripcion() { return descripcion; }

     public Double getTasaInteresAnual() {return tasaInteresAnual;}

     public Double getComisionMensual() {return comisionMensual; }



     public static TipoCuenta fromString(String valor) {
        if (valor == null) return null;
        
        for (TipoCuenta tipo : TipoCuenta.values()) {
            if (tipo.name().equalsIgnoreCase(valor) || 
                tipo.descripcion.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("❌ Tipo de cuenta no válido: " + valor);
    }

    
}

