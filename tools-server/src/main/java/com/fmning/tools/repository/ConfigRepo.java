package com.fmning.tools.repository;

import com.fmning.tools.domain.Config;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepo extends CrudRepository<Config, String> {
}
