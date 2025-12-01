package com.banco.application.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.banco.application.dto.TransferenciaRequest;
import com.banco.application.dto.TransferenciaResponse;
import com.banco.application.port.out.CuentaRepository;
import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;

import jakarta.transaction.Transactional;

//  Orquesta toda la operación de transferir dinero entre cuentas.
// EJECUTAMOS LA LOGICA DEL DOMINIO

@Service
@Transactional
public class TransaccionService {

    // INYECCION DE DEPENDENCIAS
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    // CONSTRUCTOR
    public TransaccionService(CuentaRepository cuentaRepository, TransaccionRepository transaccionRepository){

        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;

        System.out.println("TransferenciaService inicializado");
    }


    // EJECUTAR TRANSFERENCIA - Método principal

    public TransferenciaResponse ejecutarTransferencia(TransferenciaRequest request){

        try {
            
            validarRequest(request);

            // CARGAR ENTIDADES
            Cuenta cuentaOrigen = cargarCuentaOrigen(request);
            Cuenta cuentaDestino = cargarCuentaDestino(request);

            // CREAR MONTO
            Dinero monto = crearMonto(request);

            //EJECUTAR TRANSFERENCIA EN EL DOMINIO
            return ejecutarEnDominio(cuentaOrigen, cuentaDestino, monto, request.getDescripcion());

        } catch (Exception e) {

            throw new IllegalArgumentException("Hubo un error: " + e.getMessage());
        }
    }



    // METODOS DE ORQUESTACION

    private TransferenciaResponse ejecutarEnDominio(Cuenta cuentaOrigen, Cuenta cuentaDestino, Dinero monto, String descripcion){

         // 1️⃣ 📝 CREAR TRANSACCIÓN (estado PENDIENTE)
         Transaccion transaccion = crearTransaccion(cuentaOrigen, cuentaDestino, monto, descripcion);

         try {
            
            //ENTIDAD CUENTA HACE EL TRABAJO
            cuentaOrigen.transferir(monto, cuentaDestino);
            // MARCAR COMO COMPLETADA
            transaccion.completar();
            //GUARDAR CAMBIOS
            guardarCambios(cuentaOrigen, cuentaDestino, transaccion);

            return respuestaExitosa(transaccion);

         } catch (Exception e) {
            transaccion.rechazar(e.getMessage());
            transaccionRepository.guardar(transaccion);
            
            return respuestaErronea(e.getMessage());
         }

    }

    //CREAR TRANSACCIÓN
    private Transaccion crearTransaccion(Cuenta cuentaOrigen, Cuenta cuentaDestino, Dinero monto, String descripcion){

        TransaccionId transaccionId = generarTransaccionId();

        return new Transaccion(transaccionId, TipoTransaccion.TRANSFERENCIA,
             cuentaOrigen.getCuentaId(), cuentaDestino.getCuentaId(), monto, descripcion);
    }

    //GENERAR ID DE TRANSACCIÓN
    private TransaccionId generarTransaccionId(){
        String id = "TXN-" + LocalDateTime.now().getYear() + "-" + String.format("%07d", (int)Math.random() * 10000000);
        return new TransaccionId(id);
    }

    // GUARDAR TODOS LOS CAMBIOS
    private void guardarCambios(Cuenta cuentaOrigen, Cuenta cuentaDestino, Transaccion transaccion){

        cuentaRepository.actualizar(cuentaOrigen);
        cuentaRepository.actualizar(cuentaDestino);
        transaccionRepository.guardar(transaccion);

        System.out.println(" Cambios persistidos exitosamente");

    }

        //CREAR OBJETO DINERO
    private Dinero crearMonto(TransferenciaRequest request) {
      try {
        // Convierte a mayúsculas por si acaso
        String codigoMoneda = request.getMoneda().toUpperCase();
        Moneda moneda = Moneda.valueOf(codigoMoneda);
        return new Dinero(request.getMonto(), moneda);

    } catch (IllegalArgumentException e) {

        throw new IllegalArgumentException(
            "Moneda '" + request.getMoneda() + "' no válida. " +
            "Use: EUR, USD o ARG"
        );
    }
    }

    // METODOS DE RESPUESTA
    private TransferenciaResponse respuestaExitosa(Transaccion transaccion){

        return new TransferenciaResponse(
            transaccion.getId().toString(),
             "COMPLETADA",
              transaccion.getMonto().getMonto(), 
              transaccion.getMonto().getMoneda().toString(), 
            transaccion.getFechaCreacion(), 
            transaccion.getCuentaOrigen().toString(), 
            transaccion.getCuentaDestino().toString(),
             "Transaccion realizada exitosamente");
    }

    private TransferenciaResponse respuestaErronea(String mensajeError){

        return new TransferenciaResponse(
            null, 
            "RECHAZADA", 
            BigDecimal.ZERO, 
            null, 
            LocalDateTime.now(), 
            null, 
            null,
             "Transferencia fallida " + mensajeError);
    }





    // VALIDACIONES BASICAS
    private void validarRequest(TransferenciaRequest request){

        if(request == null) throw new IllegalArgumentException("La solicitud no puede ser nula");

        if(request.getMonto() == null && request.getMonto().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(
            "El monto debe ser positivo");
        
        if(request.getCuentaOrigen() == null || request.getCuentaDestino() == null) throw new IllegalArgumentException(
            "Se requieren ambas cuentas para la operacion");

        if(request.getCuentaOrigen().equals(request.getCuentaDestino())) throw new IllegalArgumentException(
            "No se puede transferir a la misma cuenta");

            System.out.println("Validaciones de request OK");
    }

    // CARGAR CUENTA DESTINO
    public Cuenta cargarCuentaDestino(TransferenciaRequest request){

        CuentaId cuentaDestino = CuentaId.newCuentaId(request.getCuentaDestino());
        
        return cuentaRepository.buscarPorId(cuentaDestino).orElseThrow(()-> new IllegalArgumentException(
            "Cuenta de destino no encontrada: " + cuentaDestino));
    }

    // CARGAR CUENTA ORIGEN
        public Cuenta cargarCuentaOrigen(TransferenciaRequest request){

        CuentaId cuentaOrigen = CuentaId.newCuentaId(request.getCuentaDestino());
        
        return cuentaRepository.buscarPorId(cuentaOrigen).orElseThrow(()-> new IllegalArgumentException(
            "Cuenta de destino no encontrada: " + cuentaOrigen));
    }





}
