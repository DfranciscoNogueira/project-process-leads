package br.com.desafio.leads.repository;

import br.com.desafio.leads.model.LoteProcessamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface LoteProcessamentoRepository extends JpaRepository<LoteProcessamento, UUID> {

    @Query("select coalesce(sum(p.totalSucesso), 0) from LoteProcessamento p where p.lote.id = :loteId")
    int sumSucesso(UUID loteId);

    @Query("select coalesce(sum(p.totalErros), 0) from LoteProcessamento p where p.lote.id = :loteId")
    int sumErros(UUID loteId);

}
