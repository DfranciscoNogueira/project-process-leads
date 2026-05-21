package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.LoteStatusResponse;
import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.exception.BusinessException;
import br.com.desafio.leads.model.LoteProcessamento;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.repository.LoteProcessamentoRepository;
import br.com.desafio.leads.repository.LoteRepository;
import br.com.desafio.leads.service.sse.LoteSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteProgressService {

    private final LoteRepository loteRepository;
    private final LoteProcessamentoRepository processamentoRepository;
    private final LoteSseService sseService;

    @Transactional
    public void onLoteIniciado(LoteIniciadoEvent event) {
        var lote = loteRepository.findById(event.loteId()).orElseThrow(() -> new BusinessException("Lote não encontrado."));
        lote.setStatus(LoteStatus.PROCESSANDO);
        lote.setTotalLinhas(event.totalLinhas());
        lote.setIniciadoEm(event.ocorridoEm());
        publish(lote.getId());
    }

    @Transactional
    public void onChunkConcluido(LoteChunkConcluidoEvent event) {
        var lote = loteRepository.findById(event.loteId()).orElseThrow(() -> new BusinessException("Lote não encontrado."));
        var processamento = LoteProcessamento.builder()
                .lote(lote)
                .chunkIndex(event.chunkIndex())
                .totalLinhas(event.totalLinhas())
                .totalSucesso(event.sucesso())
                .totalErros(event.erros())
                .tempoMs(event.tempoMs())
                .processadoEm(event.ocorridoEm())
                .build();
        processamentoRepository.save(processamento);
        lote.setTotalSucesso(processamentoRepository.sumSucesso(lote.getId()));
        lote.setTotalErros(processamentoRepository.sumErros(lote.getId()));
        publish(lote.getId());
    }

    @Transactional
    public void onLoteFinalizado(LoteFinalizadoEvent event) {
        var lote = loteRepository.findById(event.loteId()).orElseThrow(() -> new BusinessException("Lote não encontrado."));
        lote.setStatus(event.status());
        lote.setTotalSucesso(event.sucesso());
        lote.setTotalErros(event.erros());
        lote.setFinalizadoEm(LocalDateTime.now());
        publish(lote.getId());
    }

    @Transactional(readOnly = true)
    public LoteStatusResponse status(UUID loteId) {
        var lote = loteRepository.findById(loteId).orElseThrow(() -> new BusinessException("Lote não encontrado."));
        var processados = lote.getTotalSucesso() + lote.getTotalErros();
        var percentual = lote.getTotalLinhas() == 0 ? 0 : Math.min(100, (processados * 100) / lote.getTotalLinhas());
        return new LoteStatusResponse(lote.getId(), lote.getStatus(), lote.getTotalLinhas(), lote.getTotalSucesso(), lote.getTotalErros(), percentual, lote.getCriadoEm(), lote.getIniciadoEm(), lote.getFinalizadoEm());
    }

    private void publish(UUID loteId) {
        sseService.publish(loteId, status(loteId));
    }
}
