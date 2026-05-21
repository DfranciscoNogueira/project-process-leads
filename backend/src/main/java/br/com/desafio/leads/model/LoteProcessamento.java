package br.com.desafio.leads.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lote_processamento", uniqueConstraints = @UniqueConstraint(name = "uk_lote_chunk", columnNames = {"lote_id", "chunk_index"}))
public class LoteProcessamento {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "total_linhas", nullable = false)
    private int totalLinhas;

    @Column(name = "total_sucesso", nullable = false)
    private int totalSucesso;

    @Column(name = "total_erros", nullable = false)
    private int totalErros;

    @Column(name = "tempo_ms", nullable = false)
    private long tempoMs;

    @Column(name = "processado_em", nullable = false)
    private LocalDateTime processadoEm;

    @PrePersist
    void prePersist() {
        if (Objects.isNull(id)) id = UUID.randomUUID();
        if (Objects.isNull(processadoEm)) processadoEm = LocalDateTime.now();
    }

}
