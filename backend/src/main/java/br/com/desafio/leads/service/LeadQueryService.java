package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.LeadResponse;
import br.com.desafio.leads.mapper.LeadMapper;
import br.com.desafio.leads.model.Lead;
import br.com.desafio.leads.repository.LeadRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LeadQueryService {

    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public Page<LeadResponse> search(String nome, String email, String origem, Pageable pageable) {
        return leadRepository.findAll(spec(nome, email, origem), pageable).map(LeadMapper::toResponse);
    }

    private Specification<Lead> spec(String nome, String email, String origem) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (hasText(nome)) predicates.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            if (hasText(email)) predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            if (hasText(origem)) predicates.add(cb.equal(cb.lower(root.get("origem")), origem.toLowerCase()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
