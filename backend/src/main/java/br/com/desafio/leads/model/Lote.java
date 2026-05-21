package br.com.desafio.leads.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "lote")
public class Lote {

    @Id
    private UUID id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "caminho_arquivo", nullable = false)
    private String caminhoArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoteStatus status;

    @Column(name = "total_linhas", nullable = false)
    private int totalLinhas;

    @Column(name = "total_sucesso", nullable = false)
    private int totalSucesso;

    @Column(name = "total_erros", nullable = false)
    private int totalErros;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @PrePersist
    void prePersist() {
        if (Objects.isNull(id)) id = UUID.randomUUID();
        if (Objects.isNull(criadoEm)) criadoEm = LocalDateTime.now();
        if (Objects.isNull(status)) status = LoteStatus.RECEBIDO;
    }

}
