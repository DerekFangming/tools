package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRoleRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface DiscordRoleRequestRepo extends CrudRepository<DiscordRoleRequest, String> {

    @Modifying
    @Transactional
    @Query(value="delete from DiscordRoleRequest d where d.created < ?1")
    void deleteByCreated(Instant created);
}
