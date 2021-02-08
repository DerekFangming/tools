package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordRoleRepo extends CrudRepository<DiscordRole, String> {
    Page<DiscordRole> findAll(Specification<DiscordRole> spec, Pageable pageable);

    default String getNameById(String id) {
        DiscordRole role = findById(id).orElse(null);
        return role == null ? null : role.getName();
    }
}
