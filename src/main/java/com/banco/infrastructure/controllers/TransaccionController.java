package com.banco.infrastructure.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.application.dto.OperacionCuentaRequest;
import com.banco.application.dto.OperacionCuentaResponse;
import com.banco.application.dto.TransferenciaRequest;
import com.banco.application.dto.TransferenciaResponse;
import com.banco.application.services.TransaccionService;
import com.banco.domain.model.entities.Transaccion;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
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

    
    @PostMapping("transferir")
    public ResponseEntity<TransferenciaResponse> transferir(@Valid @RequestBody TransferenciaRequest request){

        try {
            
            return ResponseEntity.ok().body(transaccionService.ejecutarTransferencia(request));

        } catch (Exception e) {

            throw new IllegalArgumentException("Hubo un error al intentar transferir: " + e.getMessage());
        }
    }

    @PostMapping("/deposito")
    public ResponseEntity<OperacionCuentaResponse> depositar(@Valid @RequestBody OperacionCuentaRequest request){

        try {
            
            Transaccion transaccion = transaccionService.depositar(
                request.getCuentaId(), 
                request.getMonto(), 
                request.getMoneda(), 
                request.getDescripcion());

            
            return ResponseEntity.ok().body(new OperacionCuentaResponse(
                transaccion.getId().getValor(), 
                transaccion.getEstado().name(), 
                transaccion.getMonto().getMonto(),
                transaccion.getMonto().getMoneda().getNombre(), 
                transaccion.getFechaCreacion(), 
                transaccion.getCuentaOrigen().getValor(), 
                transaccion.getTipo().name(), "Deposito exitoso"));

        } catch (Exception e) {

            throw new IllegalArgumentException("Hubo un error al intentar depositar: " + e.getMessage());
        }
    }

    @PostMapping("/retiro")
    public ResponseEntity<OperacionCuentaResponse> retiro(@Valid @RequestBody OperacionCuentaRequest request){

        try {
            
            Transaccion transaccion = transaccionService.retirar(
                request.getCuentaId(), 
                request.getMonto(), 
                request.getMoneda(), request.getDescripcion());

            return ResponseEntity.ok().body(new OperacionCuentaResponse(
                transaccion.getId().getValor(), 
                transaccion.getEstado().name(), 
                transaccion.getMonto().getMonto(), 
                transaccion.getMonto().getMoneda().getNombre(), 
                transaccion.getFechaCreacion(), 
                transaccion.getCuentaOrigen().getValor(), 
                transaccion.getTipo().name(), "Retiro exitoso"));

        } catch (Exception e) {

            throw new IllegalArgumentException("Hubo un error al intentar retirar: " + e.getMessage());
        }
    }

    @PostMapping("/{transaccionId}/revertir")
    public ResponseEntity<OperacionCuentaResponse> revertir(@PathVariable String transaccionId){

        try {
            
            Transaccion transaccion = transaccionService.revertir(transaccionId);

            return ResponseEntity.ok().body(new OperacionCuentaResponse(
                transaccionId, 
                transaccion.getEstado().name(), 
                transaccion.getMonto().getMonto(), 
                transaccion.getMonto().getMoneda().name(), 
                transaccion.getFechaCreacion(), 
                transaccion.getCuentaOrigen().getValor(), 
                transaccion.getTipo().name(), "Reverso exitoso"));

        } catch (Exception e) {

            throw new IllegalArgumentException("Hubo un problema al intentar revertir: " +e.getMessage());
        }
    }
    
    
    
}
