package com.banco.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.banco.application.port.out.ClienteRepository;
import com.banco.domain.model.entities.Cliente;
import com.banco.domain.model.valueobjects.ClienteId;
import com.banco.infrastructure.persistence.entities.ClienteEntity;
import com.banco.infrastructure.persistence.mappers.ClienteMapper;
import jakarta.transaction.Transactional;






@Repository
@Transactional
public class ClienteRepositoryJpa implements ClienteRepository {
    

    interface ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID> {
    // Esta interfaz hereda de JPA, por lo cual podemos usar sus palabras clave
    // COMO existsBy - findBy etc + nombre del atributo
    // JPA ya conoce estas palabras y las detecta automaticamente sabiendo que queremos

    Optional<ClienteEntity> findByClienteId(String clienteId);

    boolean existsByEmail(String email);
        
    }

    //INYECCION DE DEPENDENCIA
    private final ClienteJpaRepository clienteJpaRepository;
    private final ClienteMapper clienteMapper;

    public ClienteRepositoryJpa(ClienteJpaRepository clienteJpaRepository, ClienteMapper clienteMapper) {
        this.clienteJpaRepository = clienteJpaRepository;
        this.clienteMapper = clienteMapper;
    }


    // METODOS 

    @Override
    public Optional<Cliente> buscarPorId(ClienteId clienteId){
        
        String IdString = clienteId.getValor();
        
        Optional<ClienteEntity> entityOpt = clienteJpaRepository.findByClienteId(IdString);

        if(entityOpt.isPresent()){
            Cliente cliente = clienteMapper.aDominio(entityOpt.get());

            return Optional.of(cliente);
        }
        else{
            System.out.println(" Cliente NO encontrado: " + clienteId);
            return Optional.empty();
        }

    }

    @Override
    public void guardar(Cliente cliente){

        ClienteEntity clienteEntity = clienteMapper.aEntity(cliente);
        
        if(clienteEntity != null){

         clienteJpaRepository.save(clienteEntity);
         System.out.println(" Cliente guardado exitosamente");
        }

        
    }

    @Override
    public void actualizar(Cliente cliente){
        guardar(cliente);
    }

    @Override
    public boolean existePorEmail(String email){

        boolean emailExiste = clienteJpaRepository.existsByEmail(email);

        return emailExiste;
    }


    
}
