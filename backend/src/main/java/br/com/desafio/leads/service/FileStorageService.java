package br.com.desafio.leads.service;

import br.com.desafio.leads.config.AppProperties;
import br.com.desafio.leads.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AppProperties properties;

    public Path save(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("Arquivo CSV não pode estar vazio.");
        var original = file.getOriginalFilename() == null ? "leads.csv" : file.getOriginalFilename();
        if (!original.toLowerCase().endsWith(".csv")) throw new BusinessException("Envie um arquivo .csv.");
        try {
            var storageDir = Path.of(properties.csv().storageDir());
            Files.createDirectories(storageDir);
            var destination = storageDir.resolve(UUID.randomUUID() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_"));
            file.transferTo(destination);
            return destination;
        } catch (IOException e) {
            throw new BusinessException("Falha ao salvar arquivo: " + e.getMessage());
        }
    }

}
