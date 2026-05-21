package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.LoteUploadResponse;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.repository.LoteRepository;
import br.com.desafio.leads.service.csv.CsvParserService;
import br.com.desafio.leads.service.processing.LoteProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final FileStorageService fileStorageService;
    private final CsvParserService csvParserService;
    private final LoteRepository loteRepository;
    private final LoteProcessingService processingService;

    @Transactional
    public LoteUploadResponse upload(MultipartFile file) {
        var savedFile = fileStorageService.save(file);
        csvParserService.validarUtf8EHeaders(savedFile);
        var totalLinhas = csvParserService.contarLinhas(savedFile);
        var lote = Lote.builder()
                .nomeArquivo(file.getOriginalFilename())
                .caminhoArquivo(savedFile.toAbsolutePath().toString())
                .status(LoteStatus.RECEBIDO)
                .totalLinhas(totalLinhas)
                .build();
        loteRepository.save(lote);
        processingService.processar(lote.getId(), savedFile);
        return new LoteUploadResponse(lote.getId(), lote.getStatus().name(), "Arquivo recebido. Processamento iniciado em segundo plano.");
    }

}
