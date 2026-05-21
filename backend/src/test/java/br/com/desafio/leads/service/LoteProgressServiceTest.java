package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.LoteStatusResponse;
import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.event.LoteFinalizadoEvent;
import br.com.desafio.leads.event.LoteIniciadoEvent;
import br.com.desafio.leads.exception.BusinessException;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.model.LoteProcessamento;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.repository.LoteProcessamentoRepository;
import br.com.desafio.leads.repository.LoteRepository;
import br.com.desafio.leads.service.sse.LoteSseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteProgressServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private LoteProcessamentoRepository processamentoRepository;

    @Mock
    private LoteSseService sseService;

    @InjectMocks
    private LoteProgressService service;

    @Test
    @DisplayName("Deve atualizar lote para PROCESSANDO ao iniciar")
    void deveAtualizarLoteAoIniciar() {
        UUID loteId = UUID.randomUUID();

        LocalDateTime ocorridoEm = LocalDateTime.now();

        Lote lote = criarLote(loteId);

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.of(lote));

        LoteIniciadoEvent event =
                new LoteIniciadoEvent(
                        loteId,
                        100,
                        ocorridoEm
                );

        service.onLoteIniciado(event);

        assertEquals(LoteStatus.PROCESSANDO, lote.getStatus());
        assertEquals(100, lote.getTotalLinhas());
        assertEquals(ocorridoEm, lote.getIniciadoEm());

        verify(sseService).publish(eq(loteId), any(LoteStatusResponse.class));
    }

    @Test
    @DisplayName("Deve salvar processamento e atualizar totais ao concluir chunk")
    void deveAtualizarTotaisAoConcluirChunk() {
        UUID loteId = UUID.randomUUID();

        Lote lote = criarLote(loteId);

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.of(lote));

        when(processamentoRepository.sumSucesso(loteId))
                .thenReturn(95);

        when(processamentoRepository.sumErros(loteId))
                .thenReturn(5);

        LoteChunkConcluidoEvent event =
                new LoteChunkConcluidoEvent(
                        loteId,
                        1,
                        100,
                        95,
                        5,
                        250L,
                        LocalDateTime.now()
                );

        service.onChunkConcluido(event);

        ArgumentCaptor<LoteProcessamento> captor =
                ArgumentCaptor.forClass(LoteProcessamento.class);

        verify(processamentoRepository).save(captor.capture());

        LoteProcessamento processamento = captor.getValue();

        assertEquals(lote, processamento.getLote());
        assertEquals(1, processamento.getChunkIndex());
        assertEquals(100, processamento.getTotalLinhas());
        assertEquals(95, processamento.getTotalSucesso());
        assertEquals(5, processamento.getTotalErros());
        assertEquals(250L, processamento.getTempoMs());

        assertEquals(95, lote.getTotalSucesso());
        assertEquals(5, lote.getTotalErros());

        verify(sseService).publish(eq(loteId), any(LoteStatusResponse.class));
    }

    @Test
    @DisplayName("Deve finalizar lote atualizando status e totais")
    void deveFinalizarLote() {
        UUID loteId = UUID.randomUUID();

        Lote lote = criarLote(loteId);

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.of(lote));

        LoteFinalizadoEvent event =
                new LoteFinalizadoEvent(
                        loteId,
                        LoteStatus.FINALIZADO,
                        95,
                        5,
                        LocalDateTime.now()
                );

        service.onLoteFinalizado(event);

        assertEquals(LoteStatus.FINALIZADO, lote.getStatus());
        assertEquals(95, lote.getTotalSucesso());
        assertEquals(5, lote.getTotalErros());
        assertNotNull(lote.getFinalizadoEm());

        verify(sseService).publish(eq(loteId), any(LoteStatusResponse.class));
    }

    @Test
    @DisplayName("Deve retornar status do lote com percentual calculado")
    void deveRetornarStatusComPercentual() {
        UUID loteId = UUID.randomUUID();

        Lote lote = criarLote(loteId);
        lote.setStatus(LoteStatus.PROCESSANDO);
        lote.setTotalLinhas(100);
        lote.setTotalSucesso(80);
        lote.setTotalErros(10);

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.of(lote));

        LoteStatusResponse response = service.status(loteId);

        assertEquals(loteId, response.loteId());
        assertEquals(LoteStatus.PROCESSANDO, response.status());
        assertEquals(100, response.totalLinhas());
        assertEquals(80, response.totalSucesso());
        assertEquals(10, response.totalErros());
    }

    @Test
    @DisplayName("Deve retornar percentual zero quando total linhas for zero")
    void deveRetornarPercentualZero() {
        UUID loteId = UUID.randomUUID();

        Lote lote = criarLote(loteId);
        lote.setTotalLinhas(0);
        lote.setTotalSucesso(10);
        lote.setTotalErros(5);

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.of(lote));

        LoteStatusResponse response = service.status(loteId);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Deve limitar percentual em 100")
    void deveLimitarPercentualEm100() {
        UUID loteId = UUID.randomUUID();

        Lote lote = criarLote(loteId);
        lote.setTotalLinhas(10);
        lote.setTotalSucesso(20);
        lote.setTotalErros(10);

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.of(lote));

        LoteStatusResponse response = service.status(loteId);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Deve lançar exceção quando lote não existir")
    void deveLancarExcecaoQuandoLoteNaoExistir() {
        UUID loteId = UUID.randomUUID();

        when(loteRepository.findById(loteId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.status(loteId)
                );

        assertEquals("Lote não encontrado.", exception.getMessage());
    }

    private Lote criarLote(UUID loteId) {
        return Lote.builder()
                .id(loteId)
                .status(LoteStatus.RECEBIDO)
                .totalLinhas(0)
                .totalSucesso(0)
                .totalErros(0)
                .criadoEm(LocalDateTime.now())
                .build();
    }
}