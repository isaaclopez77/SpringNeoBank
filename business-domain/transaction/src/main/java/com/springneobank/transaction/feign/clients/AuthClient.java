package com.springneobank.transaction.feign.clients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import com.springneobank.transaction.feign.FeignAuthInterceptorConfig;
import com.springneobank.transaction.feign.FeignErrorConfig;

@FeignClient(
    name = "businessdomain-auth",
    configuration = {FeignAuthInterceptorConfig.class, FeignErrorConfig.class}
)
public interface AuthClient {

    @GetMapping("/user/get_kc_data")
    Map<String, Object> getKCData();
}
