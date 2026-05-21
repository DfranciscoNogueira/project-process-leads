package br.com.desafio.leads.service.processing;

import br.com.desafio.leads.config.AppProperties;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.service.csv.CsvParserService;
import br.com.desafio.leads.service.event.LoteEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class LoteProcessingService {

    private final CsvParserService csvParserService;
    private final LeadChunkProcessor chunkProcessor;
    private final LoteEventPublisher publisher;
    private final AppProperties properties;

    @Async
    public void processar(UUID loteId, Path arquivo) {
        int totalLinhas = 0;

        try {
            var chunks = csvParserService.lerEmChunks(arquivo, properties.csv().chunkSize());
            totalLinhas = chunks.stream().mapToInt(java.util.List::size).sum();

            publisher.publicarInicio(new LoteIniciadoEvent(loteId, totalLinhas, LocalDateTime.now()));

            try (var executor = Executors.newFixedThreadPool(
                    properties.processing().parallelism(),
                    Thread.ofVirtual().factory()
            )) {
                var futures = java.util.stream.IntStream.range(0, chunks.size())
                        .mapToObj(index -> CompletableFuture.supplyAsync(
                                () -> chunkProcessor.process(loteId, index + 1, chunks.get(index)),
                                executor
                        ))
                        .toList();

                var results = futures.stream().map(CompletableFuture::join).toList();

                var sucesso = results.stream().mapToInt(ChunkResult::sucesso).sum();
                var erros = results.stream().mapToInt(ChunkResult::erros).sum();

                var status = erros == 0
                        ? LoteStatus.FINALIZADO
                        : LoteStatus.FINALIZADO_COM_ERROS;

                publisher.publicarFinalizacao(
                        new LoteFinalizadoEvent(loteId, status, sucesso, erros, LocalDateTime.now())
                );
            }
        } catch (Exception e) {
            publisher.publicarFinalizacao(
                    new LoteFinalizadoEvent(loteId, LoteStatus.FALHOU, 0, totalLinhas, LocalDateTime.now())
            );
        }
    }

}
