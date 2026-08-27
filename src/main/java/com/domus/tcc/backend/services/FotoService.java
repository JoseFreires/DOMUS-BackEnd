package com.domus.tcc.backend.services;


import java.util.UUID;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FotoService {

    private final MinioClient minioClient;

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    @Value("${minio.bucket-name:encomendas}")
    private String bucketName;

    public FotoService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void inicializarBucket() {
        try {
            boolean bucketExiste = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExiste) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }

            // Aplica a política de leitura pública (Read-Only)
            String policy = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );

            System.out.println(">>> MinIO: Bucket '" + bucketName + "' configurado com acesso PUBLICO com sucesso!");
        } catch (Exception e) {
            System.err.println(">>> MinIO: Erro ao configurar politica publica: " + e.getMessage());
        }
    }




    public String upload(MultipartFile arquivo) throws Exception {

        // Gera nome para o arquivo para enviar pro banco
        String nomeArquivo = UUID.randomUUID() + "-" + arquivo.getOriginalFilename();

        // 3. Aqui envia para o miniO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(nomeArquivo)
                        .stream(
                                arquivo.getInputStream(),
                                arquivo.getSize(),
                                -1
                        )
                        .contentType(arquivo.getContentType())
                        .build()
        );

        // Monta a URL
        return String.format("%s/%s/%s", minioUrl, bucketName, nomeArquivo);
    }
}