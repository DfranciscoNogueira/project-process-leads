package br.com.desafio.leads.repository;

import br.com.desafio.leads.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, UUID> {
}
