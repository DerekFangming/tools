package com.fmning.tools.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmning.tools.domain.RealEstate;
import com.fmning.tools.domain.SpendingAccount;
import com.fmning.tools.domain.SpendingTransaction;
import com.fmning.tools.dto.RealEstateDto;
import com.fmning.tools.repository.ConfigRepo;
import com.fmning.tools.repository.RealEstateRepo;
import com.fmning.tools.repository.SpendingAccountRepo;
import com.fmning.tools.repository.SpendingTransactionRepo;
import com.fmning.tools.service.RealEstateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommonsLog
@RestController
@RequestMapping(value = "/api/finance")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class FinanceController {

    private final ConfigRepo configRepo;
    private final ObjectMapper objectMapper;
    private final RealEstateService realEstateService;
    private final RealEstateRepo realEstateRepo;
    private final SpendingAccountRepo accountRepo;
    private final SpendingTransactionRepo transactionRepo;

    @RequestMapping(value = "/spending/accounts", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public List<SpendingAccount> getAccounts() {
        return accountRepo.findAll();
    }

    @RequestMapping(value = "/spending/accounts", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public SpendingAccount createAccount(@RequestBody SpendingAccount account) {
        account.setId(0);
        return accountRepo.save(account);
    }

    @RequestMapping(value = "/spending/accounts/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public SpendingAccount updateAccount(@PathVariable int id, @RequestBody SpendingAccount account) {
        SpendingAccount spendingAccount = accountRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        spendingAccount.setName(account.getName());
        spendingAccount.setIdentifier(account.getIdentifier());
        spendingAccount.setIcon(account.getIcon());
        spendingAccount.setOwner(account.getOwner());
        return accountRepo.save(spendingAccount);
    }

    @RequestMapping(value = "/spending/accounts/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public void deleteAccount(@PathVariable int id) {
        accountRepo.deleteById(id);
    }

    @RequestMapping(value = "/spending/transactions", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public List<SpendingTransaction> getTransactions(@RequestParam(required=false) Date from,
                                                     @RequestParam(required=false) Date to) {
        if (from == null) {
            return transactionRepo.findAll();
        }
        from.setTime(from.getTime() - 24 * 60 * 60 * 1000);
        if (to == null) {
            return transactionRepo.findAllByDateAfter(from);
        } else {
            return transactionRepo.findAllByDateAfterAndDateBefore(from, to);
        }
    }

    @RequestMapping(value = "/spending/transactions", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
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

    @RequestMapping(value = "/spending/transactions/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public SpendingTransaction updateTransaction(@PathVariable int id, @RequestBody SpendingTransaction transaction) {
        SpendingTransaction spendingTransaction = transactionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        spendingTransaction.setName(transaction.getName());
        spendingTransaction.setOriginalName(transaction.getOriginalName());
        spendingTransaction.setCategory(transaction.getCategory());
        return transactionRepo.save(spendingTransaction);
    }

    @RequestMapping(value = "/spending/transactions/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public void deleteTransaction(@PathVariable int id) {
        transactionRepo.deleteById(id);
    }

    @RequestMapping(value = "/real-estates", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public List<RealEstateDto> listRealEstates() throws JsonProcessingException {

        String realEstate = configRepo.findById("REAL_ESTATE")
                .orElseThrow(() -> new IllegalStateException("Failed to get real estate")).getValue();

        List<RealEstateDto> realEstates = objectMapper.readValue(realEstate, new TypeReference<>() {});

        for (RealEstateDto r : realEstates) {
            List<RealEstate> history = realEstateRepo.findTop12ByZidOrderByDateDesc(r.getZid());
            Collections.reverse(history);
            r.setHistory(history);
        }

        return realEstates;
    }

    @RequestMapping(value = "/real-estates/summary", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public Object realEstatesSummary() throws JsonProcessingException {
        return realEstateService.getSummary();
    }

    @RequestMapping(value = "/real-estates/reload", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public void reloadRealEstates() {
        try {
            realEstateService.processCurrentMonth();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to reload", e);
        }
    }

}
