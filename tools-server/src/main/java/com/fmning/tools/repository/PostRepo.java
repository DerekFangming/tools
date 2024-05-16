package com.fmning.tools.repository;

import com.fmning.tools.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepo extends CrudRepository<Post, Integer> {

    @Query("select p from Post p where p.created < :created and (saved <> true or saved is null)")
    List<Post> findByCreatedLessThanAndSavedIsNullOrSavedIsTrue(Instant created);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post set viewed =:viewed where id in (:idList)")
    void markAsRead(Instant viewed, List<Integer> idList);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post set saved = false where id in (:idList)")
    void markAsUnsaved(List<Integer> idList);

    Page<Post> findAll(Specification<Post> spec, Pageable pageable);
}
