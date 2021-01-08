package com.tools.repository;

import com.tools.domain.Image;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ImageRepo extends CrudRepository<Image, Integer> {

    List<Image> findAllByOrderByIdDesc();
}