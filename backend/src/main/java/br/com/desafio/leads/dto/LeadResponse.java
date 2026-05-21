package br.com.desafio.leads.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LeadResponse(UUID id, String nome, String email, String telefone, String origem, LocalDate dataCadastro, UUID loteId) {}
