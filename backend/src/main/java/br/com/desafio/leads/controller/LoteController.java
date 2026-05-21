package br.com.desafio.leads.controller;

import br.com.desafio.leads.dto.LoteStatusResponse;
import br.com.desafio.leads.dto.LoteUploadResponse;
import br.com.desafio.leads.service.LoteProgressService;
import br.com.desafio.leads.service.LoteService;
import br.com.desafio.leads.service.sse.LoteSseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoteController {

    private final LoteService loteService;
    private final LoteProgressService progressService;
    private final LoteSseService sseService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Recebe CSV de leads e inicia processamento assíncrono")
    public LoteUploadResponse upload(@RequestPart("file") MultipartFile file) {
        return loteService.upload(file);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Consulta status atual do lote")
    public LoteStatusResponse status(@PathVariable UUID id) {
        return progressService.status(id);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Assina eventos SSE de progresso do lote")
    public SseEmitter stream(@PathVariable UUID id) {
        return sseService.subscribe(id);
    }

}
