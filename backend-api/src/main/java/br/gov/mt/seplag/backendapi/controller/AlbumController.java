package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.dto.AlbumDTO;
import br.gov.mt.seplag.backendapi.model.TipoArtista;
import br.gov.mt.seplag.backendapi.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/albuns")
@RequiredArgsConstructor
@Tag(name = "Álbuns", description = "Gerenciamento de álbuns musicais")
public class AlbumController {

    private final AlbumService service;

    @Operation(summary = "Listar álbuns", description = "Retorna uma lista paginada de álbuns, com filtros opcionais por tipo de artista e termo de pesquisa (título ou artista).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de álbuns recuperada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<Page<AlbumDTO>> listar(
            @RequestParam(required = false) TipoArtista tipo,
            @RequestParam(required = false) String termo,
            @ParameterObject @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(tipo, termo, pageable));
    }

    @Operation(summary = "Criar álbum", description = "Cria um novo álbum.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Álbum criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<AlbumDTO> criar(@Valid @RequestBody AlbumDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }
}