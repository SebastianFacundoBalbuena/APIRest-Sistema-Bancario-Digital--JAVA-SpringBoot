package com.banco.infrastructure.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.application.dto.MovimientoDTO;
import com.banco.application.dto.OperacionCuentaRequest;
import com.banco.application.dto.OperacionCuentaResponse;
import com.banco.application.dto.TransferenciaRequest;
import com.banco.application.dto.TransferenciaResponse;
import com.banco.application.services.TransaccionService;


import jakarta.validation.Valid;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;







@RestController
@RequestMapping("api/transacciones")
public class TransaccionController {
    
    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    
    @PostMapping("/transferir")
    public ResponseEntity<TransferenciaResponse> transferir(@Valid @RequestBody TransferenciaRequest request){
  
        TransferenciaResponse response = transaccionService.ejecutarTransferencia(request);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/deposito")
    public ResponseEntity<OperacionCuentaResponse> depositar(@Valid @RequestBody OperacionCuentaRequest request){
   
            OperacionCuentaResponse response = transaccionService.depositar(request);

            
            return ResponseEntity.ok().body(response);

    }

    @PostMapping("/retiro")
    public ResponseEntity<OperacionCuentaResponse> retiro(@Valid @RequestBody OperacionCuentaRequest request){

            
            OperacionCuentaResponse response = transaccionService.retirar(request);

            return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{transaccionId}/revertir")
    public ResponseEntity<OperacionCuentaResponse> revertir(@PathVariable String transaccionId){

            
            OperacionCuentaResponse response = transaccionService.revertir(transaccionId);

            return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{cuentaStringId}/movimientos")
    public ResponseEntity<?> obtenerMovimientos(@PathVariable String cuentaStringId){


            List<MovimientoDTO> movimiento = transaccionService.consultarMovimiento(cuentaStringId);

            return ResponseEntity.ok().body(movimiento);
            
    }
    
    
    
}
