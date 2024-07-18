package com.fmning.tools.controller;

import com.fmning.tools.ToolsExceptionHandler;
import com.fmning.tools.domain.SpendingAccount;
import com.fmning.tools.domain.SpendingTransaction;
import com.fmning.tools.repository.SpendingAccountRepo;
import com.fmning.tools.repository.SpendingTransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@CommonsLog
@RestController
@RequestMapping(value = "/api/spending")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SpendingController {

    private final SpendingAccountRepo accountRepo;
    private final SpendingTransactionRepo transactionRepo;

    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public List<SpendingAccount> getAccounts() {
        return StreamSupport
                .stream(accountRepo.findAll().spliterator(), false)
                .toList();
    }

    @RequestMapping(value = "/accounts", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public SpendingAccount createAccount(@RequestBody SpendingAccount account) {
        account.setId(0);
        return accountRepo.save(account);
    }

    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public SpendingAccount updateAccount(@PathVariable int id, @RequestBody SpendingAccount account) {
        SpendingAccount spendingAccount = accountRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        spendingAccount.setName(account.getName());
        spendingAccount.setIdentifier(account.getIdentifier());
        spendingAccount.setIcon(account.getIcon());
        spendingAccount.setOwner(account.getOwner());
        return accountRepo.save(spendingAccount);
    }

    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public void deleteAccount(@PathVariable int id) {
        accountRepo.deleteById(id);
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public List<SpendingTransaction> getTransactions(@RequestParam(required=false) Date from) {
        if (from == null) {
            return transactionRepo.findAll();
        } else {
            return transactionRepo.findAllByDateAfter(from);
        }
    }

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public void uploadTransactions(@RequestBody List<SpendingTransaction> transactions) {
        try {
            transactionRepo.saveAll(transactions);
        } catch (DataIntegrityViolationException e) {
            if (!e.getMessage().contains("tl_spending_transactions_identifier_key")) throw e;

            Pattern p = Pattern.compile("\\(identifier\\)=\\((.*?)\\)");
            Matcher m = p.matcher(e.getMessage());
            if (m.find()) {
                throw new IllegalArgumentException(m.group(1));
            }
            throw e;
        }
    }

}
