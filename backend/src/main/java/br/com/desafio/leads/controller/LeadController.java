package br.com.desafio.leads.controller;

import br.com.desafio.leads.dto.LeadResponse;
import br.com.desafio.leads.service.LeadQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadQueryService leadQueryService;

    @GetMapping
    @Operation(summary = "Lista leads importados com filtros e paginação")
    public Page<LeadResponse> search(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String origem,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable
    ) {
        return leadQueryService.search(nome, email, origem, pageable);
    }

}
