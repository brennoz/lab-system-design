package com.lab.urlshortener.api;

import com.lab.urlshortener.domain.UrlNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(UrlNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
