package com.fmning.tools.repository;

import com.fmning.tools.domain.SpendingAccount;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpendingAccountRepo extends CrudRepository<SpendingAccount, Integer> {
    @NotNull
    List<SpendingAccount> findAll();
}
