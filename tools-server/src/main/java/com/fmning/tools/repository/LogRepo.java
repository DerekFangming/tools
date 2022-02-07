package com.fmning.tools.repository;

import com.fmning.tools.domain.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepo extends CrudRepository<Log, Integer> {
    Page<Log> findAll(Specification<Log> spec, Pageable pageable);
}
