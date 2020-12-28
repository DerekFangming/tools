package com.tools.repository;

import com.tools.domain.DiscordGuild;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordGuildRepo extends CrudRepository<DiscordGuild, String> {
}
