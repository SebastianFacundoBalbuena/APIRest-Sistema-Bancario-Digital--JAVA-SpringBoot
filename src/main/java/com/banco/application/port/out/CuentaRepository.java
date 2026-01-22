package com.banco.application.port.out;

import java.util.List;
import java.util.Optional;
import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.ClienteId;





public interface CuentaRepository {

    //GUARDAR UNA CUENTA
    void guardar(Cuenta cuenta);

    // BUSCAR CUENTA
    Optional<Cuenta> buscarPorId(CuentaId cuentaId);

    // BUSCAR CUENTAS POR CLIENTES
    List<Cuenta> buscarPorCliente(ClienteId ClienteId);


    //ACTUALIZAR CUENTA
    void actualizar(Cuenta cuenta);

    // BUSCAR CUENTA CON NUMERO
    boolean existeCuentaConNumero(String numeroCuenta);

}
