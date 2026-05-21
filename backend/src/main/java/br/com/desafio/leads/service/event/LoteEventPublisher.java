package br.com.desafio.leads.service.event;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;

public interface LoteEventPublisher {

    void publicarInicio(LoteIniciadoEvent event);

    void publicarChunkConcluido(LoteChunkConcluidoEvent event);

    void publicarFinalizacao(LoteFinalizadoEvent event);

}
