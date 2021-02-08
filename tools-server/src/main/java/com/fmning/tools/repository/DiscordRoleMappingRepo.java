package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRoleMapping;
import com.fmning.tools.type.DiscordRoleType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DiscordRoleMappingRepo extends CrudRepository<DiscordRoleMapping, Integer> {
    DiscordRoleMapping findByOwnerIdAndType(String ownerId, DiscordRoleType type);
    DiscordRoleMapping findByOwnerIdAndTypeAndRoleId(String ownerId, DiscordRoleType type, String roleId);
    DiscordRoleMapping findByCode(String code);

    @Transactional
    void deleteByRoleId(String roleId);

    List<DiscordRoleMapping> findByOwnerId(String ownerId);
}
