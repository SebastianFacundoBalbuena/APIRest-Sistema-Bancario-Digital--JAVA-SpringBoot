package com.banco.application.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.stereotype.Service;
import com.banco.application.dto.ActualizarClienteRequest;
import com.banco.application.dto.ClienteRequest;
import com.banco.application.dto.ClienteResponse;
import com.banco.application.port.out.ClienteRepository;
import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import jakarta.transaction.Transactional;


@Service
@Transactional
public class GestionClienteService {

    private final ClienteRepository clienteRepository;

    private static final int MAX_CUENTAS_PERMITIDAS = 5; // Debería venir del dominio

    // CONSTRUCTOR
    public GestionClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;

    }

    
    // METODOS BASICOS

    public ClienteResponse crearCliente(ClienteRequest request){

        validarEmail(request.getEmail());

        ClienteId clienteId = ClienteId.generarNuevoId();

        Cliente cliente = new Cliente(clienteId, request.getNombre(), request.getEmail());

        
        clienteRepository.guardar(cliente);

        return convertirResponse(cliente);

        
    }

    public ClienteResponse buscarClientePorId(String clienteId){
         
        Cliente cliente = validarClienteId(clienteId);

        return convertirResponse(cliente);

    }

    public ClienteResponse actualizarCliente(String clienteId, ActualizarClienteRequest request){


        Cliente clienteDB = validarClienteId(clienteId);

        validarEmail(request.getEmail().get());

        clienteDB.setNombre(request.getNombre().get());
        clienteDB.setEmail(request.getEmail().get());

             clienteRepository.guardar(clienteDB);

             return convertirResponse(clienteDB);
    }

    public void descativarCliente(String clienteStr){

        Cliente cliente = validarClienteId(clienteStr);

        if(!cliente.getCuentas().isEmpty()) throw new IllegalArgumentException(
            "No se puede desactivar cliente con cuentas activas: " + cliente.getCuentas().size());

        
        Cliente clienteDesactivado = new Cliente(
            cliente.getClienteId(), 
            cliente.getNombre(), 
            cliente.getEmail(), 
            false, 
            cliente.getCuentas());
        
        clienteRepository.guardar(clienteDesactivado);
    }

        public void activarCliente(String clienteStr){

        Cliente cliente = validarClienteId(clienteStr);

        
        Cliente clienteActivado = new Cliente(
            cliente.getClienteId(), 
            cliente.getNombre(), 
            cliente.getEmail(), 
            true, 
            cliente.getCuentas());
        
        clienteRepository.guardar(clienteActivado);
    }

    public void agregarCuentaAcliente(String clienteIdStr, String cuentaStr){

        CuentaId cuentaId = CuentaId.newCuentaId(cuentaStr);

        Cliente cliente = validarClienteId(clienteIdStr);
        validarLimiteDeCuentas(cliente);
        validarCuentaAgregada(cuentaStr, cliente);

        List<CuentaId> nuevaCuenta = new ArrayList<>(cliente.getCuentas());
        nuevaCuenta.add(cuentaId);

        Cliente clienteActualizado = new Cliente(
            cliente.getClienteId(),
            cliente.getNombre(), 
            cliente.getEmail(),
            cliente.getActiva(),
            Collections.unmodifiableList(nuevaCuenta));

        clienteRepository.actualizar(clienteActualizado);
    }

    public void removerCuentaAcliente(String clienteIdStr, String cuentaStr){

        CuentaId cuentaId = CuentaId.newCuentaId(cuentaStr);

        Cliente cliente = validarClienteId(clienteIdStr);
        validarCuentaExistente(cuentaStr, cliente);

        List<CuentaId> nuevaCuenta = new ArrayList<>(cliente.getCuentas());
        nuevaCuenta.remove(cuentaId);

        Cliente clienteActualizado = new Cliente(
            cliente.getClienteId(),
            cliente.getNombre(), 
            cliente.getEmail(),
            cliente.getActiva(),
            Collections.unmodifiableList(nuevaCuenta));

        clienteRepository.actualizar(clienteActualizado);
    }

    public ClienteResponse convertirResponse(Cliente cliente){

        return new ClienteResponse(
            cliente.getClienteId().getValor(), 
            cliente.getNombre(), 
            cliente.getEmail(), 
            cliente.getActiva(), 
            cliente.getCuentas().size(), 
            MAX_CUENTAS_PERMITIDAS, 
            cliente.getCuentas().stream().map(CuentaId::getValor).collect(Collectors.toList()),
             LocalDateTime.now(), 
             LocalDateTime.now());
    }





    // VALIDACIONES

    public void validarEmail(String email){

        if(clienteRepository.existePorEmail(email)){
            throw new IllegalArgumentException("El Email: " + email + " ya esta registrado");
        }
    }

    public Cliente validarClienteId(String cliente){

        ClienteId clienteId = ClienteId.newCliente(cliente);

        if(clienteRepository.buscarPorId(cliente) != null) {

          Cliente returnCliente = clienteRepository.buscarPorId(cliente);

          return returnCliente;

        } 
        else{

            throw new IllegalArgumentException(
                "Cliente no encontrado: " + clienteId);
        }

    }

    public void validarLimiteDeCuentas(Cliente cliente){

        if(cliente.getCuentas().size() >= MAX_CUENTAS_PERMITIDAS) throw new IllegalArgumentException(
            "Limite de cuentas alcanzado. Maximo permitido: " + MAX_CUENTAS_PERMITIDAS);
    }

    public void validarCuentaAgregada(String cuentaIdStr, Cliente cliente){

        CuentaId cuentaId = CuentaId.newCuentaId(cuentaIdStr);

        if(cliente.getCuentas().contains(cuentaId)) throw new IllegalArgumentException(
            "La cuenta ya esta asignada a este cliente ");
    }


    public void validarCuentaExistente(String cuentaIdStr, Cliente cliente){

        CuentaId cuentaId = CuentaId.newCuentaId(cuentaIdStr);

        if(!cliente.getCuentas().contains(cuentaId)) throw new IllegalArgumentException(
            "La cuenta no está asignada a este cliente ");
    }

        

    

}
