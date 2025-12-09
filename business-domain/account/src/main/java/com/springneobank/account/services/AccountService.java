package com.springneobank.account.services;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springneobank.account.common.OperationResult;
import com.springneobank.account.entities.Account;
import com.springneobank.account.entities.AccountType;
import com.springneobank.account.feign.auth.AuthClient;
import com.springneobank.account.repositories.AccountRepository;
import com.springneobank.account.repositories.AccountTypeRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountTypeRepository typeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthClient authClient;

    /**
     * Create new Account based on client ID
     * 
     * @param userID
     * @return
     */
    public OperationResult<String> createAccount(Long typeID) {
        try{
            // Get user ID making a request to Auth microservice
            Long userID = getUserIDRequest();

            // Generate IBAN
            String iban = generateIban();

            AccountType type = typeRepository.findById(typeID).orElseThrow(() -> new RuntimeException("Account type " + typeID + " not available"));

            Account acc = new Account(userID, type, iban);
            accountRepository.save(acc);

            return OperationResult.ok("Account created");
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Get accounts of logged user
     * 
     * @return
     */
    public OperationResult<List<Account>> getLoggedUserAccounts() {
        try{
            // Get user ID making a request to Auth microservice
            Long userID = getUserIDRequest();

            List<Account> accounts = accountRepository.findByUserIdAndStatusTrue(userID);

            return OperationResult.ok(accounts);
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Get accounts by user id
     * 
     * @param userID
     * @return
     */
    public OperationResult<List<Account>> getAccountsByUser(Long userID) {
        try{
            List<Account> accounts = accountRepository.findByUserIdAndStatusTrue(userID);

            return OperationResult.ok(accounts);
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    /**
     * Get account by id
     * 
     * @param accountId
     * @return
     */
    public OperationResult<Account> getAccount(Long accountId) {
        try{
            Optional<Account> optAccount = accountRepository.findById(accountId);

            Account account = optAccount.orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

            return OperationResult.ok(account);
        } catch(Exception e) {
            return OperationResult.fail(e.getMessage());
        }
    }


    /**
     * Change account status field
     * 
     * @param accountID
     * @param iban
     * @param status
     * @return
     */
    public OperationResult<String> changeAccountStatus(Long accountID, String iban, boolean status) {
        try{
            // Get user ID making a request to Auth microservice
            Long userID = getUserIDRequest();

            Account acc = accountRepository.findByUserIdAndIbanAndId(userID, iban, accountID).orElseThrow(() -> new RuntimeException("Account not found"));

            acc.setStatus(status);
            accountRepository.save(acc);

            return OperationResult.ok("Status changed");
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

    /**
     * Generate a IBAN code
     * 
     * @return
     */
    private String generateIban() {
        // Generate sequencial account number for IBAN
        Long maxAccountNumber = accountRepository.getMaxAccountNumber().orElse(0L);
        String accountNumber = String.format("%010d", Long.sum(maxAccountNumber ,1L));

        // Example values for IBAN
        String countryCode = "ES";
        String bankCode = "1234";
        String branchCode = "0000";
        String internalDC = "00";

        String bban = bankCode + branchCode + internalDC + accountNumber;

        // Convert country letters to numbers
        String countryConverted = convertLettersToDigits(countryCode); // ES → 1428

        // BBAN + país + 00
        String rearranged = bban + countryConverted + "00";

        BigInteger num = new BigInteger(rearranged);
        int mod97 = num.mod(BigInteger.valueOf(97)).intValue();

        int checkDigits = 98 - mod97;
        String check = String.format("%02d", checkDigits);

        return countryCode + check + bban;
    }

    private String convertLettersToDigits(String country) {
        StringBuilder sb = new StringBuilder();
        for (char c : country.toCharArray()) {
            int val = Character.getNumericValue(c); // A=10 ... Z=35
            sb.append(val);
        }
        return sb.toString();
    }

}
