package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordChannel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordChannelRepo extends CrudRepository<DiscordChannel, String> {
    List<DiscordChannel> findAll();
}
