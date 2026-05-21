package br.com.desafio.leads.dto;

import br.com.desafio.leads.model.LoteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LoteStatusResponse(
        UUID loteId,
        LoteStatus status,
        int totalLinhas,
        int totalSucesso,
        int totalErros,
        int progressoPercentual,
        LocalDateTime criadoEm,
        LocalDateTime iniciadoEm,
        LocalDateTime finalizadoEm
) {}
