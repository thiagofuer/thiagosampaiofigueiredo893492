package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.dto.ArtistaDTO;
import br.gov.mt.seplag.backendapi.service.ArtistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/artistas")
@RequiredArgsConstructor
public class ArtistaController {

    private final ArtistaService service;

    @PostMapping
    public ResponseEntity<ArtistaDTO> criar(@Valid @RequestBody ArtistaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtistaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ArtistaDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping
    public ResponseEntity<List<ArtistaDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "asc") String ordem) {
        return ResponseEntity.ok(service.listarComFiltro(nome, ordem));
    }
}
