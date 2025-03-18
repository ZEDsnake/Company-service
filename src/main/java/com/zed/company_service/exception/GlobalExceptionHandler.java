package com.zed.company_service.exception;

import com.zed.company_service.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleCompanyNotFoundException(CompanyNotFoundException ex) {
        ErrorDTO errorDTO = new ErrorDTO(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }
}
