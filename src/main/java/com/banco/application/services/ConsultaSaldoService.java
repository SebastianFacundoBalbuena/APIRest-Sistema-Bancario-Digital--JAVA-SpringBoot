package com.banco.application.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import com.banco.application.dto.ConsultaSaldoRequest;
import com.banco.application.dto.ConsultaSaldoResponse;
import com.banco.application.dto.MovimientoDTO;
import com.banco.application.port.out.CuentaRepository;
import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;

@Service
@Transactional(readOnly = true)  // Solo lectura 
public class ConsultaSaldoService {

    
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    // Configuracion
    private static final int MOVIMIENTOS_MAXIMO = 100;



    // Constructor de INYECCION
    public ConsultaSaldoService(CuentaRepository cuentaRepository, TransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }



    // METODO PRINCIPAL
    public ConsultaSaldoResponse consultarSaldo(ConsultaSaldoRequest request) {

        
        try {
            // 1️ VALIDACIONES BÁSICAS
            validarRequest(request);
            
            // 2️ CARGAR CUENTA
            Cuenta cuenta = cargarCuenta(request.getCuentaId());
            
            // 3️ CONSTRUIR RESPUESTA BASE
            ConsultaSaldoResponse response = construirRespuestaBase(cuenta, request);
            
            // 4️ PROCESAR MOVIMIENTOS (si se solicitan)
            if (request.isIncluirMovimientos()) {
                procesarMovimientos(cuenta, request, response);
            }
            
            // 5️ AGREGAR INFORMACIÓN DE ESTADO
            agregarInformacionEstado(cuenta, response);
            
            // 6️CALCULAR SALDO DISPONIBLE
            calcularSaldosDisponibles(cuenta, response);
            
            // 7️ MENSAJE FINAL
            completarMensaje(response);
            
            System.out.println("Consulta completada para cuenta: " + request.getCuentaId());
            return response;
            
        } catch (Exception e) {

            System.err.println("Error en consulta de saldo: " + e.getMessage());

            return crearRespuestaError(request != null ? request.getCuentaId() : "DESCONOCIDA", e.getMessage());
        }
    }






    //METODOS COMPLEMENTARIOS

    private Cuenta cargarCuenta(String cuentaIdString){

        CuentaId cuentaId = CuentaId.newCuentaId(cuentaIdString);

        Cuenta cuenta = cuentaRepository.buscarPorId(cuentaId).orElseThrow(()-> new IllegalArgumentException(
            "Cuenta no encontrada: " + cuentaId));

            return cuenta;
    }

    private ConsultaSaldoResponse construirRespuestaBase(Cuenta cuenta, ConsultaSaldoRequest request){

        ConsultaSaldoResponse response = new ConsultaSaldoResponse(
            cuenta.getCuentaId().getValor(),
            cuenta.getClienteId().getValor(),
            "CORRIENTE",  
            cuenta.getMoneda().name(),
            cuenta.getSaldo().getMontoConEscalaMoneda(),
            "Consulta de saldo realizada exitosamente"
        );
        
        response.setFechaConsulta(LocalDateTime.now());
        return response;
    }

    private LocalDateTime prepararFechaDesde(ConsultaSaldoRequest request) {
        // ¿El usuario proporcionó fecha?
        if (request.getFechaDesde() != null) {
            return request.getFechaDesde().atStartOfDay();// Sí → Convertir LocalDate a LocalDateTime al inicio del día
        }
        // Por defecto: últimos 30 días
        return LocalDateTime.now().minusDays(30); // No → Usar valor por defecto: hoy menos 30 días
    }

    private LocalDateTime prepararFechaHasta(ConsultaSaldoRequest request) {
        // ¿El usuario proporcionó fecha?
        if (request.getFechaHasta() != null) {
            return request.getFechaHasta().atTime(LocalTime.MAX); // Sí → Convertir LocalDate a LocalDateTime al final del día
        }
        return LocalDateTime.now(); // No -> utiliza la fecha y hora actual
    }

