package br.gov.mt.seplag.backendapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BucketInitializer {

    private final S3Client s3Client;
    private final ResourceLoader resourceLoader; // Para ler arquivos do projeto

    @Value("${s3.bucket-name}")
    private String bucketName;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        sincronizarExistenciaBucket();
        carregarImagensIniciais();
    }

    private void sincronizarExistenciaBucket() {
        try {
            //faz a verificação de existência/acessibilidade de um bucket no S3
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    log.info("Bucket '{}' criado.", bucketName);
                } catch (S3Exception ce) {
                    log.error("Falha ao criar o bucket '{}'.", bucketName, ce);
                }
            } else {
                log.error("Erro ao verificar existência do bucket '{}'.", bucketName, e);
            }
        }
    }

    private void carregarImagensIniciais() {
        String[] imagens = {
                "Bem_Sertanejo.jpg", "Bem_Sertanejo_1_Temporada.jpeg", "Bem_Sertanejo_O Show.jpeg",
                "Black_Blooms.jpg", "Greatest_Hits.jpg", "Harakiri.jpg", "Post_Traumatic.jpg",
                "Post_Traumatic_EP.jpg", "The_Rising_Tied.jpg", "The_Rough_Dog.png",
                "Use_Your_Illusion_1.jpg", "Use_Your_Illusion_2.jpg", "Where_dYou_Go.jpg"
        };

        for (String nomeImagem : imagens) {
            try {
                //Verifica se a imagem já existe no S3 (evita uploads duplicados)
                s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(nomeImagem).build());
            } catch (S3Exception e) {
                //Se não existir, busca no classpath e faz o upload
                try {
                    uploadResource(nomeImagem);
                } catch (Exception ex) {
                    System.err.println("Falha ao fazer upload da imagem inicial '" + nomeImagem + "': " + ex.getMessage());
                }
            } catch (Exception e) {
                //Outros erros (ex.: rede, timeout) são registrados, mas não impedem a inicialização das demais imagens
                System.err.println("Falha ao verificar imagem no S3: " + nomeImagem + " - " + e.getMessage());
            }
        }
    }

    private void uploadResource(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:data/images/" + fileName);
            if (resource.exists()) {
                s3Client.putObject(
                        PutObjectRequest.builder().bucket(bucketName).key(fileName).contentType("image/jpeg").build(),
                        RequestBody.fromInputStream(resource.getInputStream(), resource.contentLength())
                );
                log.info("Carga inicial: Upload da imagem '{}' concluído.", fileName);
            }
        } catch (IOException e) {
            log.error("Falha ao carregar imagem inicial {}: {}", fileName, e.getMessage());
        }
    }
}
