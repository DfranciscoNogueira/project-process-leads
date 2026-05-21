package br.com.desafio.leads.repository;

import br.com.desafio.leads.model.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID>, JpaSpecificationExecutor<Lead> {

    boolean existsByEmailAndOrigem(String email, String origem);

    @Query("select count(l) from Lead l")
    long totalLeads();

    Page<Lead> findAll(Pageable pageable);

}
