package br.com.desafio.leads.service.processing;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.model.Lead;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.repository.LeadRepository;
import br.com.desafio.leads.repository.LoteRepository;
import br.com.desafio.leads.service.csv.CsvLeadLine;
import br.com.desafio.leads.service.event.LoteEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadChunkProcessorTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private LoteEventPublisher publisher;

    @InjectMocks
    private LeadChunkProcessor processor;

    @Test
    @DisplayName("Deve processar chunk com leads válidos")
    void deveProcessarChunkComLeadsValidos() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).build();

        List<CsvLeadLine> lines = List.of(
                criarLinha("Diego", "DIEGO@EMAIL.COM", "Site"),
                criarLinha("Ana", "ana@email.com", "Google")
        );

        when(loteRepository.getReferenceById(loteId)).thenReturn(lote);
        when(leadRepository.existsByEmailAndOrigem(anyString(), anyString()))
                .thenReturn(false);

        ChunkResult result = processor.process(loteId, 1, lines);

        assertEquals(1, result.chunkIndex());
        assertEquals(2, result.sucesso());
        assertEquals(0, result.erros());

        verify(leadRepository, times(2)).save(any(Lead.class));
        verify(publisher).publicarChunkConcluido(any(LoteChunkConcluidoEvent.class));
    }

    @Test
    @DisplayName("Deve ignorar lead inválido")
    void deveIgnorarLeadInvalido() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).build();

        List<CsvLeadLine> lines = List.of(
                criarLinha("Diego", "email-invalido", "Site")
        );

        when(loteRepository.getReferenceById(loteId)).thenReturn(lote);

        ChunkResult result = processor.process(loteId, 1, lines);

        assertEquals(0, result.sucesso());
        assertEquals(1, result.erros());

        verify(leadRepository, never()).save(any());
        verify(leadRepository, never()).existsByEmailAndOrigem(anyString(), anyString());
        verify(publisher).publicarChunkConcluido(any(LoteChunkConcluidoEvent.class));
    }

    @Test
    @DisplayName("Deve ignorar duplicidade dentro do mesmo chunk")
    void deveIgnorarDuplicidadeNoMesmoChunk() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).build();

        List<CsvLeadLine> lines = List.of(
                criarLinha("Diego", "diego@email.com", "Site"),
                criarLinha("Diego Duplicado", " DIEGO@EMAIL.COM ", " site ")
        );

        when(loteRepository.getReferenceById(loteId)).thenReturn(lote);
        when(leadRepository.existsByEmailAndOrigem("diego@email.com", "site"))
                .thenReturn(false);

        ChunkResult result = processor.process(loteId, 1, lines);

        assertEquals(1, result.sucesso());
        assertEquals(1, result.erros());

        verify(leadRepository, times(1)).save(any(Lead.class));
        verify(leadRepository, times(1))
                .existsByEmailAndOrigem("diego@email.com", "site");
    }

    @Test
    @DisplayName("Deve ignorar lead já existente no banco")
    void deveIgnorarLeadJaExistenteNoBanco() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).build();

        List<CsvLeadLine> lines = List.of(
                criarLinha("Diego", "diego@email.com", "Site")
        );

        when(loteRepository.getReferenceById(loteId)).thenReturn(lote);
        when(leadRepository.existsByEmailAndOrigem("diego@email.com", "site"))
                .thenReturn(true);

        ChunkResult result = processor.process(loteId, 1, lines);

        assertEquals(0, result.sucesso());
        assertEquals(1, result.erros());

        verify(leadRepository, never()).save(any());
        verify(leadRepository).existsByEmailAndOrigem("diego@email.com", "site");
    }

    @Test
    @DisplayName("Deve tratar DataIntegrityViolationException como erro")
    void deveTratarDuplicidadePorConcorrenciaComoErro() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).build();

        List<CsvLeadLine> lines = List.of(
                criarLinha("Diego", "diego@email.com", "Site")
        );

        when(loteRepository.getReferenceById(loteId)).thenReturn(lote);
        when(leadRepository.existsByEmailAndOrigem("diego@email.com", "site"))
                .thenReturn(false);

        when(leadRepository.save(any(Lead.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicado"));

        ChunkResult result = processor.process(loteId, 1, lines);

        assertEquals(0, result.sucesso());
        assertEquals(1, result.erros());

        verify(leadRepository).save(any(Lead.class));
        verify(publisher).publicarChunkConcluido(any(LoteChunkConcluidoEvent.class));
    }

    @Test
    @DisplayName("Deve publicar estatísticas do chunk processado")
    void devePublicarEstatisticasDoChunkProcessado() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).build();

        List<CsvLeadLine> lines = List.of(
                criarLinha("Diego", "diego@email.com", "Site"),
                criarLinha("Ana", "email-invalido", "Google")
        );

        when(loteRepository.getReferenceById(loteId)).thenReturn(lote);
        when(leadRepository.existsByEmailAndOrigem("diego@email.com", "site"))
                .thenReturn(false);

        processor.process(loteId, 3, lines);

        ArgumentCaptor<LoteChunkConcluidoEvent> eventCaptor =
                ArgumentCaptor.forClass(LoteChunkConcluidoEvent.class);

        verify(publisher).publicarChunkConcluido(eventCaptor.capture());

        LoteChunkConcluidoEvent event = eventCaptor.getValue();

        assertEquals(loteId, event.loteId());
        assertEquals(3, event.chunkIndex());
        assertEquals(2, event.totalLinhas());
        assertEquals(1, event.sucesso());
        assertEquals(1, event.erros());
    }

    private CsvLeadLine criarLinha(String nome, String email, String origem) {
        return new CsvLeadLine(
                nome,
                email,
                "11999999999",
                origem,
                LocalDate.of(2026, 5, 21)
        );
    }
}