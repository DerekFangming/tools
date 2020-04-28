package com.tools.repository;

import com.tools.domain.Email;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepo extends CrudRepository<Email, Integer> {
}
