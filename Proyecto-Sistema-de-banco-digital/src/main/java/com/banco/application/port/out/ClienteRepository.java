package com.banco.application.port.out;



import java.util.List;

import com.banco.domain.model.entities.Cliente;


public interface ClienteRepository {

    // GUARDAR
     void guardar(Cliente cliente);

    //buscar todos los clientes
    List<Cliente> listarTodos();

    // BUSCAR POR ID
    Cliente buscarPorId(String clienteId);

    // BUSCAR POR EMAIL
    Cliente buscarPorEmail(String email);   

    // VALIDAR EXISTENCIA POR EMAIL
    boolean existePorEmail(String email);

    // ACTUALIZAR
    void actualizar(Cliente cliente);


}
