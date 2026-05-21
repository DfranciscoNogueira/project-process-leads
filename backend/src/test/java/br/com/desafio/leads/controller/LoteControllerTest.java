package br.com.desafio.leads.controller;

import br.com.desafio.leads.dto.LoteStatusResponse;
import br.com.desafio.leads.dto.LoteUploadResponse;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.service.LoteProgressService;
import br.com.desafio.leads.service.LoteService;
import br.com.desafio.leads.service.sse.LoteSseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoteController.class)
class LoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoteService loteService;

    @MockBean
    private LoteProgressService progressService;

    @MockBean
    private LoteSseService sseService;

    @Test
    @DisplayName("Deve fazer upload de CSV e iniciar processamento")
    void deveFazerUploadCsv() throws Exception {
        UUID loteId = UUID.randomUUID();

        var file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                """
                        nome,email,telefone,origem,data_cadastro
                        Diego,diego@email.com,11999999999,site,2026-05-21
                        """.getBytes()
        );

        when(loteService.upload(any()))
                .thenReturn(new LoteUploadResponse(
                        loteId,
                        "RECEBIDO",
                        "Arquivo recebido. Processamento iniciado em segundo plano."
                ));

        mockMvc.perform(
                        multipart("/api/lotes")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loteId").value(loteId.toString()))
                .andExpect(jsonPath("$.status").value("RECEBIDO"));

        verify(loteService).upload(any());
    }

    @Test
    @DisplayName("Deve consultar status do lote")
    void deveConsultarStatusDoLote() throws Exception {
        UUID loteId = UUID.randomUUID();

        LoteStatusResponse response = new LoteStatusResponse(
                loteId,
                LoteStatus.PROCESSANDO,
                100,
                40,
                5,
                45,
                LocalDateTime.of(2026, 5, 21, 10, 0),
                LocalDateTime.of(2026, 5, 21, 10, 1),
                null
        );

        when(progressService.status(loteId))
                .thenReturn(response);

        mockMvc.perform(get("/api/lotes/{id}/status", loteId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loteId").value(loteId.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSANDO"))
                .andExpect(jsonPath("$.totalLinhas").value(100))
                .andExpect(jsonPath("$.totalSucesso").value(40))
                .andExpect(jsonPath("$.totalErros").value(5));

        verify(progressService).status(loteId);
    }

    @Test
    @DisplayName("Deve assinar stream SSE do lote")
    void deveAssinarStreamSseDoLote() throws Exception {
        UUID loteId = UUID.randomUUID();

        SseEmitter emitter = new SseEmitter(0L);

        when(sseService.subscribe(loteId))
                .thenReturn(emitter);

        mockMvc.perform(get("/api/lotes/{id}/stream", loteId))
                .andExpect(status().isOk());

        verify(sseService).subscribe(loteId);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando UUID do lote for inválido")
    void deveRetornarErroQuandoUuidInvalido() throws Exception {
        mockMvc.perform(get("/api/lotes/{id}/status", "uuid-invalido"))
                .andExpect(status().is5xxServerError());
    }
}