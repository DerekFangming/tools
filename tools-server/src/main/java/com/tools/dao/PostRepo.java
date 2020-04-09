package com.tools.dao;

import com.tools.domain.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepo  extends CrudRepository<Post, Integer> {
}
