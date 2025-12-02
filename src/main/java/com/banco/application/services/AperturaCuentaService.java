package com.banco.application.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.banco.application.dto.AperturaCuentaRequest;
import com.banco.application.dto.AperturaCuentaResponse;
import com.banco.application.port.out.ClienteRepository;
import com.banco.application.port.out.CuentaRepository;
import com.banco.application.port.out.TransaccionRepository;
import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.domain.model.valueobjects.TipoCuenta;
import com.banco.domain.model.valueobjects.TransaccionId;
import com.banco.domain.model.valueobjects.TransaccionId.TipoTransaccion;

import jakarta.transaction.Transactional;




@Service
@Transactional
public class AperturaCuentaService {

    //INYECCION DE DEPENDENCIA
    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;

    // 🎯 CONSTRUCTOR CON INYECCIÓN
    public AperturaCuentaService(ClienteRepository clienteRepository, CuentaRepository cuentaRepository,
            TransaccionRepository transaccionRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
    }




    // METODOS DE CUENTA
    public AperturaCuentaResponse ejecutarAperturaCuenta(AperturaCuentaRequest request){

        try {
            
            validarRequest(request);

            //CARGAR Y VALIDAR CLIENTE
            Cliente cliente = cargarYValidarCliente(request);

            //CREAR VALUE OBJECTS
            Moneda moneda = Moneda.fromCodigo(request.getMoneda());
            TipoCuenta tipoCuenta = TipoCuenta.fromString(request.getTipoCuenta());

            //GENERAR NUMERO DE CUENTA UNICO
            CuentaId cuentaId = generarNumeroCuenta(request.getSucursal());

            //CREAR ENTIDAD CUENTA
            Cuenta generarCuenta = crearCuenta(cuentaId, cliente.getClienteId(), moneda, tipoCuenta);

            //PROCESAR SALDO INICIAL (si existe)
            if(request.getSaldoInicial() != null && request.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0){
                saldoInicialMinimo(generarCuenta, request.getSaldoInicial(), moneda);
            }

            //ASOCIAR CUENTA AL CLIENTE
            asociarCuentaAlCliente(cliente, cuentaId);

            // GUARDAR CAMBIOS
            guardarCambios(cliente, generarCuenta);

            return crearRespuestaExitosa(generarCuenta, request, cliente);

        } catch (Exception e) {
            
            System.err.println("❌ Error en apertura de cuenta: " + e.getMessage());
            return crearRespuestaError(e.getMessage());
        }
    }


    private Cuenta crearCuenta(CuentaId cuentaId, ClienteId clienteId, Moneda moneda, TipoCuenta tipoCuenta){

        // CREAR CUENTA CON SALDO CERO
        Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda);

        System.out.println("🏦 Cuenta creada: " + cuentaId + 
                         " - Tipo: " + tipoCuenta + 
                         " - Moneda: " + moneda);