    private void calcularTotalesPeriodo(List<Transaccion> transacciones, ConsultaSaldoResponse response) {
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
        
        for (Transaccion transaccion : transacciones) {
            //  Esta lógica simplificada asume que todas las transacciones
            // son de la misma moneda que la cuenta
            BigDecimal monto = transaccion.getMonto().getMonto();
            
            //  DETERMINAR SI ES INGRESO O EGRESO PARA ESTA CUENTA
            if (transaccion.getTipo() == TipoTransaccion.DEPOSITO) {
                totalIngresos = totalIngresos.add(monto);
            } else if (transaccion.getTipo() == TipoTransaccion.RETIRO) {
                totalEgresos = totalEgresos.add(monto);
            }
        }
        
        response.setTotalIngresos(totalIngresos);
        response.setTotalEgresos(totalEgresos);
    }

    private String determinarCuentaContraparte(Transaccion transaccion) {
        //  SIMPLIFICACIÓN: En una implementación real, necesitaríamos
        // más lógica para determinar qué cuenta mostrar como contraparte
        switch (transaccion.getTipo()) {
            case TRANSFERENCIA:
                return "Transferencia entre cuentas";
            case DEPOSITO:
                return "Depósito externo";
            case RETIRO:
                return "Retiro en cajero";
            default:
                return transaccion.getDescripcion();
        }
    }

    // AJUSTAR SALDO PARA TRANSACCIÓN ANTERIOR (simulación)
    private BigDecimal ajustarSaldoParaTransaccionAnterior(BigDecimal saldoActual, Transaccion transaccion) {
        // Esta es una simplificación. En producción, necesitaríamos
        // llevar un registro del saldo después de cada transacción
        BigDecimal monto = transaccion.getMonto().getMonto();
        
        switch (transaccion.getTipo()) {
            case DEPOSITO:
                return saldoActual.subtract(monto); // Antes del depósito había menos
            case RETIRO:
                return saldoActual.add(monto); // Antes del retiro había más
            default:
                return saldoActual;
        }
    }

    private MovimientoDTO convertirTransaccionAMovimiento(Transaccion transaccion,  BigDecimal saldoPosterior) {
        
        String cuentaContraparte = determinarCuentaContraparte(transaccion);
        
        return new MovimientoDTO(
            transaccion.getId().getValor(),
            transaccion.getTipo().toString(),
            transaccion.getFechaCreacion(),
            transaccion.getMonto().getMonto(),
            transaccion.getDescripcion(),
            transaccion.getReferencia(),
            cuentaContraparte,
            saldoPosterior
        );
    }

    private void convertirATransaccionesDTO(List<Transaccion> transacciones,Cuenta cuenta, ConsultaSaldoResponse response) {
        
        BigDecimal saldoAcumulado = cuenta.getSaldo().getMonto();
        
        //  Iterar en orden inverso (más antiguo a más reciente) para calcular saldo posterior
        for (int i = transacciones.size() - 1; i >= 0; i--) {
            Transaccion transaccion = transacciones.get(i);
            MovimientoDTO movimiento = convertirTransaccionAMovimiento(transaccion, saldoAcumulado);
            response.agregarMovimiento(movimiento);
            
            //  AJUSTAR SALDO ACUMULADO (simulación inversa)
            // En realidad necesitaríamos el saldo antes de cada transacción
            // Para simplificar, usamos una aproximación
            saldoAcumulado = ajustarSaldoParaTransaccionAnterior(saldoAcumulado, transaccion);
        }
        
        //  INVERTIR PARA MOSTRAR MÁS RECIENTES PRIMERO
        Collections.reverse(response.getMovimientos());
    }

    private void procesarMovimientos(Cuenta cuenta, ConsultaSaldoRequest request,  ConsultaSaldoResponse response) {
        
        // 1️PREPARAR RANGO DE FECHAS
        LocalDateTime fechaDesde = prepararFechaDesde(request);
        LocalDateTime fechaHasta = prepararFechaHasta(request);
        
        // 2️OBTENER TRANSACCIONES DEL REPOSITORIO
        List<Transaccion> transacciones = transaccionRepository.buscarPorCuenta(
            cuenta, 
            fechaDesde, 
            fechaHasta
        );
        
        // 3️ LIMITAR Y ORDENAR
        List<Transaccion> transaccionesLimitadas = transacciones.stream()
            .sorted(Comparator.comparing(Transaccion::getFechaCreacion).reversed()) //  Más recientes primero
            .limit(request.getLimiteMovimientos())
            .collect(Collectors.toList());
        
        // 4️CALCULAR TOTALES DEL PERIODO
        calcularTotalesPeriodo(transacciones, response);
        
        // 5️CONVERTIR A DTOs
        convertirATransaccionesDTO(transaccionesLimitadas, cuenta, response);
        
        // 6️INDICAR SI HAY MÁS MOVIMIENTOS
        response.setTieneMasMovimientos(transacciones.size() > request.getLimiteMovimientos());
        
        System.out.println( transaccionesLimitadas.size() + " movimientos procesados");
    }

