package br.com.desafio.leads.service.csv;

import java.time.LocalDate;

public record CsvLeadLine(String nome, String email, String telefone, String origem, LocalDate dataCadastro) {}
