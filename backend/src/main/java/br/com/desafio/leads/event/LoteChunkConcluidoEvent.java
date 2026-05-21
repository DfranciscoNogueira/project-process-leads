package br.com.desafio.leads.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteChunkConcluidoEvent(UUID loteId, int chunkIndex, int totalLinhas, int sucesso, int erros, long tempoMs, LocalDateTime ocorridoEm) {}
