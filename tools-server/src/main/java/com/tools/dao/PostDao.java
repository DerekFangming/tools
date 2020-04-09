package com.tools.dao;

import com.tools.domain.Post;
import org.springframework.stereotype.Component;

@Component
public class PostDao extends CommonDao<Post> {

    protected PostDao() {
        super(Post.class);
    }
}
