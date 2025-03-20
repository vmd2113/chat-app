package com.duongw.chatapp.utils;


import com.duongw.chatapp.config.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtil {

    public static boolean isNullOrEmpty(String str) {
        return !StringUtils.hasText(str);
    }

    public static boolean isNotNullOrEmpty(String str) {
        return StringUtils.hasText(str);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }

    public static String toSlug(String input) {
        if (input == null) {
            return null;
        }

        // Convert to lowercase
        String result = input.toLowerCase();

        // Replace spaces with hyphens
        result = result.replaceAll("\\s+", "-");

        // Remove special characters
        result = result.replaceAll("[^a-z0-9-]", "");

        // Remove consecutive hyphens
        result = result.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        result = result.replaceAll("^-|-$", "");

        return result;
    }

    public static String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AppConstant.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}