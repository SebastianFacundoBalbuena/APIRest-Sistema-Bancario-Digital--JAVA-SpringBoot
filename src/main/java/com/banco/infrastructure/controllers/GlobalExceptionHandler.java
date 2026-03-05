package com.banco.infrastructure.controllers;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.banco.application.dto.ErrorResponseDTO;






@RestControllerAdvice
public class GlobalExceptionHandler {
    



    // Error de email
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException e) {
    
    String mensaje = e.getFieldError("email") != null  // Busca si hay error en el campo "email"
        ? e.getFieldError("email").getDefaultMessage() // Obtiene el mensaje de @Email(message = "...")
        : "Error de validación";                       // sino usamos msj general
    
    return ResponseEntity.badRequest()
        .body(new ErrorResponseDTO("ERROR_VALIDACION", mensaje));
}



     // 400 - BAD REQUEST (para validaciones y errores de entrada)
     @ExceptionHandler(IllegalArgumentException.class)
     public ResponseEntity<ErrorResponseDTO> handlerIllegalArgument(IllegalArgumentException e){

        String mensaje = e.getMessage();

        HttpStatus status = HttpStatus.BAD_REQUEST;

        String codigo = "ERROR_VALIDACION";

        // Si el mensaje contiene "no encontrado" → 404
        if(mensaje != null && mensaje.contains("no encontrado")){

            status = HttpStatus.NOT_FOUND;
            codigo = "NO_ENCONTRADO";

        }

        // si contiene "la sintaxis del email es invalida"
        if(mensaje != null && mensaje.contains("Formato de email inválido")){

            status = HttpStatus.BAD_REQUEST;
            codigo = "SINTAXIS_DEL_EMAIL_INVALIDA";

        }

        return ResponseEntity
               .status(status)
               .body(new ErrorResponseDTO(codigo, mensaje));

     }



     // 409 - CONFLICT (para estados inválidos)
     @ExceptionHandler(IllegalStateException.class)
     public ResponseEntity<ErrorResponseDTO> handlerState(IllegalStateException e){

        String mensaje = e.getMessage();

        return ResponseEntity
               .status(HttpStatus.CONFLICT)
               .body(new ErrorResponseDTO("ESTADO_INVALIDO", mensaje));
     }



     // 500 - INTERNAL SERVER ERROR (para cualquier otra excepción)
     @ExceptionHandler(Exception.class)
     public ResponseEntity<ErrorResponseDTO>handlerGeneric(Exception e){

        String mensaje = e.getMessage();

        return ResponseEntity
               .status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(new ErrorResponseDTO("ERROR_INTERNO", mensaje));

     }
}
