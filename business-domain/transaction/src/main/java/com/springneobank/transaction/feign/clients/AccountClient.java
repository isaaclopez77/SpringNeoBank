package com.springneobank.transaction.feign.clients;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springneobank.transaction.feign.FeignAuthInterceptorConfig;
import com.springneobank.transaction.feign.FeignErrorConfig;

@FeignClient(
    name = "businessdomain-account",
    configuration = {FeignAuthInterceptorConfig.class, FeignErrorConfig.class}
)
public interface AccountClient {

    @GetMapping("/account/get_account/{account_id}")
    Map<String, Object> getAccountById(@PathVariable("account_id") Long accountId);

    @PostMapping("/account/credit/{account_id}")
    Map<String, Object> credit(@PathVariable("account_id") Long accountId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/account/debit/{account_id}")
    Map<String, Object> debit(@PathVariable("account_id") Long accountId, @RequestParam("amount") BigDecimal amount);
    
}
