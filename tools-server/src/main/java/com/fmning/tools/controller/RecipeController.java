package com.fmning.tools.controller;

import com.fmning.tools.domain.Recipe;
import com.fmning.tools.repository.RecipeRepo;
import com.fmning.tools.type.RecipeCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.Instant;
import java.util.List;

@CommonsLog
@RestController
@RequestMapping(value = "/api/recipes")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RecipeController {

    private final RecipeRepo recipeRepo;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Recipe> getRecipes(@RequestParam("category") RecipeCategory category) {
        return recipeRepo.findAllByCategory(category);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Recipe getRecipe(@PathVariable int id) {
        return recipeRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Recipe with ID " + id + " is not found."));
    }

    @RequestMapping(value = "/editing", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public ResponseEntity<Void> editing() {
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public Recipe create(@RequestBody Recipe recipe) {
        recipe.setId(0);
        recipe.setCreated(Instant.now());

        return recipeRepo.save(recipe);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TL')")
    public Recipe update(@PathVariable int id, @RequestBody Recipe recipe)  {
        recipe.setId(id);
        recipe.setCreated(recipeRepo.findById(id).orElseThrow().getCreated());
        return recipeRepo.save(recipe);
    }

}
