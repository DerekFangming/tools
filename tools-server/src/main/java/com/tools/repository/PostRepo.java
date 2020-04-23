package com.tools.repository;

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

    Long countByViewed(Instant viewed);

    List<Post> findByViewedLessThan(Instant viewed);

    List<Post> findByViewedAndFlagged(Instant viewed, boolean flagged, Pageable pageable);

    List<Post> findByViewedAndRankGreaterThan(Instant viewed, int rank, Pageable pageable);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post set viewed =:viewed where id in (:idList)")
    void markAsRead(Instant viewed, List<Integer> idList);

    List<Post> findAll(Pageable pageable);
    List<Post> findByFlagged(boolean flagged, Pageable pageable);
}
