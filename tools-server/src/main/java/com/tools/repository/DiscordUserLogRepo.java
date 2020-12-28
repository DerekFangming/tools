package com.tools.repository;

import com.tools.domain.DiscordGuild;
import com.tools.domain.DiscordUserLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserLogRepo extends CrudRepository<DiscordUserLog, Integer> {
}
