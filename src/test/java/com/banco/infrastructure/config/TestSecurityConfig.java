package com.banco.infrastructure.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.SecurityFilterChain;



// TestSecurityConfig  = Es una configuración de seguridad especial para tests. Reemplaza la seguridad real (JWT) 
// por una más simple con usuario fijo en memoria para testeos


@SuppressWarnings("deprecation")  // elimina msj de advertencia 
@TestConfiguration
@Profile("test") // perfil "test"
public class TestSecurityConfig {



    // Defimos un usuario de prueba en memoria/ Para tener un usuario válido (testuser/testpass)
    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("testuser")
            .password(new BCryptPasswordEncoder().encode("testpass"))
            .roles("USER")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }



    // Encripta contraseñas con BCrypt/ Para que coincida con la contraseña guardada
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();  // Encripta, seguro
    }


    // Valida usuario/contraseña/ Lo necesita AuthService para autenticar
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        
        //Usamos DaoAuthenticationProvider (explicito) - es el encargado de validar usuario y contraseña en Spring Security.
        // a diferencia de  AuthenticationManager, que llama a DaoAuthenticationProvider por detras
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); 
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }




    // Configuramos qué rutas son públicas/protegidas
    //Le dice a Spring Security: Desactivá CSRF (protección contra ataques)
    //Todas las rutas requieren autenticación
    //Usá autenticación básica HTTP (usuario/contraseña en el header)
    @Bean
    @Primary  // le decimos a spring que de todos los bean de confg priorice este
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // todas las rutas requieren autenticacion
            )
            .httpBasic(httpBasic -> {}); //Habilita autenticación básica HTTP (manda usuario/contraseña en cada request).
        
        return http.build();
    }

}
