package com.banco.infrastructure.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



// OncePerRequestFilter = 
// Se ejecuta una sola vez por peticion = Interceptar la petición ANTES de que llegue al controlador. 
// Contiene el metodo doFilterInternal que contendra la logica de extraer
// toda la informacion del request = Lo que hace (validar token, extraer usuario, etc.)
// Decidir si la petición sigue al controlador o se corta (chain)

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;


    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }



    // doFilterInternal - interceptar cada petición, extraer el token JWT del header Authorization, 
    // validarlo y autenticar al usuario una sola vez antes de que la petición llegue al controlador.
    // HttpServletRequest - contiene datos de la peticion
    //FilterChain - Representa los siguientes filtros y el controlador. para continuar se usa .doFilter
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
        HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{



            try {
                
                String token = extraerToken(request);

                // validar token y si ya esta autenticado(SecurityContextHolder)
                if(token != null){
                    String username = jwtUtils.extraerUsername(token);

                    if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

                        // cargamos usuario
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if(jwtUtils.validateToken(token, userDetails)){

                            // crear autorizacion y ponerla en el contexto
                            // Este es el usuario que se autenticó y estos son sus permisos.
                            UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                            //agregamos información extra sobre la petición actual:  ej Ip del cliente, sesion
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // Guardamos al usuario en el contexto
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            System.out.println("Usuario autenticado: " + username);
                        }
                    }
                }
                

            } catch (Exception e) {
                System.out.println("Error en autenticación JWT: " + e.getMessage());
            }


            // continuamos al controlador con todo verificado
            filterChain.doFilter(request, response);


        }


    // Metodo auxiliar para extraer token
    private String extraerToken(HttpServletRequest request){

        // extraemos de la clave Authorization el valor
        String header = request.getHeader("Authorization");

        // si empieza con "Bearer" obtenemos la parte del token que queremos
        if(header != null && header.startsWith("Bearer ")){

            return header.substring(7);
        }

            return null;

    }

    
}
