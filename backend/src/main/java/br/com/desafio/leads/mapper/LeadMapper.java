package br.com.desafio.leads.mapper;

import br.com.desafio.leads.dto.LeadResponse;
import br.com.desafio.leads.model.Lead;

public final class LeadMapper {

    private LeadMapper() {
    }

    public static LeadResponse toResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getNome(),
                lead.getEmail(),
                lead.getTelefone(),
                lead.getOrigem(),
                lead.getDataCadastro(),
                lead.getLote().getId()
        );
    }

}
