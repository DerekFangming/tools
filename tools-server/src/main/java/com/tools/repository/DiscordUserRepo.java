package com.tools.repository;

import com.tools.domain.DiscordUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordUserRepo extends CrudRepository<DiscordUser, Long> {
}
