package com.tools.repository;

import com.tools.domain.DiscordUserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordUserLogRepo extends CrudRepository<DiscordUserLog, Integer>, JpaSpecificationExecutor<DiscordUserLog> {

    List<DiscordUserLog> findAllByOrderByIdDesc();

    Page<DiscordUserLog> findAll(Specification<DiscordUserLog> spec, Pageable pageable);
}