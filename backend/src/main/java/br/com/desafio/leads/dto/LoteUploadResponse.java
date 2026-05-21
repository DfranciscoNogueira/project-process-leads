package br.com.desafio.leads.dto;

import java.util.UUID;

public record LoteUploadResponse(UUID loteId, String status, String mensagem) {
}
