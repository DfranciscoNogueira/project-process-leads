package br.com.desafio.leads.service.event;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.service.LoteProgressService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaLoteEventConsumer {

    private final LoteProgressService progressService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.lote-iniciado}")
    public void onInicio(String payload) {
        LoteIniciadoEvent event = read(payload, LoteIniciadoEvent.class);
        progressService.onLoteIniciado(event);
    }

    @KafkaListener(topics = "${app.kafka.topics.lote-chunk-concluido}")
    public void onChunk(String payload) {
        LoteChunkConcluidoEvent event = read(payload, LoteChunkConcluidoEvent.class);
        progressService.onChunkConcluido(event);
    }

    @KafkaListener(topics = "${app.kafka.topics.lote-finalizado}")
    public void onFinalizado(String payload) {
        LoteFinalizadoEvent event = read(payload, LoteFinalizadoEvent.class);
        progressService.onLoteFinalizado(event);
    }

    private <T> T read(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException exception) {
            log.error("Erro ao converter evento Kafka para {}. Payload recebido: {}", type.getSimpleName(), payload, exception);
            throw new IllegalArgumentException("Evento Kafka inválido: " + type.getSimpleName(), exception);
        }
    }

}
