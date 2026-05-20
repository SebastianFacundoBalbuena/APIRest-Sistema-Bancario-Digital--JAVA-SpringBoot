package com.banco.infrastructure.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import com.banco.application.services.UsersDetailsService;


@SuppressWarnings("all") // elimina los warings 
@Configuration  // Spring usara nuestra configuracion
@EnableWebSecurity  // Activa la seguridad web
@Profile("!test") //no carga cuando el perfil es tets
public class SecurityConfig {
    

    private final UsersDetailsService usersDetailsService;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UsersDetailsService usersDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.usersDetailsService = usersDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    // @Bean - se le coloca al metodo para que spring la ejecute automa.
    // lo cual con un @Autowired en una propiedad tendremos el resultado del metodo
    // logrando que sea mas dinamico sin tener que crear el objeto nosotros mismos
    
    @Bean
    public PasswordEncoder passwordEncoder(){
        // BCrypt: Encripta contraseñas (no se guardan en texto plano)
        // // Cada vez que encriptás, obtenés un hash diferente (seguro)
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager - se usa para autenticar usuarios por primera vez en el Login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{

        return config.getAuthenticationManager();

    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{


        // Nota : //CSRF = (falsificación de petición). Un ataque donde un sitio 
        // malicioso hace que un usuario autenticado ejecute acciones sin saberlo.
        // deshabilitamos ya que usamos JWT para seguridad

        http
        .csrf(csrf -> csrf.disable()) 

        // Cada request es independiente, no guardes nada en el servidor(Cookies)
        // cada request tiene su Authorization(datos) y creara conflictos de datos almacenados anteriormente
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
         )

         // Configurar autorización de rutas
         .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            
            //Rutas PÚBLICAS
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/clientes/verificar-email").permitAll()

            .requestMatchers(HttpMethod.POST, "/api/cuentas").authenticated() // crear cuenta(usuario autenticado)

            // RUTAS SOLO PARA ADMIN
            .requestMatchers(HttpMethod.GET, "/api/clientes").hasRole("ADMIN")  // Listar todos
            .requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasRole("ADMIN")  // Actualizar
            .requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasRole("ADMIN")  // Desactivar
            .requestMatchers(HttpMethod.POST, "/api/clientes/*/cuenta/*").hasRole("ADMIN")  // Agregar cuenta
            .requestMatchers(HttpMethod.DELETE, "/api/clientes/*/cuenta/*").hasRole("ADMIN")  // Remover cuenta
            .requestMatchers("/api/cuentas").hasRole("ADMIN")  // Operaciones administrativas en cuentas


            // RUTAS PARA USUARIOS AUTENTICADOS (CLIENTES)
            .requestMatchers(HttpMethod.GET, "/api/cuentas/{cuentaId}").authenticated() // ver saldo
            .requestMatchers(HttpMethod.GET, "/api/clientes/{id}").authenticated()  // Ver su propio cliente
            .requestMatchers(HttpMethod.GET, "/api/clientes/{id}/cuenta").authenticated()  // Ver sus cuentas
            .requestMatchers("/api/transacciones/**").authenticated()  // Transferencias, movimientos

            // cualquier otra ruta requiere AUTENTICACION
            .anyRequest().authenticated()

         )


         // Antes de que se ejecute el filtro de autenticación 
         // por usuario/contraseña, ejecutá primero MI filtro JWT.
         // Así podemos validar el token primero
         .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

         return http.build();

    }

    


}
