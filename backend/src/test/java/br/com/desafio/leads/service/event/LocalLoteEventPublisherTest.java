package br.com.desafio.leads.service.event;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.service.LoteProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalLoteEventPublisherTest {

    @Mock
    private LoteProgressService progressService;

    @InjectMocks
    private LocalLoteEventPublisher publisher;

    @Test
    @DisplayName("Deve publicar evento de início do lote")
    void devePublicarEventoDeInicio() {
        LoteIniciadoEvent event = new LoteIniciadoEvent(
                UUID.randomUUID(),
                100,
                LocalDateTime.now()
        );

        publisher.publicarInicio(event);

        verify(progressService, times(1))
                .onLoteIniciado(event);
    }

    @Test
    @DisplayName("Deve publicar evento de chunk concluído")
    void devePublicarChunkConcluido() {
        LoteChunkConcluidoEvent event = new LoteChunkConcluidoEvent(
                UUID.randomUUID(),
                1,
                100,
                95,
                5,
                250L,
                LocalDateTime.now()
        );

        publisher.publicarChunkConcluido(event);

        verify(progressService, times(1))
                .onChunkConcluido(event);
    }

    @Test
    @DisplayName("Deve publicar evento de finalização do lote")
    void devePublicarEventoDeFinalizacao() {
        LoteFinalizadoEvent event = new LoteFinalizadoEvent(
                UUID.randomUUID(),
                LoteStatus.FINALIZADO,
                95,
                5,
                LocalDateTime.now()
        );

        publisher.publicarFinalizacao(event);

        verify(progressService, times(1))
                .onLoteFinalizado(event);
    }
}