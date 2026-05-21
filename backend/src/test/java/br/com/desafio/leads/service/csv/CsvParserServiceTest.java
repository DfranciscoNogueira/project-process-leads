package br.com.desafio.leads.service.csv;

import br.com.desafio.leads.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CsvParserServiceTest {

    private final CsvParserService service = new CsvParserService();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve validar CSV UTF-8 com cabeçalho correto")
    void deveValidarUtf8EHeadersComSucesso() throws Exception {
        Path file = criarArquivoCsv("""
                nome,email,telefone,origem,data_cadastro
                Diego,diego@email.com,11999999999,site,2026-05-21
                """);

        assertDoesNotThrow(() -> service.validarUtf8EHeaders(file));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando cabeçalho estiver inválido")
    void deveLancarErroQuandoHeaderInvalido() throws Exception {
        Path file = criarArquivoCsv("""
                nome,email,telefone
                Diego,diego@email.com,11999999999
                """);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validarUtf8EHeaders(file)
        );

        assertEquals(
                "CSV inválido. Cabeçalho esperado: nome,email,telefone,origem,data_cadastro",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Deve ler CSV em chunks conforme tamanho informado")
    void deveLerCsvEmChunks() throws Exception {
        Path file = criarArquivoCsv("""
                nome,email,telefone,origem,data_cadastro
                Diego,diego@email.com,11999999999,site,2026-05-21
                Ana,ana@email.com,11988888888,google,2026-05-20
                Joao,joao@email.com,11977777777,facebook,2026-05-19
                """);

        var chunks = service.lerEmChunks(file, 2);

        assertEquals(2, chunks.size());

        assertEquals(2, chunks.get(0).size());
        assertEquals(1, chunks.get(1).size());

        assertEquals("Diego", chunks.get(0).get(0).nome());
        assertEquals("diego@email.com", chunks.get(0).get(0).email());
        assertEquals("site", chunks.get(0).get(0).origem());

        assertEquals("Joao", chunks.get(1).get(0).nome());
        assertEquals("joao@email.com", chunks.get(1).get(0).email());
    }

    @Test
    @DisplayName("Deve contar linhas desconsiderando cabeçalho")
    void deveContarLinhasDesconsiderandoHeader() throws Exception {
        Path file = criarArquivoCsv("""
                nome,email,telefone,origem,data_cadastro
                Diego,diego@email.com,11999999999,site,2026-05-21
                Ana,ana@email.com,11988888888,google,2026-05-20
                """);

        int total = service.contarLinhas(file);

        assertEquals(2, total);
    }

    @Test
    @DisplayName("Deve retornar zero quando CSV possuir apenas cabeçalho")
    void deveRetornarZeroQuandoCsvPossuirApenasHeader() throws Exception {
        Path file = criarArquivoCsv("""
                nome,email,telefone,origem,data_cadastro
                """);

        int total = service.contarLinhas(file);

        assertEquals(0, total);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando data estiver inválida")
    void deveLancarErroQuandoDataInvalida() throws Exception {
        Path file = criarArquivoCsv("""
                nome,email,telefone,origem,data_cadastro
                Diego,diego@email.com,11999999999,site,21/05/2026
                """);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.lerEmChunks(file, 2)
        );

        assertTrue(exception.getMessage().contains("Falha ao fazer parsing do CSV"));
    }

    private Path criarArquivoCsv(String conteudo) throws Exception {
        Path file = tempDir.resolve("leads.csv");
        Files.writeString(file, conteudo, StandardCharsets.UTF_8);
        return file;
    }

}
