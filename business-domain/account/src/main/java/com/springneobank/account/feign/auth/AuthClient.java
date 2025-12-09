package com.springneobank.account.feign.auth;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.springneobank.account.feign.FeignAuthInterceptorConfig;
import com.springneobank.account.feign.FeignErrorConfig;

@FeignClient(
    name = "businessdomain-auth",
    configuration = {FeignAuthInterceptorConfig.class, FeignErrorConfig.class}
)
public interface AuthClient {

    @GetMapping("/user/get_kc_data")
    Map<String, Object> getKCData();
}
