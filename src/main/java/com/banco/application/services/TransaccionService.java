package com.banco.application.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.banco.application.dto.MovimientoDTO;
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

import org.springframework.transaction.annotation.Transactional;

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


    public Transaccion depositar(String cuentaId, BigDecimal monto, String moneda, String descripcion){

        try {
            // convertir a objetos del dominio
            CuentaId id = CuentaId.newCuentaId(cuentaId);
            Dinero dinero = Dinero.nuevo(monto, Moneda.valueOf(moneda.toUpperCase()));

            // buscar cuenta x id
            Cuenta cuenta = cuentaRepository.buscarPorId(id).orElseThrow(()-> new IllegalArgumentException(
                "Cuenta no encontrada"));

            // crear transaccion

            Transaccion transaccion = new Transaccion(
                generarTransaccionId(),
                 TipoTransaccion.DEPOSITO, 
                 null,
                 id,
                 dinero, 
                 descripcion != null ? descripcion : "Deposito");

            cuenta.depositar(dinero);
            transaccion.completar();

            cuentaRepository.actualizar(cuenta);
            transaccionRepository.guardar(transaccion);

            System.out.println("✅ Depósito completado: " + transaccion.getId());
            return transaccion;


        } catch (Exception e) {
            throw new IllegalArgumentException("Error de deposito: " + e.getMessage());
        }
    }


        public Transaccion retirar(String cuentaId, BigDecimal monto, String moneda, String descripcion){

        try {
            // convertir a objetos del dominio
            CuentaId id = CuentaId.newCuentaId(cuentaId);
            Dinero dinero = Dinero.nuevo(monto, Moneda.valueOf(moneda.toUpperCase()));

            // buscar cuenta x id
            Cuenta cuenta = cuentaRepository.buscarPorId(id).orElseThrow(()-> new IllegalArgumentException(
                "Cuenta no encontrada"));

            // crear transaccion

            Transaccion transaccion = new Transaccion(
                generarTransaccionId(),
                 TipoTransaccion.RETIRO, 
                 id,
                 null,
                 dinero, 
                 descripcion != null ? descripcion : "Retiro");

            cuenta.retirar(dinero);
            transaccion.completar();

            cuentaRepository.actualizar(cuenta);
            transaccionRepository.guardar(transaccion);

            System.out.println("✅ Retiro completado: " + transaccion.getId());
            return transaccion;


        } catch (Exception e) {
            throw new IllegalArgumentException("Error en retiro: " + e.getMessage());
        }
    }


    public Transaccion revertir(String transaccionId){

        try {
            
                    // buscar transaccion original
        TransaccionId id = new TransaccionId(transaccionId);
        Transaccion original = transaccionRepository.buscarPorId(id).orElseThrow(()-> new IllegalArgumentException(
            "Transaccion no encontrada"));

        // validar que sea reversible
        if(!original.esReversible()) throw new IllegalArgumentException(
            "Transaccion no reversible");

        // crear transaccion reverso
        Transaccion transaccion = new Transaccion(
            generarTransaccionId(),
            TipoTransaccion.REVERSO,
            original.getCuentaOrigen(),
            original.getCuentaDestino(), 
            original.getMonto(), 
            "reverso de:" + original.getId());

        // cargar cuentas y ejecutar reversion
        if(original.getCuentaOrigen() != null && original.getCuentaDestino() != null){

            Cuenta cuentaOrigen = cuentaRepository.buscarPorId(original.getCuentaOrigen())
            .orElseThrow(()-> new IllegalArgumentException("Cuenta origen no encontrada"));

            Cuenta cuentaDestino = cuentaRepository.buscarPorId(original.getCuentaDestino())
            .orElseThrow(()-> new IllegalArgumentException(
                "Cuenta destino no encontrada"));


            // reverit segun el tipo
            if(original.getTipo() == TipoTransaccion.TRANSFERENCIA){
                cuentaDestino.transferir(original.getMonto(), cuentaOrigen);
            }
             else if(original.getTipo() == TipoTransaccion.DEPOSITO){
                cuentaDestino.retirar(original.getMonto());
            }
            else if(original.getTipo() == TipoTransaccion.RETIRO){
                cuentaOrigen.depositar(original.getMonto());
            }

            cuentaRepository.actualizar(cuentaOrigen);
            cuentaRepository.actualizar(cuentaDestino);

        }


            original.revertir();
            transaccion.completar();

            transaccionRepository.guardar(original);
            transaccionRepository.guardar(transaccion);

            System.out.println("✅ Transacción revertida: " + original.getId());
            return transaccion;

        } catch (Exception e) {
            throw new IllegalArgumentException("Error al revertir: " + e.getMessage());
        }
    }

    public List<MovimientoDTO> consultarMovimiento(String cuentaStrg){

        try {
            
            CuentaId cuenta = CuentaId.newCuentaId(cuentaStrg);
            List<Transaccion> transaccion = transaccionRepository.buscarCuentas(cuenta);

            return transaccion.stream()
            .map(this::convertirAmovimientoDto)
            .collect(Collectors.toList());

        } catch (Exception e) {

            throw new IllegalArgumentException("Hubo un error al intentar consultar movimientos");
        }
    }

    



    // METODOS AUXILIARES

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

    
    private Transaccion crearTransaccion(Cuenta cuentaOrigen, Cuenta cuentaDestino, Dinero monto, String descripcion){

        TransaccionId transaccionId = generarTransaccionId();

        return new Transaccion(transaccionId, TipoTransaccion.TRANSFERENCIA,
             cuentaOrigen.getCuentaId(), cuentaDestino.getCuentaId(), monto, descripcion);
    }

    //GENERAR ID DE TRANSACCIÓN
    private TransaccionId generarTransaccionId() {
    
    int randomNum = (int)(Math.random() * 10000000);
    String id = "TXN-" + LocalDateTime.now().getYear() + "-" + 
    String.format("%07d", randomNum);
    return new TransaccionId(id);
    }

    
    private void guardarCambios(Cuenta cuentaOrigen, Cuenta cuentaDestino, Transaccion transaccion){

        cuentaRepository.actualizar(cuentaOrigen);
        cuentaRepository.actualizar(cuentaDestino);
        transaccionRepository.guardar(transaccion);

        System.out.println(" Cambios persistidos exitosamente");

    }

    
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

    public MovimientoDTO convertirAmovimientoDto(Transaccion transaccion){

        return new MovimientoDTO(
            transaccion.getId().getValor(), 
            transaccion.getTipo().name(), 
            transaccion.getFechaCreacion(), 
            transaccion.getMonto().getMonto(), 
            transaccion.getDescripcion(), 
            transaccion.getReferencia(), 
            transaccion.getCuentaOrigen() != null ? transaccion.getCuentaOrigen().getValor() : null, 
            null);
    }







    // METODOS DE RESPUESTA
    private TransferenciaResponse respuestaExitosa(Transaccion transaccion){

        return new TransferenciaResponse(
            transaccion.getId().toString(),
             "COMPLETADA",
              transaccion.getMonto().getMonto().setScale(2), 
              transaccion.getMonto().getMoneda().toString(), 
            transaccion.getFechaCreacion(), 
            transaccion.getCuentaOrigen().getValor(), 
            transaccion.getCuentaDestino().getValor(),
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
             "Transferencia fallida. " + mensajeError);
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

        CuentaId cuentaOrigen = CuentaId.newCuentaId(request.getCuentaOrigen());
        
        return cuentaRepository.buscarPorId(cuentaOrigen).orElseThrow(()-> new IllegalArgumentException(
            "Cuenta de origen no encontrada: " + cuentaOrigen));
    }





}
