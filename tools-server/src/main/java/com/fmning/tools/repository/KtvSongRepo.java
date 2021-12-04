package com.fmning.tools.repository;

import com.fmning.tools.domain.KtvSong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface KtvSongRepo extends CrudRepository<KtvSong, Integer> {
    Page<KtvSong> findAll(Specification<KtvSong> spec, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value="delete from KtvSong where requested = false")
    void deleteAllSongs();
}
