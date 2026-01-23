package br.gov.mt.seplag.backendapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
@RequiredArgsConstructor
public class MinioHealthIndicator implements HealthIndicator {

    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Override
    public Health health() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return Health.up()
                    .withDetail("bucket", bucketName)
                    .withDetail("status", "Conectado ao MinIO")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("erro", e.getMessage())
                    .build();
        }
    }
}
