package com.banco.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.entities.Transaccion;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.TransaccionId;

public interface TransaccionRepository {

    // GUARDAR
    void guardar(Transaccion transaccion);

    // BUSCAR POR ID
    Optional<Transaccion> buscarPorId(TransaccionId transaccionId);

    // BUSCAR POR CUENTA
    List<Transaccion> buscarPorCuenta(Cuenta cuenta, LocalDateTime desde, LocalDateTime hasta);

    //BUSCAR TTANSACCION POR NUMERO DE CUENTA
    List<Transaccion> buscarCuentas(CuentaId cuentaId);

    //BUSCAR POR REFERENCIA
    List<Transaccion> buscarPorReferencia(String referencia);

    
}
