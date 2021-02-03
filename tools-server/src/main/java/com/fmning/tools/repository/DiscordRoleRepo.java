package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordRoleRepo extends CrudRepository<DiscordRole, String> {
}
