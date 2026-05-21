package br.com.desafio.leads.controller;

import br.com.desafio.leads.dto.DashboardResponse;
import br.com.desafio.leads.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Indicadores simples: total de leads, lotes processados e taxa de erro")
    public DashboardResponse get() {
        return dashboardService.get();
    }

}
