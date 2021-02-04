package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordUserRepo extends CrudRepository<DiscordUser, String> {
    List<DiscordUser> findByBirthday(String birthday);
    List<DiscordUser> findByBirthdayNotNullOrderByBirthdayAsc();
    List<DiscordUser> findByBirthdayStartingWithOrderByBirthdayAsc(String birthday);
}
