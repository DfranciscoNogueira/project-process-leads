package br.com.desafio.leads.event;

import br.com.desafio.leads.model.LoteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteFinalizadoEvent(UUID loteId, LoteStatus status, int sucesso, int erros, LocalDateTime ocorridoEm) {}
