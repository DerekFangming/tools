package com.fmning.tools.controller;

import com.fmning.tools.domain.Email;
import com.fmning.tools.domain.SpendingAccount;
import com.fmning.tools.dto.EmailDto;
import com.fmning.tools.repository.SpendingAccountRepo;
import com.fmning.tools.repository.SpendingTransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.fmning.tools.util.WebUtil.TOTAL_COUNT;

@CommonsLog
@RestController
@RequestMapping(value = "/api/spending")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SpendingController {

    private final SpendingAccountRepo accountRepo;
    private final SpendingTransactionRepo transactionRepo;

//    @GetMapping
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public List<SpendingAccount> getAccounts() {
        return StreamSupport
                .stream(accountRepo.findAll().spliterator(), false)
                .toList();
    }

//    @PostMapping
    @RequestMapping(value = "/accounts", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public SpendingAccount createAccount(@RequestBody SpendingAccount account) {
        account.setId(0);

        return accountRepo.save(account);
    }

//    @PutMapping
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

//    @DeleteMapping
    @RequestMapping(value = "/accounts/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPENDING')")
    public void deleteAccount(@PathVariable int id) {
        accountRepo.deleteById(id);
    }

}
