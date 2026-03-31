package com.banco.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CierreCuentaRequest {

    // ATRIBUTOS

    @NotBlank(message = "La razon del cierre es obligatorio")
    @Size(min = 5, max = 200, message = "La razon debe tener entre 5 y 200 caracteres")
    private String razon;

    @NotBlank(message = "La cuenta de destino para saldo remanente es obligatorio")
    private String cuentaDestinoSaldo;



    public String getRazon() { return razon;  }
    public void setRazon(String razon) {this.razon = razon;}

    public String getCuentaDestinoSaldo() { return cuentaDestinoSaldo; }
    public void setCuentaDestinoSaldo(String cuentaDestinoSaldo) { this.cuentaDestinoSaldo = cuentaDestinoSaldo; }


    
}
