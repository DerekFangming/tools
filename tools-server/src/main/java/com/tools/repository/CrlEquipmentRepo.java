package com.tools.repository;

import com.tools.domain.CrlEquipment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrlEquipmentRepo extends CrudRepository<CrlEquipment, Integer> {
}
