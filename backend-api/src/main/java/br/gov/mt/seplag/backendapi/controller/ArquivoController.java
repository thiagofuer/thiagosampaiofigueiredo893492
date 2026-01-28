package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/v1/arquivos")
@RequiredArgsConstructor
@Tag(name = "Arquivos", description = "Gerenciamento de upload de arquivos")
public class ArquivoController {

    private final S3Service s3Service;

    @Operation(summary = "Upload de arquivo", description = "Realiza o upload de um arquivo (imagem) para o armazenamento S3 e retorna o nome do arquivo salvo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"fileName\": \"uuid-nomearquivo.jpg\"}"))),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar o upload")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = s3Service.uploadFile(file);
            return ResponseEntity.ok(Map.of("fileName", fileName));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao processar upload: " + e.getMessage()));
        }
    }
}