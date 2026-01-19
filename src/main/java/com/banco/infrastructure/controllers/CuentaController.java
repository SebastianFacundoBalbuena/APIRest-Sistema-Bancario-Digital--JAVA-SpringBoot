package com.banco.infrastructure.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.application.dto.AperturaCuentaRequest;
import com.banco.application.dto.AperturaCuentaResponse;
import com.banco.application.dto.ConsultaSaldoRequest;
import com.banco.application.dto.ConsultaSaldoResponse;
import com.banco.application.dto.TransferenciaRequest;
import com.banco.application.dto.TransferenciaResponse;
import com.banco.application.services.AperturaCuentaService;
import com.banco.application.services.ConsultaSaldoService;
import com.banco.application.services.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@RequestMapping("api/cuentas")
public class CuentaController {
    
    // ATRIBUTOS
    private final AperturaCuentaService aperturaCuentaService;
    private final ConsultaSaldoService consultaSaldoService;
    private final TransaccionService transaccionService;



    public CuentaController(AperturaCuentaService aperturaCuentaService, ConsultaSaldoService consultaSaldoService,
            TransaccionService transaccionService) {
        this.aperturaCuentaService = aperturaCuentaService;
        this.consultaSaldoService = consultaSaldoService;
        this.transaccionService = transaccionService;

    }


    @PostMapping()
    public ResponseEntity<AperturaCuentaResponse> abrirCuenta(@Valid @RequestBody AperturaCuentaRequest request){

        AperturaCuentaResponse response = aperturaCuentaService.ejecutarAperturaCuenta(request);

        return ResponseEntity
        .status(HttpStatus.OK)
        .body(new AperturaCuentaResponse(
            response.getCuentaId(), 
            response.getClienteId(), 
            response.getTipoCuenta(), 
            response.getMoneda(), 
            response.getSaldoInicial(), 
            response.getFechaApertura(), 
            "Cuenta creada exitosamente"));
    }

    @GetMapping()
    public ResponseEntity<ConsultaSaldoResponse> consultaSaldo(@Valid @RequestBody ConsultaSaldoRequest request){


        ConsultaSaldoResponse response = consultaSaldoService.consultarSaldo(request);

        return ResponseEntity
        .ok(response);
    }

    @PostMapping("/deposito")
    public ResponseEntity<TransferenciaResponse> depositar(@Valid @RequestBody TransferenciaRequest request){
 
        TransferenciaResponse response = transaccionService.ejecutarTransferencia(request);

        return ResponseEntity
        .ok(response);
    }

    
    
    
}