        return cuenta;
    }

    private void saldoInicialMinimo(Cuenta cuenta, BigDecimal monto, Moneda moneda){

        Dinero saldoInicial = new Dinero(monto, moneda);

        //VALIDAR MONTO MÍNIMO (ejemplo: $100 para cuentas corrientes)
        BigDecimal minimo = new BigDecimal("100.00");
        if(monto.compareTo(minimo) < 0) throw new IllegalArgumentException(
            "Saldo inicial minimo $" + minimo + ". Se recibio $" + monto );

        // DEPOSITAR EN LA CUENTA
        cuenta.depositar(saldoInicial);

        //CREAR TRANSACCIÓN DE APERTURA
        Transaccion transaccionApertura = new Transaccion(
            generarIdTransaccion(), 
            TipoTransaccion.DEPOSITO,
             null,
              cuenta.getCuentaId(),
               saldoInicial,
                "Deposito inicial apertura de cuenta");

            transaccionApertura.completar();
            transaccionRepository.guardar(transaccionApertura);

            System.out.println("💰 Saldo inicial depositado: " + saldoInicial);


    }

    private void asociarCuentaAlCliente(Cliente cliente, CuentaId cuentaId){

        try {
            cliente.agregarCuenta(cuentaId);
            System.out.println("✅ Cuenta asociada al cliente: " + cliente.getNombre());
        } catch (Exception e) {
            throw new IllegalStateException("El cliente excede limite de cuentas");
        }
    }

    private void guardarCambios(Cliente cliente, Cuenta cuenta){
        clienteRepository.actualizar(cliente);
        cuentaRepository.guardar(cuenta);
        System.out.println("💾 Cambios guardados exitosamente");

    }

    private TransaccionId generarIdTransaccion() {
        String id = "TXN-APERTURA-" + System.currentTimeMillis();
        return new TransaccionId(id);
    }







    

    //VALIDACIONES DATOS DE ENTRADA
    private void validarRequest(AperturaCuentaRequest request){

        if(request == null) throw new IllegalArgumentException("La solicitud no puede ser nula");

        if(request.getClienteId() == null) throw new IllegalArgumentException("Se necesita id del cliente");

        if(request.getMoneda() == null) throw new IllegalArgumentException("Se requiere moneda");

        if(request.getTipoCuenta() == null) throw new IllegalArgumentException("Se requiere tipo de cuenta");

        if(request.getSaldoInicial() != null && request.getSaldoInicial().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(
            "El saldo inicial no puede ser menor a 0");

            System.out.println("✅ Validaciones de request OK");
    }

    private Cliente cargarYValidarCliente(AperturaCuentaRequest request){

        ClienteId clienteId = ClienteId.newCliente(request.getClienteId());

        Cliente cliente = clienteRepository.buscarPorId(clienteId).orElseThrow(()-> new IllegalStateException(
            "Cliente no encontrado: " + clienteId));

            //VALIDAR QUE ESTA ACTIVO
            if(!cliente.getActiva()) throw new IllegalStateException("El cliente esta inactivo y no puede abrir cuentas");

            System.out.println("✅ Cliente validado: " + cliente.getNombre());
            return cliente;
    }

        private int calcularDigitoVerificador(String numero) {
        int suma = 0;
        boolean doble = false;
        
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(numero.charAt(i));
            
            if (doble) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            
            suma += digito;
            doble = !doble;
        }
        
        return (10 - (suma % 10)) % 10;
    }

    private CuentaId generarNumeroCuenta(String sucursal){
        //🎯 FORMATO: PAÍS-BANCO-SUCURSAL-SECUENCIA-DÍGITO
        // // Ejemplo: ARG-017-001-00012345-8

        String codigoPais = "ARG";
        String codigoBanco = "017"; // Código ficticio de nuestro banco
        String codigoSucursal = sucursal != null ? String.format("%03d", Integer.parseInt(sucursal)) : "001";

        // GENERAR SECUENCIA ÚNICA (en producción sería de una base de datos)
        long secuencia = System.currentTimeMillis() % 100000000L; // 8 dígitos
        String numeroSecuencial = String.format("%08d", secuencia);

        // CALCULAR DÍGITO VERIFICADOR (algoritmo simplificado)
        String base = codigoBanco + codigoSucursal + numeroSecuencial;
        int digitoVerificador = calcularDigitoVerificador(base);

        // CONSTRUIR NÚMERO COMPLETO
        String numeroCompleto = String.format("%s-%s-%s-%s-%d",
            codigoPais, codigoBanco, codigoSucursal, numeroSecuencial, digitoVerificador);

        //GENERAR NUMERO DE CUENTA 
        CuentaId cuentaId = CuentaId.newCuentaId(numeroCompleto);

        return cuentaId;

    }




    // RESPUESTA EXITOSA Y ERRONEA
        public AperturaCuentaResponse crearRespuestaExitosa(Cuenta cuenta, AperturaCuentaRequest request,Cliente cliente) {
        
        String mensaje = String.format(
            "✅ Cuenta %s creada exitosamente para %s. %s",
            cuenta.getCuentaId(),
            cliente.getNombre(),
            request.getSaldoInicial() != null && 
            request.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0 ?
            "Saldo inicial: $" + request.getSaldoInicial() : "Sin saldo inicial"
        );
        
        return new AperturaCuentaResponse(
            cuenta.getCuentaId().toString(),
            cliente.getClienteId().toString(),
            request.getTipoCuenta(),
            request.getMoneda(),
            request.getSaldoInicial(),
            LocalDateTime.now(),
            mensaje
        );
    }

        public AperturaCuentaResponse crearRespuestaError(String mensajeError) {
        return new AperturaCuentaResponse(
            null,
            null,
            null,
            null,
            BigDecimal.ZERO,
            LocalDateTime.now(),
            "❌ Error en apertura de cuenta: " + mensajeError
        );
    }
    
}
