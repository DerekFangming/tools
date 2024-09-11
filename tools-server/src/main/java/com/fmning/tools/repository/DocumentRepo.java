package com.fmning.tools.repository;

import com.fmning.tools.domain.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepo extends CrudRepository<Document, Integer> {

    @NotNull
    List<Document> findAll();
}
