package br.com.desafio.leads.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteIniciadoEvent(UUID loteId, int totalLinhas, LocalDateTime ocorridoEm) {}
