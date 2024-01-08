package com.organica.services;

import java.util.Map;

public interface SunskyApiService {
    String call(String apiUrl, String secret, Map<String, String> parameters) throws Exception;
    void download(String apiUrl, String secret, Map<String, String> parameters) throws Exception;
    void exampleFetch() throws Exception;

}
