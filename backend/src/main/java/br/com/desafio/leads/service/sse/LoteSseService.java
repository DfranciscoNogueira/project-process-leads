package br.com.desafio.leads.service.sse;

import br.com.desafio.leads.dto.LoteStatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LoteSseService {

    private final ConcurrentHashMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID loteId) {
        var emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(loteId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(loteId, emitter));
        emitter.onTimeout(() -> remove(loteId, emitter));
        emitter.onError(error -> remove(loteId, emitter));
        return emitter;
    }

    public void publish(UUID loteId, LoteStatusResponse status) {
        var subscribers = emitters.getOrDefault(loteId, List.of());
        subscribers.forEach(emitter -> send(loteId, emitter, status));
    }

    private void send(UUID loteId, SseEmitter emitter, LoteStatusResponse status) {
        try {
            emitter.send(SseEmitter.event().name("lote-status").data(status));
        } catch (IOException e) {
            remove(loteId, emitter);
        }
    }

    private void remove(UUID loteId, SseEmitter emitter) {
        emitters.getOrDefault(loteId, List.of()).remove(emitter);
    }

}
