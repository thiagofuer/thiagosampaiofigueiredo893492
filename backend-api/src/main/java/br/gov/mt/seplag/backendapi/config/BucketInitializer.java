package br.gov.mt.seplag.backendapi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class BucketInitializer {

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            log.info("Bucket '{}' já existe.", bucketName);

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.info("Bucket '{}' não encontrado. Criando...", bucketName);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                log.info("Bucket '{}' criado com sucesso!", bucketName);
            } else {
                log.error("Erro ao verificar/criar bucket: {}", e.getMessage());
            }
        }
    }
}
