package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordTask;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DiscordTaskRepo extends CrudRepository<DiscordTask, String> {

    List<DiscordTask> findByTimeoutBefore(Instant timeout);
}
