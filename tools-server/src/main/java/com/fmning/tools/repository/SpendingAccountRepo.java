package com.fmning.tools.repository;

import com.fmning.tools.domain.SpendingAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpendingAccountRepo extends CrudRepository<SpendingAccount, Integer> {
}
