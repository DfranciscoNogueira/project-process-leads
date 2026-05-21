package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.DashboardResponse;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.repository.LeadRepository;
import br.com.desafio.leads.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LeadRepository leadRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public DashboardResponse get() {
        var lotes = loteRepository.findAll();
        var processados = lotes.stream().filter(l -> l.getStatus() == LoteStatus.FINALIZADO || l.getStatus() == LoteStatus.FINALIZADO_COM_ERROS).count();
        var totalLinhas = lotes.stream().mapToInt(l -> l.getTotalSucesso() + l.getTotalErros()).sum();
        var totalErros = lotes.stream().mapToInt(Lote::getTotalErros).sum();
        var taxaErro = totalLinhas == 0 ? 0 : (totalErros * 100.0) / totalLinhas;
        return new DashboardResponse(leadRepository.totalLeads(), processados, taxaErro);
    }

}
