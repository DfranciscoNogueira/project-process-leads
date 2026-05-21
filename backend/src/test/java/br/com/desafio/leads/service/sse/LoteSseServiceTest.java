package br.com.desafio.leads.service.sse;

import br.com.desafio.leads.dto.LoteStatusResponse;
import br.com.desafio.leads.model.LoteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoteSseServiceTest {

    private final LoteSseService service = new LoteSseService();

    @Test
    @DisplayName("Deve criar inscrição SSE para um lote")
    void deveCriarInscricaoSseParaLote() {
        UUID loteId = UUID.randomUUID();

        SseEmitter emitter = service.subscribe(loteId);

        assertNotNull(emitter);
    }

    @Test
    @DisplayName("Deve permitir publicar status para lote com subscriber")
    void devePublicarStatusParaLoteComSubscriber() {
        UUID loteId = UUID.randomUUID();

        service.subscribe(loteId);

        LoteStatusResponse status = criarStatus(loteId);

        assertDoesNotThrow(() -> service.publish(loteId, status));
    }

    @Test
    @DisplayName("Deve permitir publicar status para lote sem subscriber")
    void devePublicarStatusParaLoteSemSubscriber() {
        UUID loteId = UUID.randomUUID();

        LoteStatusResponse status = criarStatus(loteId);

        assertDoesNotThrow(() -> service.publish(loteId, status));
    }

    private LoteStatusResponse criarStatus(UUID loteId) {
        return new LoteStatusResponse(
                loteId,
                LoteStatus.PROCESSANDO,
                100,
                40,
                35,
                5,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

}