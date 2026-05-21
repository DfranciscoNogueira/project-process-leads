package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.LoteUploadResponse;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.repository.LoteRepository;
import br.com.desafio.leads.service.csv.CsvParserService;
import br.com.desafio.leads.service.processing.LoteProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private LoteProcessingService processingService;

    @InjectMocks
    private LoteService loteService;

    @Test
    @DisplayName("Deve salvar arquivo, criar lote e iniciar processamento")
    void deveSalvarArquivoCriarLoteEIniciarProcessamento() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                "nome,email,telefone,origem,data_cadastro\nDiego,diego@email.com,11999999999,site,2026-05-21".getBytes()
        );

        UUID loteId = UUID.randomUUID();
        Path savedFile = Path.of("uploads", "leads.csv").toAbsolutePath();

        when(fileStorageService.save(file)).thenReturn(savedFile);
        when(csvParserService.contarLinhas(savedFile)).thenReturn(1);

        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> {
            Lote lote = invocation.getArgument(0);
            lote.setId(loteId);
            return lote;
        });

        LoteUploadResponse response = loteService.upload(file);

        ArgumentCaptor<Lote> loteCaptor = ArgumentCaptor.forClass(Lote.class);

        verify(fileStorageService).save(file);
        verify(csvParserService).validarUtf8EHeaders(savedFile);
        verify(csvParserService).contarLinhas(savedFile);
        verify(loteRepository).save(loteCaptor.capture());
        verify(processingService).processar(loteId, savedFile);

        Lote loteSalvo = loteCaptor.getValue();

        assertEquals("leads.csv", loteSalvo.getNomeArquivo());
        assertEquals(savedFile.toAbsolutePath().toString(), loteSalvo.getCaminhoArquivo());
        assertEquals(LoteStatus.RECEBIDO, loteSalvo.getStatus());
        assertEquals(1, loteSalvo.getTotalLinhas());

        assertNotNull(response);
        assertEquals(loteId, response.loteId());
        assertEquals("RECEBIDO", response.status());
    }

    @Test
    @DisplayName("Não deve criar lote quando validação do CSV falhar")
    void naoDeveCriarLoteQuandoValidacaoFalhar() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                "conteudo-invalido".getBytes()
        );

        Path savedFile = Path.of("uploads", "leads.csv").toAbsolutePath();

        when(fileStorageService.save(file)).thenReturn(savedFile);

        doThrow(new RuntimeException("CSV inválido"))
                .when(csvParserService)
                .validarUtf8EHeaders(savedFile);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loteService.upload(file)
        );

        assertEquals("CSV inválido", exception.getMessage());

        verify(fileStorageService).save(file);
        verify(csvParserService).validarUtf8EHeaders(savedFile);
        verify(csvParserService, never()).contarLinhas(any());
        verify(loteRepository, never()).save(any());
        verify(processingService, never()).processar(any(), any());
    }

    @Test
    @DisplayName("Não deve iniciar processamento quando falhar ao salvar o lote")
    void naoDeveIniciarProcessamentoQuandoFalharAoSalvarLote() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                "nome,email,telefone,origem,data_cadastro\nDiego,diego@email.com,11999999999,site,2026-05-21".getBytes()
        );

        Path savedFile = Path.of("uploads", "leads.csv").toAbsolutePath();

        when(fileStorageService.save(file)).thenReturn(savedFile);
        when(csvParserService.contarLinhas(savedFile)).thenReturn(1);
        when(loteRepository.save(any(Lote.class)))
                .thenThrow(new RuntimeException("Erro ao salvar lote"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loteService.upload(file)
        );

        assertEquals("Erro ao salvar lote", exception.getMessage());

        verify(fileStorageService).save(file);
        verify(csvParserService).validarUtf8EHeaders(savedFile);
        verify(csvParserService).contarLinhas(savedFile);
        verify(loteRepository).save(any(Lote.class));
        verify(processingService, never()).processar(any(), any());
    }
}