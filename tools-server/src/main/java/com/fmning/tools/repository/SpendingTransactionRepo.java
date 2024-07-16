package com.fmning.tools.repository;

import com.fmning.tools.domain.SpendingTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpendingTransactionRepo extends CrudRepository<SpendingTransaction, Integer> {
}
