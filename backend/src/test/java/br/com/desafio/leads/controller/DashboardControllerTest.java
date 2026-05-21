package br.com.desafio.leads.controller;

import br.com.desafio.leads.dto.DashboardResponse;
import br.com.desafio.leads.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("Deve retornar indicadores do dashboard")
    void deveRetornarDashboard() throws Exception {
        DashboardResponse response = new DashboardResponse(
                120L,
                10L,
                12.5
        );

        when(dashboardService.get())
                .thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalLeads").value(120))
                .andExpect(jsonPath("$.lotesProcessados").value(10));

        verify(dashboardService).get();
    }

    @Test
    @DisplayName("Deve responder GET /api/dashboard")
    void deveResponderGetDashboard() throws Exception {
        when(dashboardService.get())
                .thenReturn(new DashboardResponse(
                        0L,
                        0L,
                        0.0
                ));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk());

        verify(dashboardService).get();
    }
}