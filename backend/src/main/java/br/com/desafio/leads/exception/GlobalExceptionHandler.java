package br.com.desafio.leads.exception;

import br.com.desafio.leads.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse business(BusinessException ex) {
        return new ApiErrorResponse(LocalDateTime.now(), 400, "Regra de negócio", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse validation(MethodArgumentNotValidException ex) {
        return new ApiErrorResponse(LocalDateTime.now(), 400, "Validação", "Dados inválidos na requisição.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse generic(Exception ex) {
        return new ApiErrorResponse(LocalDateTime.now(), 500, "Erro interno", ex.getMessage());
    }

}
