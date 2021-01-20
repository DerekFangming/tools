package com.fmning.tools.repository;

import com.fmning.tools.domain.CrlEquipment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrlEquipmentRepo extends CrudRepository<CrlEquipment, Integer> {
}
