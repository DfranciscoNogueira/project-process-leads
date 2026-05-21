package br.com.desafio.leads.service.event;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.service.LoteProgressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaLoteEventConsumerTest {

    @Mock
    private LoteProgressService progressService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaLoteEventConsumer consumer;

    @Test
    @DisplayName("Deve consumir evento de início e atualizar progresso")
    void deveConsumirEventoDeInicio() throws Exception {
        String payload = """
                {"loteId":"5e1a05dc-dba6-4c43-84e6-6a52a52ff001","totalLinhas":100}
                """;

        LoteIniciadoEvent event = new LoteIniciadoEvent(
                UUID.fromString("5e1a05dc-dba6-4c43-84e6-6a52a52ff001"),
                100, LocalDateTime.now()
        );

        when(objectMapper.readValue(eq(payload), eq(LoteIniciadoEvent.class)))
                .thenReturn(event);

        consumer.onInicio(payload);

        verify(progressService).onLoteIniciado(event);
    }

    @Test
    @DisplayName("Deve consumir evento de chunk concluído e atualizar progresso")
    void deveConsumirEventoDeChunk() throws Exception {
        String payload = """
                {"loteId":"5e1a05dc-dba6-4c43-84e6-6a52a52ff001","chunkIndex":1,"totalLinhas":100,"sucesso":95,"erros":5,"tempoMs":250}
                """;

        LoteChunkConcluidoEvent event = new LoteChunkConcluidoEvent(
                UUID.fromString("5e1a05dc-dba6-4c43-84e6-6a52a52ff001"),
                1,
                100,
                95,
                5,
                250L,
                LocalDateTime.now()
        );

        when(objectMapper.readValue(eq(payload), eq(LoteChunkConcluidoEvent.class)))
                .thenReturn(event);

        consumer.onChunk(payload);

        verify(progressService).onChunkConcluido(event);
    }

    @Test
    @DisplayName("Deve consumir evento de finalização e atualizar progresso")
    void deveConsumirEventoDeFinalizacao() throws Exception {
        String payload = """
                {"loteId":"5e1a05dc-dba6-4c43-84e6-6a52a52ff001","status":"FINALIZADO","sucesso":95,"erros":5}
                """;

        LoteFinalizadoEvent event = new LoteFinalizadoEvent(
                UUID.fromString("5e1a05dc-dba6-4c43-84e6-6a52a52ff001"),
                LoteStatus.FINALIZADO,
                95,
                5,
                LocalDateTime.now()
        );

        when(objectMapper.readValue(eq(payload), eq(LoteFinalizadoEvent.class)))
                .thenReturn(event);

        consumer.onFinalizado(payload);

        verify(progressService).onLoteFinalizado(event);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando payload for inválido")
    void deveLancarErroQuandoPayloadInvalido() throws Exception {
        String payload = "{json-invalido}";

        when(objectMapper.readValue(eq(payload), eq(LoteIniciadoEvent.class)))
                .thenThrow(new JsonProcessingException("JSON inválido") {
                });

        assertThrows(
                IllegalArgumentException.class,
                () -> consumer.onInicio(payload)
        );
    }
}