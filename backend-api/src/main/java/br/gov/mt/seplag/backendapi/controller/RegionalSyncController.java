package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/regionais")
@RequiredArgsConstructor
@Tag(name = "Sincronização", description = "Operações administrativas de sincronização")
public class RegionalSyncController {

    private final RegionalSyncService regionalSyncService;

    @PostMapping("/sincronizar")
    @Operation(summary = "Sincronizar regionais", description = "Dispara manualmente a sincronização de regionais a partir da API externa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronização efetuada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno durante a sincronização")
    })
    public ResponseEntity<String> dispararSincronizacao() {
        regionalSyncService.sincronizar();
        return ResponseEntity.ok("Sincronização efetuada com sucesso.");
    }
}