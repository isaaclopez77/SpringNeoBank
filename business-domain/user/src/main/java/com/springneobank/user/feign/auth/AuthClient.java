package com.springneobank.user.feign.auth;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import com.springneobank.user.feign.FeignAuthInterceptorConfig;
import com.springneobank.user.feign.FeignErrorConfig;

@FeignClient(
    name = "businessdomain-auth",
    configuration = {FeignAuthInterceptorConfig.class, FeignErrorConfig.class}
)
public interface AuthClient {

    @GetMapping("/user/get_id_by_authorization")
    ResponseEntity<String> getIDByAuthorization();

    @GetMapping("/user/get_kc_data")
    Map<String, Object> getKCData();

    // @TODO update entity
}
