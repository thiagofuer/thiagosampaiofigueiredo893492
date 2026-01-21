package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.dto.AlbumDTO;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/albuns")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService service;

    @GetMapping
    public ResponseEntity<Page<AlbumDTO>> listar(
            @RequestParam(required = false) TipoArtista tipo,
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(tipo, busca, pageable));
    }

    @PostMapping
    public ResponseEntity<AlbumDTO> criar(@Valid @RequestBody AlbumDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }
}