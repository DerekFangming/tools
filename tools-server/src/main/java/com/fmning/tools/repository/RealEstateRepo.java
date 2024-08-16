package com.fmning.tools.repository;

import com.fmning.tools.domain.RealEstate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealEstateRepo extends CrudRepository<RealEstate, RealEstatePK> {
}
