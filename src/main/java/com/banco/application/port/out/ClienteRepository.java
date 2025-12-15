package com.banco.application.port.out;

import java.util.Optional;

import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;

public interface ClienteRepository {

    // GUARDAR
     void guardar(Cliente cliente);

    // BUSCAR POR ID
    Optional<Cliente> buscarPorId(ClienteId clienteId);

    // VALIDAR EXISTENCIA POR EMAIL
    boolean existePorEmail(String email);

    // ACTUALIZAR
    void actualizar(Cliente cliente);


}
