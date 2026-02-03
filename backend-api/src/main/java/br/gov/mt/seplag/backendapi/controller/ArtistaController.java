package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.dto.ArtistaDTO;
import br.gov.mt.seplag.backendapi.service.ArtistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/artistas")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "Gerenciamento de artistas (cantores e bandas)")
public class ArtistaController {

    private final ArtistaService service;

    @Operation(summary = "Criar artista", description = "Cadastra um novo artista.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artista criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PostMapping
    public ResponseEntity<ArtistaDTO> criar(@Valid @RequestBody ArtistaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }

    @Operation(summary = "Buscar artista por ID", description = "Retorna os detalhes de um artista específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista encontrado"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArtistaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Atualizar artista", description = "Atualiza os dados de um artista existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artista atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ArtistaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ArtistaDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @Operation(summary = "Excluir artista", description = "Remove um artista do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artista excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @Operation(summary = "Listar artistas", description = "Retorna uma lista de artistas com filtros opcionais por nome e ordenação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de artistas recuperada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<ArtistaDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "asc") String ordem) {
        return ResponseEntity.ok(service.listarComFiltro(nome, ordem));
    }
}