package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRoleMapping;
import com.fmning.tools.type.DiscordRoleType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface DiscordRoleMappingRepo extends CrudRepository<DiscordRoleMapping, Integer> {
    DiscordRoleMapping findByOwnerIdAndType(String ownerId, DiscordRoleType type);
    DiscordRoleMapping findByOwnerIdAndTypeAndRoleId(String ownerId, DiscordRoleType type, String roleId);
    DiscordRoleMapping findByCode(String code);
    DiscordRoleMapping findByTypeAndRoleId(DiscordRoleType type, String roleId);

    @Transactional
    void deleteByRoleId(String roleId);

    @Modifying
    @Transactional
    @Query(value="delete from DiscordRoleMapping d where d.created < ?1")
    void deleteByCreated(Instant created);

    List<DiscordRoleMapping> findByOwnerId(String ownerId);
}
