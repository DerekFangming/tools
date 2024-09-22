package com.fmning.tools.repository;

import com.fmning.tools.domain.Recipe;
import com.fmning.tools.type.RecipeCategory;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepo extends CrudRepository<Recipe, Integer> {

    @NotNull
    List<Recipe> findAllByCategory(RecipeCategory category);
}
