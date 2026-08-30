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

    @Value("${minio.bucket-name-encomenda:encomendas}")
    private String bucketName;

    @Value("${minio.bucket-name-pessoa:pessoas}")
    private String bucketNamePessoa;

    public FotoService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void inicializarBucket() {
        try {
            boolean bucketExisteEncomenda = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            boolean bucketExistePessoa = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketNamePessoa).build()
            );

            if (!bucketExisteEncomenda) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            if (!bucketExistePessoa) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketNamePessoa).build());
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
            """;

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy.formatted(bucketName))
                            .build()
            );

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketNamePessoa)
                            .config(policy.formatted(bucketNamePessoa))
                            .build()
            );

            System.out.println(">>> MinIO: Bucket '" + bucketName + "' configurado com acesso PUBLICO com sucesso!");
            System.out.println(">>> MinIO: Bucket '" + bucketNamePessoa + "' configurado com acesso PUBLICO com sucesso!");
        } catch (Exception e) {
            System.err.println(">>> MinIO: Erro ao configurar politica publica: " + e.getMessage());
        }
    }




    public String upload(MultipartFile arquivo) throws Exception {

        // Gera nome para o arquivo para enviar pro banco
        String nomeArquivo = UUID.randomUUID() + "-" + arquivo.getOriginalFilename();

        // Aqui envia para o miniO
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


    public String uploadFotoPessoa(MultipartFile arquivo) throws Exception {


        String nomeArquivo = UUID.randomUUID() + "-" + arquivo.getOriginalFilename();


        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketNamePessoa)
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
        return String.format("%s/%s/%s", minioUrl, bucketNamePessoa, nomeArquivo);
    }

    //Método para extrair o nome do arquivo para delete
    private String extrairNomeArquivo(String fotoUrl) {
        if (fotoUrl.contains("/")) {
            return fotoUrl.substring(fotoUrl.lastIndexOf("/") + 1);
        }
        return fotoUrl;
    }

    public void deletarFotoEncomenda(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return;
        }

        try {
            String nomeArquivo = extrairNomeArquivo(fotoUrl);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(nomeArquivo)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível remover o arquivo antigo do MinIO: " + e.getMessage());
        }
    }

    public void deletarFotoPessoa(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return;
        }

        try {
            String nomeArquivo = extrairNomeArquivo(fotoUrl);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketNamePessoa)
                            .object(nomeArquivo)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível remover o arquivo antigo do MinIO: " + e.getMessage());
        }
    }
}

