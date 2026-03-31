package com.banco;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class BancoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoApplication.class, args);
        System.out.println("✅ Aplicación bancaria iniciada correctamente");
		System.out.print("✅ LOCALHOST:  http://localhost:8080\n"); //n = para salto de linea
		System.out.println("✅ Documentacion implementada en Swagger: http://localhost:8080/swagger-ui/index.html");
	}


}
