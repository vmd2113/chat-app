package com.duongw.chatapp.service;

import java.util.Map;

public interface IOAuth2Service {
    Map<String, Object> getUserAttributes(String provider, String code);
}