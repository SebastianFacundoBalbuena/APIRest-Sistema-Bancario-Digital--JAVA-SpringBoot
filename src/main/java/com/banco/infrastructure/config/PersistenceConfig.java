package com.banco.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


//@EnableJpaRepositories: Le dice a Spring dónde buscar interfaces de repositorio
// @EntityScan: Le dice a Spring dónde buscar entidades JPA
// @EnableTransactionManagement: Habilita transacciones automáticas


@Configuration
@EnableJpaRepositories(
  basePackages = "com.banco.infrastructure.persistence.jpa",
  considerNestedRepositories = true
)

@EntityScan(
    basePackages = "com.banco.infrastructure.persistence.entities" )

@EnableTransactionManagement
public class PersistenceConfig{}