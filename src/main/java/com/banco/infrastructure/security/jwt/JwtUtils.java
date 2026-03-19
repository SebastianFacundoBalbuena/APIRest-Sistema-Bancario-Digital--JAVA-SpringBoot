package com.banco.infrastructure.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;




//@Component - Spring crea y administre el objeto automáticamente.
//Así después puedo usarlo en otras clases sin tener que hacer new JwtUtils().

@Component
public class JwtUtils {
    

    //@Value - firma u codigo, Spring traeme este valor del archivo de configuración(application.properties).
    @Value("${jwt.secret}")
    private String jwtSecret;

    //Es el plazo de validez del token 24hs(en milisegundos).
    @Value("${jwt.expiration}")
    private int jwtExpiration;


     // Generar clave secreta segura
     public Key key(){
        
        byte[] bytes = jwtSecret.getBytes(); // Convierte el String en un array de bytes.
        return Keys.hmacShaKeyFor(bytes);    //  Toma esos bytes y crea una clave HMAC-SHA (el algoritmo de firma).
     }



     // GENERAR TOKEN
     public String generarToken(UserDetails userDetails){

        // Agregar información extra al token
        // Nota = Siempre y cuando cumpla con clave, valor = Map<String, Object>
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", userDetails.getAuthorities());

        return crearToken(claims, userDetails.getUsername());

     }

     private String crearToken(Map<String, Object> claims, String subject){

        // Crea el token con todos los datos necesarios como Codigo, valor
        // Transforma a un String largo que retornara
        return Jwts.builder()
               .setClaims(claims) //informacion extra
               .setSubject(subject) // sujetx
               .setIssuedAt(new Date(System.currentTimeMillis())) //conjunto demandado en...
               .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // expira en..
               .signWith(key(), SignatureAlgorithm.HS256) //firma con el algoritmo..
               .compact();


     }




     // EXTRAER DATOS DEL TOKEN



     public String extraerUsername(String token){
        return extraerClaimsEspecificamente(token, Claims::getSubject);
     }

     public Date extraerExpiracion(String token){
        return extraerClaimsEspecificamente(token, Claims::getExpiration);
     }

    private Boolean tokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }


    //  VALIDAR TOKEN
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extraerUsername(token);
        return (username.equals(userDetails.getUsername()) && !tokenExpirado(token));
    }


     // La función devuelve lo que necesitás (String, Date, etc.)
     // <T> Tipo de dato generico - declara el tipo genérico
     // T es el tipo de retorno(generico)
     private <T> T extraerClaimsEspecificamente(String tokenEspecifico, Function<Claims, T> claimsResuelto){

        final Claims claims = extraerTodoClaims(tokenEspecifico);

        return claimsResuelto.apply(claims);
     }


     // Este método abre el token, verifica que sea auténtico y devuelve los datos que contiene.
     public Claims extraerTodoClaims(String token){

        return Jwts.parserBuilder() //Prepara un "lector" de tokens JWT.
               .setSigningKey(key()) // Uso la llave para abrir 
               .build()
               .parseClaimsJws(token) //Analiza el token y verifica: Que la firma sea válida, expiracion y formato
               .getBody(); //Devuelve los claims (toda la info que guarde: sub, rol, exp, etc.)
               
     }




}
