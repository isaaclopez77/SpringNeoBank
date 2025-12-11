package com.springneobank.transaction.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springneobank.transaction.common.OperationResult;
import com.springneobank.transaction.entities.Transaction;
import com.springneobank.transaction.entities.TransactionStatus;
import com.springneobank.transaction.entities.TransactionType;
import com.springneobank.transaction.feign.clients.AccountClient;
import com.springneobank.transaction.feign.clients.AuthClient;
import com.springneobank.transaction.repositories.TransactionRepository;
import com.springneobank.transaction.repositories.TransactionStatusRepository;
import com.springneobank.transaction.repositories.TransactionTypeRepository;

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

    @Autowired
    private TransactionTypeRepository typeRepository;

    @Autowired
    private TransactionStatusRepository statusRepository;

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
     * Make a withdraw. Inserts the transaction and modify the balance of the account
     * 
     * @param sourceId
     * @param amount
     * @param decription
     * @return
     */
    public OperationResult<String> withdraw(Long sourceId, BigDecimal amount, String decription) {
        
        try{
            TransactionStatus status;

            try{
                // Check if account owner is logged user
                Long userID = getUserIDRequest();

                // Get account
                Map<String, Object> accountResponse = accountClient.getAccountById(sourceId);
                Map<String, Object> accountData = (Map<String, Object>) accountResponse.get("message");
                // Convert account user ID to long for comparing
                Number accountUserId = (Number)accountData.get("userId");
                Long accountUserIDLong = accountUserId.longValue();
                if(!(accountUserIDLong).equals(userID)) {
                    return OperationResult.fail("Error. La cuenta no es de tu propiedad");
                }

                // Do request
                accountClient.debit(sourceId, amount);

                status = statusRepository.getReferenceById(1L); // Successful status if no error - by reference
            } catch(RuntimeException e) {
                status = statusRepository.getReferenceById(0L); // Error status - by reference
            }

            TransactionType withdrawRef = typeRepository.getReferenceById(2L); // Withdraw Type by reference
            // Create and save transaction
            Transaction tr = new Transaction(sourceId, null, amount, withdrawRef, status, decription);
            tRepository.save(tr);

            return OperationResult.ok("Retirada efectuada");
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Make a deposit. Inserts the transaction and modify the balance of the account
     * 
     * @param targetId
     * @param amount
     * @param decription
     * @return
     */
    public OperationResult<String> deposit(Long targetId, BigDecimal amount, String decription) {
        
        try{
            TransactionStatus status;
            String response;

            try{
                // Check if account owner is logged user
                Long userID = getUserIDRequest();

                // Get account
                Map<String, Object> accountResponse = accountClient.getAccountById(targetId);
                Map<String, Object> accountData = (Map<String, Object>) accountResponse.get("message");
                // Convert account user ID to long for comparing
                Number accountUserId = (Number)accountData.get("userId");
                Long accountUserIDLong = accountUserId.longValue();
                if(!(accountUserIDLong).equals(userID)) {
                    return OperationResult.fail("Error. La cuenta no es de tu propiedad");
                }

                // Do request
                accountClient.credit(targetId, amount);

                status = statusRepository.getReferenceById(1L); // Successful status if no error - by reference
                response = "Ingreso efectuado";
            } catch(RuntimeException e) {
                status = statusRepository.getReferenceById(0L); // Error status - by reference
                response = "Ingreso fallido";
                log.info(e.getMessage());
            }

            TransactionType depositRef = typeRepository.getReferenceById(3L); // Deposit Type by reference
            // Create and save transaction
            Transaction tr = new Transaction(null, targetId, amount, depositRef, status, decription);
            tRepository.save(tr);

            return OperationResult.ok(response);
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Make a transference between 2 accounts. Inserts the transaction and modify the balance of both accounts
     * 
     * @param sourceId
     * @param targetId
     * @param amount
     * @param decription
     * @return
     */
    public OperationResult<String> transfer(Long sourceId, Long targetId, BigDecimal amount, String decription) {
        
        try{
            TransactionStatus status;
            String response;
            boolean debitDone = false;
            boolean creditDone = false;

            try{
                // Do credit and debit request
                accountClient.debit(sourceId, amount);
                debitDone = true;

                accountClient.credit(targetId, amount);
                creditDone = true;

                status = statusRepository.getReferenceById(1L); // Successful status if no error - by reference
                response = "Transferencia efectuada";
            } catch(RuntimeException e) {
                // Compensation on error
                if (debitDone && !creditDone) {
                    accountClient.credit(sourceId, amount);
                }

                status = statusRepository.getReferenceById(0L); // Error status - by reference
                response = "Transferencia fallida";
                log.info(e.getMessage());
            }

            TransactionType depositRef = typeRepository.getReferenceById(1L); // Transference Type by reference
            // Create and save transaction
            Transaction tr = new Transaction(sourceId, targetId, amount, depositRef, status, decription);
            tRepository.save(tr);

            return OperationResult.ok(response);
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
