package com.onbius.busspass.controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String,String>> status(ResponseStatusException e){
    return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Operação não permitida.":e.getReason()));
  }
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String,String>> bad(IllegalArgumentException e){
    return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));
  }
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String,String>> duplicate(DataIntegrityViolationException e){
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Registro duplicado ou incompatível."));
  }
}
