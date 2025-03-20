package com.duongw.chatapp.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt");
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Gets the file extension from a filename, including the dot (e.g., ".jpg").
     *
     * @param fileName The filename
     * @return The extension with dot, or empty string if no extension
     */
    public static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty() || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    /**
     * Checks if a file is an image based on its extension.
     *
     * @param fileName The filename to check
     * @return true if the file has an image extension
     */
    public static boolean isImage(String fileName) {
        String extension = getExtension(fileName).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * Checks if a file is a document based on its extension.
     *
     * @param fileName The filename to check
     * @return true if the file has a document extension
     */
    public static boolean isDocument(String fileName) {
        String extension = getExtension(fileName).toLowerCase();
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * Gets the base filename without extension.
     *
     * @param fileName The complete filename
     * @return The filename without extension
     */
    public static String getBaseFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return fileName;
        }

        return fileName.substring(0, lastDotIndex);
    }

    /**
     * Generates a safe filename by removing problematic characters.
     *
     * @param fileName The original filename
     * @return A sanitized version of the filename
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        // Remove any path info that might be present
        String name = new java.io.File(fileName).getName();

        // Replace potentially problematic characters with underscores
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    /**
     * Reads an InputStream into a byte array.
     *
     * @param input The InputStream to read from
     * @return The byte array containing all data from the stream
     * @throws IOException If an I/O error occurs
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n;

        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }

        return output.toByteArray();
    }

    /**
     * Extracts the content type from a file extension.
     *
     * @param extension The file extension
     * @return The corresponding content type or "application/octet-stream" if unknown
     */
    public static String getContentTypeFromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }

        String ext = extension.toLowerCase();
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }

        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }

    /**
     * Generates a unique filename by adding a timestamp to the base filename.
     *
     * @param fileName The original filename
     * @return A unique filename
     */
    public static String generateUniqueFileName(String fileName) {
        String baseName = getBaseFileName(fileName);
        String extension = getExtension(fileName);
        long timestamp = System.currentTimeMillis();

        return baseName + "_" + timestamp + extension;
    }

    /**
     * Determines if the given file is likely to be valid by checking it has content
     * and a valid filename.
     *
     * @param file The file to check
     * @return true if the file appears to be valid
     */
    public static boolean isValidFile(MultipartFile file) {
        return file != null &&
                !file.isEmpty() &&
                StringUtils.hasText(file.getOriginalFilename());
    }
}
