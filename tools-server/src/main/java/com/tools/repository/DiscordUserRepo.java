package com.tools.repository;

import com.tools.domain.DiscordUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordUserRepo extends CrudRepository<DiscordUser, Long> {
    List<DiscordUser> findByBirthday(String birthday);
    List<DiscordUser> findByBirthdayNotNull();
    List<DiscordUser> findByBirthdayStartingWith(String birthday);
}
