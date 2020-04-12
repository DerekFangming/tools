package com.tools.dao;

import com.tools.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepo extends CrudRepository<Post, Integer> {

    List<Post> findByViewed(Instant viewed, Pageable pageable);

    List<Post> findByViewedLessThan(Instant viewed);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post set viewed =:viewed where id in (:idList)")
    void markAsRead(Instant viewed, List<Integer> idList);

    List<Post> findAll(Pageable pageable);
    List<Post> findByFlagged(boolean flagged, Pageable pageable);
}
