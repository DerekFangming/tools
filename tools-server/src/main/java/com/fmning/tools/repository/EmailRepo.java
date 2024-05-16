package com.fmning.tools.repository;

import com.fmning.tools.domain.Email;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface EmailRepo extends CrudRepository<Email, Integer> {

    List<Email> findAllByOrderByIdDesc(Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Email set read =:read where id in (:idList)")
    void markAsRead(boolean read, List<Integer> idList);
}
