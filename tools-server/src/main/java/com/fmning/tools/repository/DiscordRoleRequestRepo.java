package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRoleRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordRoleRequestRepo extends CrudRepository<DiscordRoleRequest, String> {
}
