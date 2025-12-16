package com.banco;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.banco.domain.model.entities.Cuenta;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.domain.model.valueobjects.CuentaId;
import com.banco.domain.model.valueobjects.Dinero;
import com.banco.domain.model.valueobjects.Moneda;
import com.banco.infrastructure.persistence.jpa.CuentaRepositoryJpa;

@SpringBootApplication
public class BancoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoApplication.class, args);
	}





	@Bean
    CommandLineRunner probarRepositorio(CuentaRepositoryJpa repository) {
        return args -> {
            System.out.println("🚀 Probando conexión con base de datos...");
            
            // 1. Crear una cuenta de dominio
            CuentaId cuentaId = CuentaId.generarNueva(290, 1234, Moneda.USD);
            ClienteId clienteId = ClienteId.newCliente("CLI-00000001");
            Moneda moneda = Moneda.USD;
            Dinero saldoInicial = Dinero.nuevoCero(moneda);
            
            Cuenta cuenta = new Cuenta(cuentaId, clienteId, moneda, saldoInicial, true);
            
            // 2. Guardar en BD usando nuestro repositorio
            repository.guardar(cuenta);
            System.out.println("✅ Cuenta guardada en BD");
            
            // 3. Buscar la cuenta
            var cuentaRecuperada = repository.buscarPorId(cuentaId);
            if (cuentaRecuperada.isPresent()) {
                System.out.println("✅ Cuenta recuperada de BD: " + 
                    cuentaRecuperada.get().getCuentaId());
            } else {
                System.out.println("❌ No se pudo recuperar la cuenta");
            }
            
            // 4. Verificar existencia
            boolean existe = repository.existeCuentaConNumero(cuentaId.getValor());
            System.out.println("✅ Verificación existencia: " + existe);

            System.out.println("✅ Aplicación bancaria iniciada correctamente");
        };
    }

}
