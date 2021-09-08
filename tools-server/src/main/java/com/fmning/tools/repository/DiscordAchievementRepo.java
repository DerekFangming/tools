package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordAchievement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordAchievementRepo extends CrudRepository<DiscordAchievement, String> {
    List<DiscordAchievement> findAll();
}
