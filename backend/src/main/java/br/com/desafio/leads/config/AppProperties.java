package br.com.desafio.leads.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Csv csv, Processing processing, Kafka kafka) {

    public record Csv(int chunkSize, String storageDir) {
    }

    public record Processing(int parallelism) {
    }

    public record Kafka(Topics topics) {
        public record Topics(String loteIniciado, String loteChunkConcluido, String loteFinalizado) {
        }
    }

}
