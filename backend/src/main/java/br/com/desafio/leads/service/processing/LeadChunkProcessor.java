package br.com.desafio.leads.service.processing;

import br.com.desafio.leads.event.LoteChunkConcluidoEvent;
import br.com.desafio.leads.model.Lead;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.repository.LeadRepository;
import br.com.desafio.leads.repository.LoteRepository;
import br.com.desafio.leads.service.csv.CsvLeadLine;
import br.com.desafio.leads.service.event.LoteEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LeadChunkProcessor {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final LeadRepository leadRepository;
    private final LoteRepository loteRepository;
    private final LoteEventPublisher publisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChunkResult process(UUID loteId, int chunkIndex, List<CsvLeadLine> lines) {
        var started = System.nanoTime();
        var lote = loteRepository.getReferenceById(loteId);
        var seenInChunk = new HashSet<String>();
        var sucesso = 0;
        var erros = 0;

        for (var line : lines) {
            try {
                var key = normalize(line.email()) + "|" + normalize(line.origem());
                if (!isValid(line) || !seenInChunk.add(key) || leadRepository.existsByEmailAndOrigem(normalize(line.email()), normalize(line.origem()))) {
                    erros++;
                    continue;
                }
                leadRepository.save(toEntity(line, lote));
                sucesso++;
            } catch (Exception duplicatedByConcurrency) {
                erros++;
            }
        }

        var elapsedMs = (System.nanoTime() - started) / 1_000_000;
        publisher.publicarChunkConcluido(new LoteChunkConcluidoEvent(loteId, chunkIndex, lines.size(), sucesso, erros, elapsedMs, LocalDateTime.now()));
        return new ChunkResult(chunkIndex, lines.size(), sucesso, erros, elapsedMs);
    }

    private boolean isValid(CsvLeadLine line) {
        return hasText(line.nome()) && EMAIL_PATTERN.matcher(line.email()).matches() && hasText(line.telefone()) && hasText(line.origem()) && line.dataCadastro() != null;
    }

    private Lead toEntity(CsvLeadLine line, Lote lote) {
        return Lead.builder()
                .nome(line.nome().trim())
                .email(normalize(line.email()))
                .telefone(line.telefone().trim())
                .origem(normalize(line.origem()))
                .dataCadastro(line.dataCadastro())
                .lote(lote)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
