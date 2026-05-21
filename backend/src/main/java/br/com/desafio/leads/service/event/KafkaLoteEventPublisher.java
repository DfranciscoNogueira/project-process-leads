package br.com.desafio.leads.service.event;

import br.com.desafio.leads.config.AppProperties;
import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaLoteEventPublisher implements LoteEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppProperties properties;

    @Override
    public void publicarInicio(LoteIniciadoEvent event) {
        kafkaTemplate.send(properties.kafka().topics().loteIniciado(), event.loteId().toString(), event);
    }

    @Override
    public void publicarChunkConcluido(LoteChunkConcluidoEvent event) {
        kafkaTemplate.send(properties.kafka().topics().loteChunkConcluido(), event.loteId().toString(), event);
    }

    @Override
    public void publicarFinalizacao(LoteFinalizadoEvent event) {
        kafkaTemplate.send(properties.kafka().topics().loteFinalizado(), event.loteId().toString(), event);
    }

}
