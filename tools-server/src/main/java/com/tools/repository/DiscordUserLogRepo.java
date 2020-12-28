package com.tools.repository;

import com.tools.domain.DiscordUserLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordUserLogRepo extends CrudRepository<DiscordUserLog, Integer> {

    List<DiscordUserLog> findAllByOrderByIdDesc();
}
