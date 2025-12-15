package com.banco.infrastructure.persistence.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;



@Entity
@Table(name = "clientes")
public class ClienteEntity {
    
    // ATRIBUTOS CON ANOTACIONES JPA
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", unique = true, nullable = false, length = 50)
    private String clienteId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @ElementCollection // @ElementCollection → Crea tabla separada para la lista
    @CollectionTable(name = "cliente_cuentas",
                     joinColumns = @JoinColumn(name = "cliente_entity_id")) //@CollectionTable → Define nombre de tabla y columna de unión
    @Column(name = "cuentas_Ids")
    private List<String> cuentasIds;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @Column(name = "maximo_cuentas_permitidas", nullable = false)
    private int maxCuentasPermitidas = 5;


    // CONSTRUCTOR VACIO REQUERIDO POR JPA
    public ClienteEntity(){}


    public ClienteEntity(String clienteId, String nombre, String email) {

        this.clienteId = clienteId;
        this.nombre = nombre;
        this.email = email;
        this.cuentasIds = new ArrayList<>();
        this.activa = true;
        this.maxCuentasPermitidas = 5;
    }


    public UUID getId() {  return id; }
    public void setId(UUID id) { this.id = id;  }

    public String getClienteId() {return clienteId; }
    public void setClienteId(String clienteId) {this.clienteId = clienteId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getCuentasIds() { return cuentasIds; }
    public void setCuentasIds(List<String> cuentasIds) {  this.cuentasIds = cuentasIds; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa;}

    public int getMaxCuentasPermitidas() { return maxCuentasPermitidas; }
    public void setMaxCuentasPermitidas(int maxCuentasPermitidas) { this.maxCuentasPermitidas = maxCuentasPermitidas;}


    //METODOS PERSISTENCIA
    public void agregarCuentaId(String cuentaId) {
        this.cuentasIds.add(cuentaId);
    }
    

}
