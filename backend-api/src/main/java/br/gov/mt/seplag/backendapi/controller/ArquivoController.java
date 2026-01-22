package br.gov.mt.seplag.backendapi.controller;

import br.gov.mt.seplag.backendapi.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class ArquivoController {

    private final S3Service s3Service;

    @PostMapping("/upload")
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