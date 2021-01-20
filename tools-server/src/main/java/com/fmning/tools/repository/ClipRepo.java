package com.fmning.tools.repository;

import com.fmning.tools.domain.Clip;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClipRepo extends CrudRepository<Clip, Integer> {

    Clip findTopByOrderByIdDesc();
}
