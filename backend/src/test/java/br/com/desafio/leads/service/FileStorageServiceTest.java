package br.com.desafio.leads.service;

import br.com.desafio.leads.config.AppProperties;
import br.com.desafio.leads.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private AppProperties properties;

    @Mock
    private AppProperties.Csv csv;

    @InjectMocks
    private FileStorageService service;

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve salvar arquivo CSV com sucesso")
    void deveSalvarArquivoCsvComSucesso() throws Exception {
        mockStorageDir();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                """
                        nome,email,telefone,origem,data_cadastro
                        Diego,diego@email.com,11999999999,site,2026-05-21
                        """.getBytes()
        );

        Path savedPath = service.save(file);

        assertNotNull(savedPath);
        assertTrue(Files.exists(savedPath));
        assertTrue(savedPath.getFileName().toString().endsWith("leads.csv"));

        String content = Files.readString(savedPath);

        assertTrue(content.contains("Diego"));
    }

    @Test
    @DisplayName("Deve salvar arquivo com nome sanitizado")
    void deveSalvarArquivoComNomeSanitizado() {
        mockStorageDir();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lead@s teste#.csv",
                "text/csv",
                "conteudo".getBytes()
        );

        Path savedPath = service.save(file);

        assertTrue(
                savedPath.getFileName()
                        .toString()
                        .contains("lead_s_teste_.csv")
        );
    }

    @Test
    @DisplayName("Deve usar nome padrão quando nome original for nulo")
    void deveUsarNomePadraoQuandoNomeOriginalForNulo() throws IOException {
        mockStorageDir();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        Path savedPath = service.save(file);

        assertTrue(
                savedPath.getFileName()
                        .toString()
                        .endsWith("leads.csv")
        );

        verify(file).transferTo(any(Path.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo estiver vazio")
    void deveLancarErroQuandoArquivoVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                new byte[0]
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.save(file)
                );

        assertEquals(
                "Arquivo CSV não pode estar vazio.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando arquivo não for CSV")
    void deveLancarErroQuandoArquivoNaoForCsv() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.save(file)
                );

        assertEquals(
                "Envie um arquivo .csv.",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve lançar exceção quando ocorrer IOException")
    void deveLancarErroQuandoFalharAoSalvarArquivo() throws Exception {
        mockStorageDir();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("leads.csv");

        doThrow(new IOException("Erro disco"))
                .when(file)
                .transferTo(any(Path.class));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.save(file)
                );

        assertEquals(
                "Falha ao salvar arquivo: Erro disco",
                exception.getMessage()
        );
    }

    private void mockStorageDir() {
        when(properties.csv()).thenReturn(csv);
        when(csv.storageDir())
                .thenReturn(tempDir.toString());
    }
}