package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.service.RegionalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/regionais")
@RequiredArgsConstructor
public class RegionalSyncController {

    private final RegionalSyncService regionalSyncService;

    @PostMapping("/sincronizar")
    @Operation(summary = "Dispara a sincronização manual das regionais")
    public ResponseEntity<String> dispararSincronizacao() {
        regionalSyncService.sincronizar();
        return ResponseEntity.ok("Sincronização efetuada com sucesso.");
    }
}