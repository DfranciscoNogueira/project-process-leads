package br.com.desafio.leads.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(LocalDateTime timestamp, int status, String erro, String mensagem) {}
