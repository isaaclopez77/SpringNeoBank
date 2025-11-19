package com.springneobank.user.feign;

import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorConfig implements ErrorDecoder{
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        return switch (status) {
            case 404 -> new RuntimeException("Not found");
            case 401 -> new RuntimeException("Unauthorized");
            case 500 -> new RuntimeException("Server error");
            default -> new RuntimeException("Feign error: " + status);
        };
    }
}
