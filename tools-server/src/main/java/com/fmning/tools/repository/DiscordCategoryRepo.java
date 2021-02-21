package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordCategoryRepo extends CrudRepository<DiscordCategory, String> {
}
