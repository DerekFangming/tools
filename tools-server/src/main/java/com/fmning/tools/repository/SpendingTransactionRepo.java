package com.fmning.tools.repository;

import com.fmning.tools.domain.SpendingTransaction;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface SpendingTransactionRepo extends CrudRepository<SpendingTransaction, Integer> {

    @NotNull
    List<SpendingTransaction> findAll();
    List<SpendingTransaction> findAllByDateAfter(Date date);
    List<SpendingTransaction> findAllByDateAfterAndDateBefore(Date from, Date to);
}
