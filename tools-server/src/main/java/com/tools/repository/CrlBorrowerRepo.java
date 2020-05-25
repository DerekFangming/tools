package com.tools.repository;

import com.tools.domain.CrlBorrowerLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrlBorrowerRepo extends CrudRepository<CrlBorrowerLog, Integer> {

    CrlBorrowerLog findFirstByEquipmentIdOrderByBorrowDateDesc(int equipmentId);

}
