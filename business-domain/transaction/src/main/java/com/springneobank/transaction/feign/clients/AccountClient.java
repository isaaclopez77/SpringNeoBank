package com.springneobank.transaction.feign.clients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.springneobank.transaction.feign.FeignAuthInterceptorConfig;
import com.springneobank.transaction.feign.FeignErrorConfig;

@FeignClient(
    name = "businessdomain-account",
    configuration = {FeignAuthInterceptorConfig.class, FeignErrorConfig.class}
)
public interface AccountClient {

    @GetMapping("/account/get_account/{account_id}")
    Map<String, Object> getAccountById(@PathVariable("account_id") Long accountId);
    
}
