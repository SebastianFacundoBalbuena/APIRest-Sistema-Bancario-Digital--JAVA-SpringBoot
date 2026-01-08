package com.banco.application.port.out;



import com.banco.domain.model.entities.Cliente;


public interface ClienteRepository {

    // GUARDAR
     void guardar(Cliente cliente);

    // BUSCAR POR ID
    Cliente buscarPorId(String clienteId);

    // VALIDAR EXISTENCIA POR EMAIL
    boolean existePorEmail(String email);

    // ACTUALIZAR
    void actualizar(Cliente cliente);


}
