package br.com.desafio.leads.controller;

import br.com.desafio.leads.dto.LeadResponse;
import br.com.desafio.leads.service.LeadQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeadController.class)
class LeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeadQueryService leadQueryService;

    @Test
    @DisplayName("Deve listar leads com paginação")
    void deveListarLeadsComPaginacao() throws Exception {
        LeadResponse lead = criarLeadResponse();

        Page<LeadResponse> page = new PageImpl<>(
                List.of(lead),
                PageRequest.of(0, 20, Sort.by("criadoEm")),
                1
        );

        when(leadQueryService.search(
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/leads"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].nome").value("Diego"))
                .andExpect(jsonPath("$.content[0].email").value("diego@email.com"))
                .andExpect(jsonPath("$.content[0].origem").value("site"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(leadQueryService)
                .search(
                        isNull(),
                        isNull(),
                        isNull(),
                        any(Pageable.class)
                );
    }

    @Test
    @DisplayName("Deve listar leads usando filtros")
    void deveListarLeadsUsandoFiltros() throws Exception {
        LeadResponse lead = criarLeadResponse();

        Page<LeadResponse> page = new PageImpl<>(
                List.of(lead),
                PageRequest.of(0, 20),
                1
        );

        when(leadQueryService.search(
                eq("Diego"),
                eq("diego@email.com"),
                eq("site"),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/leads")
                                .param("nome", "Diego")
                                .param("email", "diego@email.com")
                                .param("origem", "site")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Diego"))
                .andExpect(jsonPath("$.content[0].email").value("diego@email.com"))
                .andExpect(jsonPath("$.content[0].origem").value("site"));

        verify(leadQueryService)
                .search(
                        eq("Diego"),
                        eq("diego@email.com"),
                        eq("site"),
                        any(Pageable.class)
                );
    }

    @Test
    @DisplayName("Deve respeitar parâmetros de paginação")
    void deveRespeitarParametrosDePaginacao() throws Exception {
        Page<LeadResponse> page = new PageImpl<>(
                List.of(criarLeadResponse()),
                PageRequest.of(1, 5, Sort.by("nome")),
                10
        );

        when(leadQueryService.search(
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/leads")
                                .param("page", "1")
                                .param("size", "5")
                                .param("sort", "nome")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(1));

        verify(leadQueryService)
                .search(
                        isNull(),
                        isNull(),
                        isNull(),
                        argThat(pageable ->
                                pageable.getPageNumber() == 1
                                        && pageable.getPageSize() == 5
                                        && pageable.getSort().getOrderFor("nome") != null
                        )
                );
    }

    private LeadResponse criarLeadResponse() {
        return new LeadResponse(
                java.util.UUID.randomUUID(),
                "Diego",
                "diego@email.com",
                "11999999999",
                "site",
                LocalDate.of(2026, 5, 21),
                java.util.UUID.randomUUID()
        );
    }

}