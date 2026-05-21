package br.com.desafio.leads.service.processing;

public record ChunkResult(int chunkIndex, int total, int sucesso, int erros, long tempoMs) {}
