package br.com.desafio.leads.service.event;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.service.LoteProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LocalLoteEventPublisher implements LoteEventPublisher {

    private final LoteProgressService progressService;

    @Override
    public void publicarInicio(LoteIniciadoEvent event) {
        progressService.onLoteIniciado(event);
    }

    @Override
    public void publicarChunkConcluido(LoteChunkConcluidoEvent event) {
        progressService.onChunkConcluido(event);
    }

    @Override
    public void publicarFinalizacao(LoteFinalizadoEvent event) {
        progressService.onLoteFinalizado(event);
    }

}
