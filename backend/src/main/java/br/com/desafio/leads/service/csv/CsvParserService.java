package br.com.desafio.leads.service.csv;

import br.com.desafio.leads.exception.BusinessException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvParserService {

    private static final List<String> HEADERS = List.of("nome", "email", "telefone", "origem", "data_cadastro");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter BR_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    public void validarUtf8EHeaders(Path file) {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            var parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader);
            var headers = new ArrayList<>(parser.getHeaderMap().keySet());
            if (!headers.containsAll(HEADERS)) {
                throw new BusinessException("CSV inválido. Cabeçalho esperado: nome,email,telefone,origem,data_cadastro");
            }
        } catch (CharacterCodingException e) {
            throw new BusinessException("Arquivo deve estar em encoding UTF-8.");
        } catch (IOException e) {
            throw new BusinessException("Não foi possível ler o CSV: " + e.getMessage());
        }
    }

    public List<List<CsvLeadLine>> lerEmChunks(Path file, int chunkSize) {
        var chunks = new ArrayList<List<CsvLeadLine>>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build().parse(reader)) {
            var current = new ArrayList<CsvLeadLine>(chunkSize);
            for (var csvRecord : parser) {
                current.add(new CsvLeadLine(
                        csvRecord.get("nome"),
                        csvRecord.get("email"),
                        csvRecord.get("telefone"),
                        csvRecord.get("origem"),
                        parseDataCadastro(csvRecord.get("data_cadastro"))
                ));
                if (current.size() == chunkSize) {
                    chunks.add(List.copyOf(current));
                    current.clear();
                }
            }
            if (!current.isEmpty()) chunks.add(List.copyOf(current));
            return chunks;
        } catch (Exception e) {
            throw new BusinessException("Falha ao fazer parsing do CSV: " + e.getMessage());
        }
    }

    public int contarLinhas(Path file) {
        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return (int) Math.max(0, lines.count() - 1);
        } catch (IOException e) {
            throw new BusinessException("Não foi possível contar linhas do CSV: " + e.getMessage());
        }
    }

    private LocalDate parseDataCadastro(String value) {
        try {
            return LocalDate.parse(value, ISO_DATE);
        } catch (Exception ignored) {
            return LocalDateTime.parse(value, BR_DATE_TIME).toLocalDate();
        }
    }

}
