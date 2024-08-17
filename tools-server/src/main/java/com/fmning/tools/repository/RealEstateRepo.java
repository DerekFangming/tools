package com.fmning.tools.repository;

import com.fmning.tools.domain.RealEstate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealEstateRepo extends CrudRepository<RealEstate, RealEstatePK> {

    List<RealEstate> findTop12ByZidOrderByDateDesc(String zid);
}
