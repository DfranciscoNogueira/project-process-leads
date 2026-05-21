package br.com.desafio.leads.service.processing;

import br.com.desafio.leads.config.AppProperties;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.service.csv.CsvLeadLine;
import br.com.desafio.leads.service.csv.CsvParserService;
import br.com.desafio.leads.service.event.LoteEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteProcessingServiceTest {

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private LeadChunkProcessor chunkProcessor;

    @Mock
    private LoteEventPublisher publisher;

    @Mock
    private AppProperties properties;

    @Mock
    private AppProperties.Csv csv;

    @Mock
    private AppProperties.Processing processing;

    @InjectMocks
    private LoteProcessingService service;

    @Test
    @DisplayName("Deve processar lote com sucesso e publicar finalização FINALIZADO")
    void deveProcessarLoteComSucesso() {
        UUID loteId = UUID.randomUUID();
        Path arquivo = Path.of("leads.csv");

        mockProperties();

        List<List<CsvLeadLine>> chunks = List.of(
                List.of(criarLead("Diego"), criarLead("Ana")),
                List.of(criarLead("Joao"))
        );

        when(csvParserService.lerEmChunks(arquivo, 2)).thenReturn(chunks);

        when(chunkProcessor.process(eq(loteId), eq(1), eq(chunks.get(0))))
                .thenReturn(new ChunkResult(2, 0, 2, 2, 22));

        when(chunkProcessor.process(eq(loteId), eq(2), eq(chunks.get(1))))
                .thenReturn(new ChunkResult(1, 0, 2, 3, 3));

        service.processar(loteId, arquivo);

        ArgumentCaptor<LoteIniciadoEvent> inicioCaptor =
                ArgumentCaptor.forClass(LoteIniciadoEvent.class);

        ArgumentCaptor<LoteFinalizadoEvent> finalizadoCaptor =
                ArgumentCaptor.forClass(LoteFinalizadoEvent.class);

        verify(publisher).publicarInicio(inicioCaptor.capture());
        verify(publisher).publicarFinalizacao(finalizadoCaptor.capture());

        assertEquals(loteId, inicioCaptor.getValue().loteId());
        assertEquals(3, inicioCaptor.getValue().totalLinhas());

        assertEquals(loteId, finalizadoCaptor.getValue().loteId());

        verify(chunkProcessor).process(loteId, 1, chunks.get(0));
        verify(chunkProcessor).process(loteId, 2, chunks.get(1));
    }

    @Test
    @DisplayName("Deve finalizar lote como FINALIZADO_COM_ERROS quando houver erros nos chunks")
    void deveFinalizarComErrosQuandoChunkRetornarErro() {
        UUID loteId = UUID.randomUUID();
        Path arquivo = Path.of("leads.csv");

        mockProperties();

        List<List<CsvLeadLine>> chunks = List.of(
                List.of(criarLead("Diego"), criarLead("Ana"))
        );

        when(csvParserService.lerEmChunks(arquivo, 2)).thenReturn(chunks);

        when(chunkProcessor.process(eq(loteId), eq(1), eq(chunks.get(0))))
                .thenReturn(new ChunkResult(1, 1, 1, 1, 3));

        service.processar(loteId, arquivo);

        ArgumentCaptor<LoteFinalizadoEvent> captor =
                ArgumentCaptor.forClass(LoteFinalizadoEvent.class);

        verify(publisher).publicarFinalizacao(captor.capture());

        assertEquals(loteId, captor.getValue().loteId());
        assertEquals(LoteStatus.FINALIZADO_COM_ERROS, captor.getValue().status());
        assertEquals(1, captor.getValue().sucesso());
        assertEquals(1, captor.getValue().erros());
    }

    @Test
    @DisplayName("Deve publicar lote FALHOU quando ocorrer erro no processamento")
    void devePublicarFalhaQuandoOcorrerErroNoProcessamento() {
        UUID loteId = UUID.randomUUID();
        Path arquivo = Path.of("leads.csv");

        mockProperties();

        List<List<CsvLeadLine>> chunks = List.of(
                List.of(criarLead("Diego"), criarLead("Ana"))
        );

        when(csvParserService.lerEmChunks(arquivo, 2)).thenReturn(chunks);

        when(chunkProcessor.process(any(), anyInt(), any()))
                .thenThrow(new RuntimeException("Erro ao processar chunk"));

        service.processar(loteId, arquivo);

        ArgumentCaptor<LoteFinalizadoEvent> captor =
                ArgumentCaptor.forClass(LoteFinalizadoEvent.class);

        verify(publisher).publicarFinalizacao(captor.capture());

        assertEquals(loteId, captor.getValue().loteId());
        assertEquals(LoteStatus.FALHOU, captor.getValue().status());
        assertEquals(0, captor.getValue().sucesso());
        assertEquals(2, captor.getValue().erros());
    }

    private void mockProperties() {
        when(properties.csv()).thenReturn(csv);
        when(csv.chunkSize()).thenReturn(2);

        when(properties.processing()).thenReturn(processing);
        when(processing.parallelism()).thenReturn(2);
    }

    private CsvLeadLine criarLead(String nome) {
        return new CsvLeadLine(
                nome,
                nome.toLowerCase() + "@email.com",
                "11999999999",
                "site",
                LocalDate.of(2026, 5, 21)
        );
    }
}