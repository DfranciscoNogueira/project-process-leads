package br.com.desafio.leads.service;

import br.com.desafio.leads.dto.DashboardResponse;
import br.com.desafio.leads.model.Lote;
import br.com.desafio.leads.model.LoteStatus;
import br.com.desafio.leads.repository.LeadRepository;
import br.com.desafio.leads.repository.LoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    @DisplayName("Deve retornar dashboard com taxa de erro calculada")
    void deveRetornarDashboardComTaxaErroCalculada() {
        when(leadRepository.totalLeads())
                .thenReturn(120L);

        when(loteRepository.findAll())
                .thenReturn(List.of(
                        criarLote(
                                LoteStatus.FINALIZADO,
                                90,
                                10
                        ),
                        criarLote(
                                LoteStatus.FINALIZADO_COM_ERROS,
                                45,
                                5
                        ),
                        criarLote(
                                LoteStatus.PROCESSANDO,
                                0,
                                0
                        )
                ));

        DashboardResponse response = service.get();

        assertEquals(120L, response.totalLeads());
        assertEquals(2L, response.lotesProcessados());

        verify(leadRepository).totalLeads();
        verify(loteRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar taxa de erro zero quando não houver linhas")
    void deveRetornarTaxaErroZeroQuandoNaoHouverLinhas() {
        when(leadRepository.totalLeads())
                .thenReturn(0L);

        when(loteRepository.findAll())
                .thenReturn(List.of(
                        criarLote(
                                LoteStatus.FINALIZADO,
                                0,
                                0
                        )
                ));

        DashboardResponse response = service.get();

        assertEquals(0L, response.totalLeads());
        assertEquals(1L, response.lotesProcessados());
    }

    @Test
    @DisplayName("Deve contar apenas lotes finalizados")
    void deveContarApenasLotesFinalizados() {
        when(leadRepository.totalLeads())
                .thenReturn(10L);

        when(loteRepository.findAll())
                .thenReturn(List.of(
                        criarLote(
                                LoteStatus.RECEBIDO,
                                0,
                                0
                        ),
                        criarLote(
                                LoteStatus.PROCESSANDO,
                                0,
                                0
                        ),
                        criarLote(
                                LoteStatus.FINALIZADO,
                                5,
                                0
                        ),
                        criarLote(
                                LoteStatus.FINALIZADO_COM_ERROS,
                                4,
                                1
                        ),
                        criarLote(
                                LoteStatus.FALHOU,
                                0,
                                10
                        )
                ));

        DashboardResponse response = service.get();

        assertEquals(2L, response.lotesProcessados());
    }

    @Test
    @DisplayName("Deve retornar dashboard vazio")
    void deveRetornarDashboardVazio() {
        when(leadRepository.totalLeads())
                .thenReturn(0L);

        when(loteRepository.findAll())
                .thenReturn(List.of());

        DashboardResponse response = service.get();

        assertEquals(0L, response.totalLeads());
        assertEquals(0L, response.lotesProcessados());
    }

    private Lote criarLote(
            LoteStatus status,
            int sucesso,
            int erros
    ) {
        return Lote.builder()
                .status(status)
                .totalSucesso(sucesso)
                .totalErros(erros)
                .build();
    }
}