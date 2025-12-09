package com.springneobank.transaction.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.discovery.converters.Auto;
import com.springneobank.transaction.common.OperationResult;
import com.springneobank.transaction.entities.Transaction;
import com.springneobank.transaction.feign.clients.AccountClient;
import com.springneobank.transaction.feign.clients.AuthClient;
import com.springneobank.transaction.repositories.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private TransactionRepository tRepository;

    /**
     * Get transactions by account id
     * 
     * @param request
     * @param accountID
     * @return
     */
    public OperationResult<?> getTransactions(Long accountID) {

        try{
            // Get logged user ID
            Long userID = getUserIDRequest();

            // Get account
            Map<String, Object> accountResponse = accountClient.getAccountById(accountID);
            Map<String, Object> accountData = (Map<String, Object>) accountResponse.get("message");
            // Convert account user ID to long for comparing
            Number accountUserId = (Number)accountData.get("userId");
            Long accountUserIDLong = accountUserId.longValue();
            if(!(accountUserIDLong).equals(userID)) {
                return OperationResult.fail("Error. La cuenta no es de tu propiedad");
            }

            // Get transactions
            List<Transaction> transactions = tRepository.findBySourceAccountIdOrTargetAccountId(accountID, accountID);

            return OperationResult.ok(transactions);
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }


    /**
     * Make a request to Auth microservice to obtain user data and extract the user ID
     * 
     * @return
     */
    private Long getUserIDRequest() {
        Map<String, Object> getKCDataResponse = authClient.getKCData();


        return Long.parseLong(getKCDataResponse.get("user_id").toString());
    }
}
