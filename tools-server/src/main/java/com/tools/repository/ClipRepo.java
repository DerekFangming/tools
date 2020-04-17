package com.tools.repository;

import com.tools.domain.Clip;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClipRepo extends CrudRepository<Clip, Integer> {

    Clip findTopByOrderByIdDesc();
}
