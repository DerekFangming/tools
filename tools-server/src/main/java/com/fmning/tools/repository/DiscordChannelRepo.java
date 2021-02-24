package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscordChannelRepo extends CrudRepository<DiscordChannel, String> {
    List<DiscordChannel> findAll();
    Page<DiscordChannel> findAll(Specification<DiscordChannel> spec, Pageable pageable);
}