    private void agregarInformacionEstado(Cuenta cuenta, ConsultaSaldoResponse response) {
        //  ESTADO DE LA CUENTA
        response.setEstadoCuenta(cuenta.getActiva() ? "ACTIVA" : "INACTIVA");
        
        //  RESTRICCIONES
        if (!cuenta.getActiva()) {
            response.agregarRestriccion("Cuenta inactiva - No puede realizar operaciones");
        }
        
        //  VALIDAR SALDO BAJO
        BigDecimal saldo = cuenta.getSaldo().getMonto();
        BigDecimal umbralMinimo = new BigDecimal("1000.00"); //  Ejemplo: $1000 mínimo recomendado
        
        if (saldo.compareTo(umbralMinimo) < 0) {
            response.agregarRestriccion("Saldo bajo - Mantenga al menos $" + umbralMinimo);
        }
        
        //  LÍMITE DE SOBREGIRO (ejemplo fijo)
        response.setLimiteSobregiro(new BigDecimal("5000.00")); //  $5000 de sobregiro permitido
        
        System.out.println(" Información de estado agregada");
    }

    private void calcularSaldosDisponibles(Cuenta cuenta, ConsultaSaldoResponse response) {
        BigDecimal saldoActual = cuenta.getSaldo().getMonto();
        BigDecimal limiteSobregiro = response.getLimiteSobregiro() != null ? response.getLimiteSobregiro() : BigDecimal.ZERO;
        
        //  SALDO DISPONIBLE = Saldo actual + Límite de sobregiro
        BigDecimal saldoDisponible = saldoActual.add(limiteSobregiro);
        
        response.setSaldoDisponible(saldoDisponible);
        
        // Podríamos restar retenciones, cheques en proceso, etc.
        System.out.println(" Saldos calculados: Actual=" + saldoActual +  ", Disponible=" + saldoDisponible);
    }

    private void completarMensaje(ConsultaSaldoResponse response) {
        String mensajeBase = response.getMensaje();
        String saldoFormateado = String.format("$%,.2f", response.getSaldoActual());
        
        String mensajeCompleto = String.format("%s Saldo actual: %s %s. %s movimientos recientes.",
            mensajeBase,
            saldoFormateado,
            response.getMoneda(),
            response.getMovimientos().size()
        );
        
        response.setMensaje(mensajeCompleto);
    }

    private ConsultaSaldoResponse crearRespuestaError(String cuentaId, String error) {
        ConsultaSaldoResponse response = new ConsultaSaldoResponse(
            cuentaId,
            null,
            null,
            null,
            BigDecimal.ZERO,
            "Error en consulta: " + error
        );
        response.setEstadoCuenta("ERROR");
        response.agregarRestriccion("Consulta fallida - " + error);
        return response;
    }






    // VALIDACIONES

    private void validarRequest(ConsultaSaldoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        
        if (request.getCuentaId() == null || request.getCuentaId().trim().isEmpty()) {
            throw new IllegalArgumentException(" Se requiere ID de cuenta");
        }
        
        // VALIDAR LÍMITES
        if (request.getLimiteMovimientos() > MOVIMIENTOS_MAXIMO) {
            throw new IllegalArgumentException("Límite de movimientos excedido. Máximo: " + MOVIMIENTOS_MAXIMO);
        }
        
        // VALIDAR RANGO DE FECHAS
        if (request.getFechaDesde() != null && request.getFechaHasta() != null) {
            if (request.getFechaDesde().isAfter(request.getFechaHasta())) {
                throw new IllegalArgumentException("Fecha desde debe ser anterior a fecha hasta");
            }
            
            //  LIMITAR CONSULTAS A MÁXIMO 1 AÑO
            LocalDate maxFecha = request.getFechaDesde().plusYears(1);
            if (request.getFechaHasta().isAfter(maxFecha)) {
                throw new IllegalArgumentException(" El rango máximo de consulta es 1 año");
            }
        }

        System.out.println(" Validaciones de request OK");
    }


}
