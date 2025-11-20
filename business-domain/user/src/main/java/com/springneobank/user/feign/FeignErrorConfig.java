package com.springneobank.user.feign;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorConfig implements ErrorDecoder{

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;

        try {
            if (response.body() != null) {
                body = convertStreamToString(response.body().asInputStream());
            }
        } catch (IOException e) {
            body = "Error reading body: " + e.getMessage();
        }

        String message = "Feign error: status=" + response.status()
                + ", method=" + methodKey
                + ", body=" + body;

        return new RuntimeException(message);
    }

    private String convertStreamToString(InputStream is) throws IOException {
        if (is == null) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
