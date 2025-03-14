package com.duongw.chatapp.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class MinioService {

    private final MinioClient minioClient;

    private String bucketName;


    @Autowired
    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    private String buildPath(MultipartFile file) {
        LocalDateTime dateTime = LocalDateTime.now();
        String datePath = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timePath = dateTime.format(DateTimeFormatter.ofPattern("hh-mm-ss"));
        String uniqueId = UUID.randomUUID().toString().substring(1, 8);
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        return String.join("/",
                datePath,
                datePath + "_" + timePath + "_" + uniqueId + "." + extension);


    }


    /**
     * Upload file to MinIO
     *
     * @param file        File to upload
     * @param contentType MIME type of the file
     * @return Name of the uploaded file
     */
    public String uploadFile(MultipartFile file, String contentType) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String filename = buildPath(file);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(contentType)
                        .build()
        );

        return filename;

    }

    /**
     * Generate a presigned URL for downloading a file
     *
     * @param fileName Name of the file
     * @return Presigned URL for downloading
     */
    public String getFileUrl(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .method(Method.GET)
                        .expiry(60, TimeUnit.MINUTES)

                        .build()
        );


    }


    /**
     * Download a file from MinIO
     *
     * @param fileName Name of the file
     * @return InputStream of the file
     */
    public InputStream downloadFile(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }


    /**
     * Delete a file from MinIO
     *
     * @param fileName Name of the file
     */
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build());
    }


}
